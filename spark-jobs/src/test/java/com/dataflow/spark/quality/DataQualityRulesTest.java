package com.dataflow.spark.quality;

import org.apache.spark.sql.Column;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataQualityRulesTest {

    @Test
    void shouldDefineValidColumnExpressions() {
        Column idCol = DataQualityRules.isValidRestaurantId();
        assertThat(idCol).isNotNull();

        Column nameCol = DataQualityRules.isValidName();
        assertThat(nameCol).isNotNull();

        Column cityCol = DataQualityRules.isValidCity();
        assertThat(cityCol).isNotNull();

        Column ratingCol = DataQualityRules.isValidRating();
        assertThat(ratingCol).isNotNull();

        Column costCol = DataQualityRules.isValidCost();
        assertThat(costCol).isNotNull();

        Column allValidCol = DataQualityRules.isAllValid();
        assertThat(allValidCol).isNotNull();

        Column errorCodeCol = DataQualityRules.buildErrorCode();
        assertThat(errorCodeCol).isNotNull();
    }
}
