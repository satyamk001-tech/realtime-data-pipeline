package com.dataflow.backend.service;

import com.dataflow.backend.exception.SecurityValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextToSqlServiceTest {

    private TextToSqlService textToSqlService;

    @BeforeEach
    void setUp() {
        textToSqlService = new TextToSqlService(null);
    }

    @Test
    void shouldPermitSafeSelectQueryOnWhitelistedTable() {
        String safeSql = "SELECT name, city, aggregate_rating FROM RESTAURANTS WHERE aggregate_rating > 4.0";
        assertThatCode(() -> textToSqlService.validateSqlSafety(safeSql))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDropTableStatement() {
        String maliciousSql = "DROP TABLE restaurants";
        assertThatThrownBy(() -> textToSqlService.validateSqlSafety(maliciousSql))
                .isInstanceOf(SecurityValidationException.class)
                .hasMessageContaining("Prohibited statement type");
    }

    @Test
    void shouldRejectDeleteStatement() {
        String maliciousSql = "DELETE FROM restaurants WHERE 1=1";
        assertThatThrownBy(() -> textToSqlService.validateSqlSafety(maliciousSql))
                .isInstanceOf(SecurityValidationException.class)
                .hasMessageContaining("Prohibited statement type");
    }

    @Test
    void shouldRejectUnauthorizedTableAccess() {
        String unauthorizedSql = "SELECT * FROM users";
        assertThatThrownBy(() -> textToSqlService.validateSqlSafety(unauthorizedSql))
                .isInstanceOf(SecurityValidationException.class)
                .hasMessageContaining("Unauthorized table access: 'users'");
    }

    @Test
    void shouldRejectStackedMultiStatementQueries() {
        String stackedSql = "SELECT * FROM restaurants; DROP TABLE restaurants";
        assertThatThrownBy(() -> textToSqlService.validateSqlSafety(stackedSql))
                .isInstanceOf(SecurityValidationException.class)
                .hasMessageContaining("Multi-statement");
    }

    @Test
    void shouldRejectSqlCommentDelimiters() {
        String commentSql = "SELECT * FROM restaurants -- bypass";
        assertThatThrownBy(() -> textToSqlService.validateSqlSafety(commentSql))
                .isInstanceOf(SecurityValidationException.class)
                .hasMessageContaining("comment delimiters");
    }
}
