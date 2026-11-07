package com.dataflow.backend.service;

import com.dataflow.backend.dto.TextToSqlRequest;
import com.dataflow.backend.dto.TextToSqlResponse;
import com.dataflow.backend.exception.SecurityValidationException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
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
            "RESTAURANT_REVIEWS",
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
        TextToSqlResponse response = new TextToSqlResponse();
        response.setQuestion(request.getQuestion());

        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            response.setSafe(false);
            response.setRejectionReason("Input query cannot be empty");
            return response;
        }

        // 1. Resolve structured analytical SQL template from user intent
        String candidateSql = resolveAnalyticalSqlTemplate(request.getQuestion().trim());
        response.setGeneratedSql(candidateSql);

        // 2. Strict AST Security Gate
        try {
            validateSqlSafety(candidateSql);
            response.setSafe(true);
        } catch (SecurityValidationException e) {
            log.warn("AST security validator rejected query: {}", e.getMessage());
            response.setSafe(false);
            response.setRejectionReason(e.getMessage());
            return response;
        }

        // 3. Execution under read-only connection with strict 5-second statement timeout
        try (Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            conn.setReadOnly(true);
            stmt.setQueryTimeout(5);

            try (ResultSet rs = stmt.executeQuery(candidateSql)) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    columns.add(meta.getColumnLabel(i));
                }
                response.setColumnNames(columns);

                List<Map<String, Object>> rows = new ArrayList<>();
                int rowLimit = 50;
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
                response.setExplanation(String.format("Executed AST-verified query against analytical schema in %d ms (%d rows retrieved).",
                        response.getExecutionDurationMs(), rows.size()));
            }

        } catch (Exception e) {
            log.error("Execution error on validated query '{}': {}", candidateSql, e.getMessage());
            response.setSafe(false);
            response.setRejectionReason("Database execution error: " + e.getMessage());
        }

        return response;
    }

    public void validateSqlSafety(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new SecurityValidationException("SQL query string is empty");
        }

        String sanitized = sql.trim();
        if (sanitized.endsWith(";")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }

        // Layer 1: Prohibit multi-statement batching (semicolon injection)
        if (sanitized.contains(";")) {
            throw new SecurityValidationException("Multi-statement query injection detected (semicolons are strictly prohibited)");
        }

        // Layer 2: Reject SQL comment evasion
        if (sanitized.contains("--") || sanitized.contains("/*")) {
            throw new SecurityValidationException("SQL comment delimiters are prohibited");
        }

        // Layer 3: Abstract Syntax Tree (AST) Parsing via JSqlParser
        Statement stmt;
        try {
            stmt = CCJSqlParserUtil.parse(sanitized);
        } catch (Exception e) {
            throw new SecurityValidationException("AST Parsing failed (invalid SQL syntax): " + e.getMessage());
        }

        // Layer 4: Statement Type Enforce (Strictly SELECT only)
        if (!(stmt instanceof Select)) {
            throw new SecurityValidationException("Non-SELECT statement rejected. DDL and DML operations are strictly blocked");
        }

        // Layer 5: Table Whitelist Verification
        TablesNamesFinder tablesFinder = new TablesNamesFinder();
        List<String> tables = tablesFinder.getTableList(stmt);
        for (String table : tables) {
            String normalizedTable = table.replace("\"", "").replace("'", "").trim().toUpperCase();
            if (!ALLOWED_TABLES.contains(normalizedTable)) {
                throw new SecurityValidationException("Access violation: Table '" + table + "' is not permitted under read-only analytical policy");
            }
        }
    }

    private String resolveAnalyticalSqlTemplate(String query) {
        String q = query.toLowerCase();
        if (q.contains("cuisine") && (q.contains("best") || q.contains("rating") || q.contains("top"))) {
            return "SELECT cuisines, COUNT(*) AS count, ROUND(AVG(aggregate_rating)::numeric, 2) AS avg_rating " +
                   "FROM restaurants GROUP BY cuisines HAVING COUNT(*) >= 3 ORDER BY avg_rating DESC LIMIT 10";
        } else if (q.contains("city") || q.contains("location")) {
            return "SELECT city, COUNT(*) AS restaurant_count, ROUND(AVG(average_cost_for_two)::numeric, 2) AS avg_cost, " +
                   "ROUND(AVG(aggregate_rating)::numeric, 2) AS avg_rating " +
                   "FROM restaurants GROUP BY city ORDER BY restaurant_count DESC LIMIT 10";
        } else if (q.contains("cost") || q.contains("expensive") || q.contains("cheap")) {
            return "SELECT name, city, locality, cuisines, average_cost_for_two, aggregate_rating " +
                   "FROM restaurants ORDER BY average_cost_for_two DESC LIMIT 10";
        } else {
            return "SELECT name, city, locality, cuisines, aggregate_rating, votes " +
                   "FROM restaurants ORDER BY aggregate_rating DESC, votes DESC LIMIT 10";
        }
    }
}
