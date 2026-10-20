package com.dataflow.spark.etl;

import com.dataflow.spark.config.SparkConfig;
import com.dataflow.spark.model.EtlMetrics;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantBatchEtlJobTest {

    private static SparkSession spark;

    @BeforeAll
    static void initSpark() {
        int javaMajor = Runtime.version().feature();
        org.junit.jupiter.api.Assumptions.assumeTrue(javaMajor < 24,
                "Embedded local SparkContext requires JDK <= 21 due to Hadoop UserGroupInformation Subject.getSubject() removal in JDK 24+. In production, Spark runs on Java 17/21 containers.");
        spark = SparkConfig.createSparkSession("Test-Spark-Batch-ETL", "local[2]");
    }

    @AfterAll
    static void stopSpark() {
        if (spark != null) {
            spark.stop();
        }
    }

    @Test
    void shouldProcessBatchAndQuarantineAnomaliesAndDuplicates(@TempDir Path tempDir) throws IOException {
        File csvFile = tempDir.resolve("test_restaurants.csv").toFile();
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("restaurant_id,name,city,address,locality,cuisines,average_cost_for_two,currency,has_online_delivery,has_table_booking,price_range,aggregate_rating,rating_text,votes,review_text\n");
            // Valid 1
            writer.write("101,Royal Spice,Bangalore,Address 1,Indiranagar,North Indian,800.0,INR,Yes,No,2,4.5,Excellent,500,Great food\n");
            // Valid 2
            writer.write("102,Cafe Blue,Mumbai,Address 2,Bandra,Italian,1200.0,INR,No,Yes,3,4.2,Very Good,300,Nice ambiance\n");
            // Invalid Rating (7.8 > 5.0)
            writer.write("103,Broken Rating,Delhi,Address 3,CP,Chinese,600.0,INR,Yes,No,2,7.8,Poor,100,Bad rating\n");
            // Invalid Cost (-500 <= 0)
            writer.write("104,Negative Cost,Pune,Address 4,Baner,Fast Food,-500.0,INR,Yes,No,1,3.5,Average,50,Strange cost\n");
            // Duplicate of 101 with lower votes (votes=10 vs votes=500)
            writer.write("101,Royal Spice Dup,Bangalore,Address 1,Indiranagar,North Indian,800.0,INR,Yes,No,2,4.5,Excellent,10,Duplicate review\n");
        }

        String outputDir = tempDir.resolve("lake").toString();
        RestaurantBatchEtlJob job = new RestaurantBatchEtlJob(spark);
        EtlMetrics metrics = job.execute(csvFile.getAbsolutePath(), outputDir, "test-run-001");

        assertThat(metrics.getInputRecords()).isEqualTo(5);
        assertThat(metrics.getValidRecords()).isEqualTo(2);
        assertThat(metrics.getQuarantinedRecords()).isEqualTo(3);
        assertThat(metrics.getDuplicateRecords()).isEqualTo(1);

        // Verify valid Parquet dataset
        Dataset<Row> validDf = spark.read().parquet(metrics.getProcessedPath());
        assertThat(validDf.count()).isEqualTo(2);

        // Verify quarantine Parquet dataset
        Dataset<Row> quarantineDf = spark.read().parquet(metrics.getQuarantinePath());
        assertThat(quarantineDf.count()).isEqualTo(3);

        // Verify error codes
        var errorCodes = quarantineDf.select("error_code").as(org.apache.spark.sql.Encoders.STRING()).collectAsList();
        assertThat(errorCodes).contains("ERR_INVALID_RATING", "ERR_INVALID_COST", "ERR_DUPLICATE_RECORD");
    }
}
