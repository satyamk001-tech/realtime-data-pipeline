package com.dataflow.spark.config;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkConfig {
    private static final Logger log = LoggerFactory.getLogger(SparkConfig.class);

    public static SparkSession createSparkSession(String appName, String masterUrl) {
        String master = (masterUrl != null && !masterUrl.isEmpty()) ? masterUrl : "local[*]";
        log.info("Initializing SparkSession [app: '{}', master: '{}']", appName, master);

        SparkConf conf = new SparkConf()
                .setAppName(appName)
                .setMaster(master)
                .set("spark.sql.shuffle.partitions", "4")
                .set("spark.default.parallelism", "4")
                .set("spark.driver.host", "localhost")
                .set("spark.sql.session.timeZone", "UTC");

        // MinIO / S3A Configuration
        String s3Endpoint = System.getenv().getOrDefault("MINIO_ENDPOINT", "http://localhost:9000");
        String s3AccessKey = System.getenv().getOrDefault("MINIO_ACCESS_KEY", "minioadmin");
        String s3SecretKey = System.getenv().getOrDefault("MINIO_SECRET_KEY", "minioadmin123");

        conf.set("spark.hadoop.fs.s3a.endpoint", s3Endpoint)
            .set("spark.hadoop.fs.s3a.access.key", s3AccessKey)
            .set("spark.hadoop.fs.s3a.secret.key", s3SecretKey)
            .set("spark.hadoop.fs.s3a.path.style.access", "true")
            .set("spark.hadoop.fs.s3a.connection.ssl.enabled", "false")
            .set("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");

        return SparkSession.builder()
                .config(conf)
                .getOrCreate();
    }
}
