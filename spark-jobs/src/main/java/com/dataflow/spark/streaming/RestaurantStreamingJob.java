package com.dataflow.spark.streaming;

import com.dataflow.spark.config.SparkConfig;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.spark.sql.functions.*;

public class RestaurantStreamingJob {
    private static final Logger log = LoggerFactory.getLogger(RestaurantStreamingJob.class);

    public static StructType getEventSchema() {
        return new StructType()
                .add("eventId", DataTypes.StringType, false)
                .add("eventType", DataTypes.StringType, false)
                .add("restaurantId", DataTypes.LongType, false)
                .add("restaurantName", DataTypes.StringType, true)
                .add("city", DataTypes.StringType, true)
                .add("rating", DataTypes.DoubleType, true)
                .add("orderAmount", DataTypes.DoubleType, true)
                .add("eventTimestamp", DataTypes.TimestampType, false)
                .add("source", DataTypes.StringType, true);
    }

    public static void runStreaming(String bootstrapServers, String topic, String checkpointDir) throws Exception {
        SparkSession spark = SparkConfig.createSparkSession("DataFlow-Spark-Streaming", "local[*]");

        log.info("Starting Spark Structured Streaming from Kafka topic '{}'...", topic);

        // Read stream from Kafka
        Dataset<Row> kafkaRawStream = spark.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", bootstrapServers)
                .option("subscribe", topic)
                .option("startingOffsets", "latest")
                .option("failOnDataLoss", "false")
                .load();

        // Parse JSON payload
        Dataset<Row> parsedEvents = kafkaRawStream
                .selectExpr("CAST(key AS STRING) AS messageKey", "CAST(value AS STRING) AS jsonPayload")
                .withColumn("data", from_json(col("jsonPayload"), getEventSchema()))
                .select("data.*");

        // Filter valid events with watermark
        Dataset<Row> watermarkedEvents = parsedEvents
                .filter(col("eventId").isNotNull().and(col("eventTimestamp").isNotNull()))
                .withWatermark("eventTimestamp", "10 minutes");

        // 1-minute tumbling window aggregations
        Dataset<Row> windowedMetrics = watermarkedEvents
                .groupBy(
                        window(col("eventTimestamp"), "1 minute"),
                        col("city"),
                        col("eventType")
                )
                .agg(
                        count("*").as("event_count"),
                        avg("rating").as("avg_rating"),
                        sum("orderAmount").as("total_volume")
                )
                .select(
                        col("window.start").as("window_start"),
                        col("window.end").as("window_end"),
                        col("city"),
                        col("eventType"),
                        col("event_count"),
                        round(col("avg_rating"), 2).as("avg_rating"),
                        round(col("total_volume"), 2).as("total_volume")
                );

        // Write stream to Console (or Parquet/Delta sink in production)
        StreamingQuery query = windowedMetrics.writeStream()
                .format("console")
                .outputMode(OutputMode.Update())
                .option("truncate", "false")
                .option("checkpointLocation", checkpointDir)
                .trigger(Trigger.ProcessingTime("10 seconds"))
                .start();

        log.info("Streaming query initialized. Awaiting termination...");
        query.awaitTermination();
    }

    public static void main(String[] args) throws Exception {
        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String topic = System.getenv().getOrDefault("KAFKA_TOPIC_RESTAURANTS", "dataflow.restaurant.events");
        String checkpoint = "target/checkpoints/streaming";

        runStreaming(bootstrapServers, topic, checkpoint);
    }
}
