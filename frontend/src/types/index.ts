export interface Restaurant {
  id: number;
  name: string;
  city: string;
  locality?: string;
  address?: string;
  cuisines?: string;
  averageCostForTwo: number;
  currency: string;
  hasOnlineDelivery: boolean;
  hasTableBooking: boolean;
  priceRange: number;
  aggregateRating: number;
  ratingText: string;
  votes: number;
  pipelineRunId?: string;
}

export interface AnalyticsOverview {
  totalRestaurants: number;
  overallAverageRating: number;
  overallAverageCost: number;
  totalVotes: number;
  onlineDeliveryCount: number;
  onlineDeliveryPercentage: number;
  tableBookingCount: number;
  tableBookingPercentage: number;
}

export interface LocationAnalytics {
  city: string;
  restaurantCount: number;
  averageRating: number;
  averageCost: number;
}

export interface CuisineAnalytics {
  cuisine: string;
  restaurantCount: number;
  averageRating: number;
}

export interface PipelineJob {
  id: string;
  runId: string;
  pipelineType: string;
  status: 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED';
  sourcePath?: string;
  targetPath?: string;
  inputRecords: number;
  validRecords: number;
  quarantinedRecords: number;
  duplicateRecords: number;
  durationMs: number;
  triggeredBy: string;
  errorMessage?: string;
  startedAt: string;
  completedAt?: string;
}

export interface TextToSqlResult {
  question: string;
  generatedSql: string;
  safe: boolean;
  rejectionReason?: string;
  executionDurationMs: number;
  columnNames: string[];
  rows: Record<string, any>[];
  rowCount: number;
  explanation: string;
}

export interface SourceReview {
  reviewId: string;
  restaurantName: string;
  city: string;
  rating: number;
  reviewText: string;
  sentiment: string;
  similarityScore: number;
}

export interface RagResult {
  query: string;
  answer: string;
  sourceReviews: SourceReview[];
  searchDurationMs: number;
}

export interface SystemHealth {
  status: string;
  databaseConnected: boolean;
  kafkaConnected: boolean;
  minioConnected: boolean;
  totalProcessedRestaurants: number;
  memoryUsedMb: number;
  memoryMaxMb: number;
  uptimeSeconds: number;
}
