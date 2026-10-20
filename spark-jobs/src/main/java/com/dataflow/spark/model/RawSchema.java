package com.dataflow.spark.model;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class RawSchema {

    public static StructType getRestaurantCsvSchema() {
        return new StructType(new StructField[]{
                DataTypes.createStructField("restaurant_id", DataTypes.StringType, true),
                DataTypes.createStructField("name", DataTypes.StringType, true),
                DataTypes.createStructField("city", DataTypes.StringType, true),
                DataTypes.createStructField("address", DataTypes.StringType, true),
                DataTypes.createStructField("locality", DataTypes.StringType, true),
                DataTypes.createStructField("cuisines", DataTypes.StringType, true),
                DataTypes.createStructField("average_cost_for_two", DataTypes.StringType, true),
                DataTypes.createStructField("currency", DataTypes.StringType, true),
                DataTypes.createStructField("has_online_delivery", DataTypes.StringType, true),
                DataTypes.createStructField("has_table_booking", DataTypes.StringType, true),
                DataTypes.createStructField("price_range", DataTypes.StringType, true),
                DataTypes.createStructField("aggregate_rating", DataTypes.StringType, true),
                DataTypes.createStructField("rating_text", DataTypes.StringType, true),
                DataTypes.createStructField("votes", DataTypes.StringType, true),
                DataTypes.createStructField("review_text", DataTypes.StringType, true)
        });
    }
}
