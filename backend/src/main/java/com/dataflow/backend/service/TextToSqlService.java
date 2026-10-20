package com.dataflow.backend.service;

import com.dataflow.backend.dto.TextToSqlRequest;
import com.dataflow.backend.dto.TextToSqlResponse;
import com.dataflow.backend.exception.SecurityValidationException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

@Service
public class TextToSqlService {
    private static final Logger log = LoggerFactory.getLogger(TextToSqlService.class);

    private final DataSource dataSource;

    private static final Set<String> ALLOWED_TABLES = Set.of(
            "RESTAURANTS",
            "DIM_RESTAURANT",
            "DIM_LOCATION",
            "DIM_CUISINE",
            "FACT_RESTAURANT_METRICS"
    );

    public TextToSqlService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public TextToSqlResponse processQuestion(TextToSqlRequest request) {
        long startTime = System.currentTimeMillis();
        String question = request.getQuestion();
        TextToSqlResponse response = new TextToSqlResponse();
        response.setQuestion(question);

        if (question == null || question.trim().isEmpty()) {
            response.setSafe(false);
            response.setRejectionReason("Question prompt cannot be empty");
            return response;
        }

        // Generate candidate SQL from intent
        String candidateSql = generateSqlFromQuestion(question);
        response.setGeneratedSql(candidateSql);

        // AST Security Validation
        try {
            validateSqlSafety(candidateSql);
            response.setSafe(true);
        } catch (SecurityValidationException e) {
            log.warn("SQL security validation rejected candidate query: {}", e.getMessage());
            response.setSafe(false);
            response.setRejectionReason(e.getMessage());
            return response;
        }

        // Execute validated read-only SQL with strict 5-second timeout
        try (Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(5); // 5-second statement timeout

            try (ResultSet rs = stmt.executeQuery(candidateSql)) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    columns.add(meta.getColumnLabel(i));
                }
                response.setColumnNames(columns);

                List<Map<String, Object>> rows = new ArrayList<>();
                int rowLimit = 100;
                while (rs.next() && rowLimit-- > 0) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }

                response.setRows(rows);
                response.setRowCount(rows.size());
                response.setExecutionDurationMs(System.currentTimeMillis() - startTime);
                response.setExplanation(generateExplanation(question, rows.size()));
            }

        } catch (Exception e) {
            log.error("Execution of validated SQL failed: {}", candidateSql, e);
            response.setSafe(false);
            response.setRejectionReason("Query execution error: " + e.getMessage());
        }

        return response;
    }

    public void validateSqlSafety(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new SecurityValidationException("SQL query is empty");
        }

        // 1. Prohibit semicolons (prevent stacked / multiple statement injection)
        String trimmed = sql.trim();
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.contains(";")) {
            throw new SecurityValidationException("Multi-statement queries containing semicolons are strictly prohibited");
        }

        // 2. Prohibit SQL comment delimiters
        if (trimmed.contains("--") || trimmed.contains("/*")) {
            throw new SecurityValidationException("SQL comment delimiters are prohibited in generated queries");
        }

        // 3. Parse AST using JSqlParser
        Statement stmt;
        try {
            stmt = CCJSqlParserUtil.parse(trimmed);
        } catch (Exception e) {
            throw new SecurityValidationException("SQL parsing failed: syntax is invalid or unsupported (" + e.getMessage() + ")");
        }

        // 4. Enforce that statement is strictly a SELECT statement
        if (!(stmt instanceof Select)) {
            throw new SecurityValidationException("Prohibited statement type: only SELECT queries are permitted");
        }

        // 5. Whitelist Table Names
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(stmt);
        for (String table : tableList) {
            String cleanTable = table.replace("\"", "").replace("'", "").trim().toUpperCase();
            if (!ALLOWED_TABLES.contains(cleanTable)) {
                throw new SecurityValidationException("Unauthorized table access: '" + table + "' is not in the analytical whitelist");
            }
        }
    }

    private String generateSqlFromQuestion(String q) {
        String lower = q.toLowerCase();
        if (lower.contains("cuisine") && (lower.contains("rating") || lower.contains("best") || lower.contains("highest"))) {
            return "SELECT cuisines, COUNT(*) AS restaurant_count, ROUND(AVG(aggregate_rating), 2) AS avg_rating " +
                    "FROM restaurants GROUP BY cuisines HAVING COUNT(*) >= 5 ORDER BY avg_rating DESC LIMIT 10";
        } else if (lower.contains("city") && (lower.contains("count") || lower.contains("how many") || lower.contains("distribution"))) {
            return "SELECT city, COUNT(*) AS restaurant_count, ROUND(AVG(aggregate_rating), 2) AS avg_rating, ROUND(AVG(average_cost_for_two), 2) AS avg_cost " +
                    "FROM restaurants GROUP BY city ORDER BY restaurant_count DESC LIMIT 10";
        } else if (lower.contains("cost") || lower.contains("expensive") || lower.contains("price")) {
            return "SELECT city, locality, ROUND(AVG(average_cost_for_two), 2) AS avg_cost, COUNT(*) AS total_restaurants " +
                    "FROM restaurants GROUP BY city, locality ORDER BY avg_cost DESC LIMIT 10";
        } else if (lower.contains("delivery") || lower.contains("online")) {
            return "SELECT city, has_online_delivery, COUNT(*) AS count " +
                    "FROM restaurants GROUP BY city, has_online_delivery ORDER BY city LIMIT 20";
        } else {
            // General top-rated restaurants query
            return "SELECT name, city, locality, cuisines, average_cost_for_two, aggregate_rating, votes " +
                    "FROM restaurants ORDER BY aggregate_rating DESC, votes DESC LIMIT 10";
        }
    }

    private String generateExplanation(String question, int resultCount) {
        return String.format("Query executed securely under read-only analytical policy. Retrieved %d record(s) answering: '%s'",
                resultCount, question);
    }
}
