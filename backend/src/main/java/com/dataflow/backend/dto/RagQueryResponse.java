package com.dataflow.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class RagQueryResponse {
    private String query;
    private String answer;
    private List<SourceReview> sourceReviews;
    private long searchDurationMs;

    public static class SourceReview {
        private String reviewId;
        private String restaurantName;
        private String city;
        private BigDecimal rating;
        private String reviewText;
        private String sentiment;
        private double similarityScore;

        public SourceReview() {}

        public SourceReview(String reviewId, String restaurantName, String city,
                            BigDecimal rating, String reviewText, String sentiment, double similarityScore) {
            this.reviewId = reviewId;
            this.restaurantName = restaurantName;
            this.city = city;
            this.rating = rating;
            this.reviewText = reviewText;
            this.sentiment = sentiment;
            this.similarityScore = similarityScore;
        }

        public String getReviewId() { return reviewId; }
        public void setReviewId(String reviewId) { this.reviewId = reviewId; }
        public String getRestaurantName() { return restaurantName; }
        public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public BigDecimal getRating() { return rating; }
        public void setRating(BigDecimal rating) { this.rating = rating; }
        public String getReviewText() { return reviewText; }
        public void setReviewText(String reviewText) { this.reviewText = reviewText; }
        public String getSentiment() { return sentiment; }
        public void setSentiment(String sentiment) { this.sentiment = sentiment; }
        public double getSimilarityScore() { return similarityScore; }
        public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
    }

    public RagQueryResponse() {}

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public List<SourceReview> getSourceReviews() { return sourceReviews; }
    public void setSourceReviews(List<SourceReview> sourceReviews) { this.sourceReviews = sourceReviews; }
    public long getSearchDurationMs() { return searchDurationMs; }
    public void setSearchDurationMs(long searchDurationMs) { this.searchDurationMs = searchDurationMs; }
}
