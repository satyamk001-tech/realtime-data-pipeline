# DataFlow REST API Documentation

Base URL: `http://localhost:8080/api/v1`  
OpenAPI / Swagger UI: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON Spec: `http://localhost:8080/v3/api-docs`

---

## 1. Restaurant Discovery Endpoints

### `GET /api/v1/restaurants`
Retrieve paginated and filtered restaurant profiles.

**Query Parameters:**
- `page` (int, default `0`): Page index.
- `size` (int, default `20`): Page size.
- `keyword` (string, optional): Search across name, locality, and cuisines.
- `city` (string, optional): Filter by metropolitan city (e.g. `Bangalore`, `New Delhi`).
- `sortBy` (string, default `aggregateRating`): Field to sort by.
- `direction` (string, default `desc`): `asc` or `desc`.

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1001,
      "name": "The Royal Kitchen",
      "city": "Bangalore",
      "locality": "Indiranagar",
      "address": "Shop 42, 2nd Floor, Indiranagar, Bangalore",
      "cuisines": "North Indian, Continental",
      "averageCostForTwo": 850.00,
      "currency": "INR",
      "hasOnlineDelivery": true,
      "hasTableBooking": false,
      "priceRange": 2,
      "aggregateRating": 4.50,
      "ratingText": "Excellent",
      "votes": 775,
      "pipelineRunId": "run-98a72b..."
    }
  ],
  "totalElements": 1440,
  "totalPages": 72,
  "size": 20,
  "number": 0
}
```

---

## 2. Analytics Endpoints

### `GET /api/v1/analytics/overview`
System-wide metrics and KPIs across all ingested restaurants.

**Response (200 OK):**
```json
{
  "totalRestaurants": 1440,
  "overallAverageRating": 4.12,
  "overallAverageCost": 845.50,
  "totalVotes": 1248390,
  "onlineDeliveryCount": 986,
  "onlineDeliveryPercentage": 68.5,
  "tableBookingCount": 348,
  "tableBookingPercentage": 24.2
}
```

### `GET /api/v1/analytics/locations`
Geographic metrics aggregated by metropolitan market.

### `GET /api/v1/analytics/cuisines`
Top cuisine rankings and satisfaction scores.

---

## 3. Pipeline Operations Endpoints

### `POST /api/v1/pipelines/run`
Trigger an asynchronous pipeline execution ticket.

**Request Body:**
```json
{
  "pipelineType": "BATCH_ETL",
  "sourcePath": "data/samples/zomato_restaurants.csv",
  "triggeredBy": "WEB_DASHBOARD"
}
```

**Response (202 Accepted):**
```json
{
  "id": "job-a1b2c3d4",
  "runId": "run-f84e2a19-...",
  "pipelineType": "BATCH_ETL",
  "status": "PENDING",
  "sourcePath": "data/samples/zomato_restaurants.csv",
  "inputRecords": 0,
  "validRecords": 0,
  "quarantinedRecords": 0,
  "duplicateRecords": 0,
  "durationMs": 0,
  "triggeredBy": "WEB_DASHBOARD",
  "startedAt": "2026-10-20T17:00:00Z"
}
```

### `GET /api/v1/pipelines`
Retrieve full pipeline execution history sorted descending by start time.

### `GET /api/v1/pipelines/{id}`
Retrieve single pipeline run ticket with detailed valid and quarantined record counts.

---

## 4. AI & Natural-Language Intelligence Endpoints

### `POST /api/v1/ai/text-to-sql`
Translate natural-language prompt into AST-validated read-only analytical SQL.

**Request Body:**
```json
{
  "question": "Which cuisines have the highest average ratings?"
}
```

**Response (200 OK):**
```json
{
  "question": "Which cuisines have the highest average ratings?",
  "generatedSql": "SELECT cuisines, COUNT(*) AS restaurant_count, ROUND(AVG(aggregate_rating), 2) AS avg_rating FROM restaurants GROUP BY cuisines HAVING COUNT(*) >= 5 ORDER BY avg_rating DESC LIMIT 10",
  "safe": true,
  "executionDurationMs": 42,
  "columnNames": ["cuisines", "restaurant_count", "avg_rating"],
  "rows": [
    { "cuisines": "Continental", "restaurant_count": 172, "avg_rating": 4.45 },
    { "cuisines": "Italian", "restaurant_count": 169, "avg_rating": 4.41 }
  ],
  "rowCount": 2,
  "explanation": "Query executed securely under read-only analytical policy. Retrieved 2 record(s) answering: 'Which cuisines have the highest average ratings?'"
}
```

### `POST /api/v1/ai/rag`
Query customer reviews via semantic search with grounded citations.

**Request Body:**
```json
{
  "query": "What are the common complaints about delivery?",
  "topK": 5
}
```

---

## 5. System Health Telemetry

### `GET /api/v1/system/health`
Retrieve cluster readiness, database connectivity, and JVM memory allocation.
