import {
  Restaurant,
  AnalyticsOverview,
  LocationAnalytics,
  CuisineAnalytics,
  PipelineJob,
  TextToSqlResult,
  RagResult,
  SystemHealth
} from '../types';

const API_BASE = '/api/v1';

export async function fetchOverview(): Promise<AnalyticsOverview> {
  const res = await fetch(`${API_BASE}/analytics/overview`);
  if (!res.ok) throw new Error('Failed to fetch analytics overview');
  return res.json();
}

export async function fetchLocations(): Promise<LocationAnalytics[]> {
  const res = await fetch(`${API_BASE}/analytics/locations`);
  if (!res.ok) throw new Error('Failed to fetch location analytics');
  return res.json();
}

export async function fetchCuisines(): Promise<CuisineAnalytics[]> {
  const res = await fetch(`${API_BASE}/analytics/cuisines`);
  if (!res.ok) throw new Error('Failed to fetch cuisine analytics');
  return res.json();
}

export async function fetchRestaurants(
  page = 0,
  size = 20,
  keyword?: string,
  city?: string
): Promise<{ content: Restaurant[]; totalElements: number; totalPages: number }> {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString()
  });
  if (keyword) params.append('keyword', keyword);
  if (city) params.append('city', city);

  const res = await fetch(`${API_BASE}/restaurants?${params.toString()}`);
  if (!res.ok) throw new Error('Failed to fetch restaurants');
  return res.json();
}

export async function fetchRestaurantById(id: number): Promise<Restaurant> {
  const res = await fetch(`${API_BASE}/restaurants/${id}`);
  if (!res.ok) throw new Error(`Restaurant ${id} not found`);
  return res.json();
}

export async function fetchPipelines(): Promise<PipelineJob[]> {
  const res = await fetch(`${API_BASE}/pipelines`);
  if (!res.ok) throw new Error('Failed to fetch pipeline history');
  return res.json();
}

export async function triggerPipeline(
  pipelineType = 'BATCH_ETL',
  sourcePath = 'data/samples/zomato_restaurants.csv'
): Promise<PipelineJob> {
  const res = await fetch(`${API_BASE}/pipelines/run`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ pipelineType, sourcePath, triggeredBy: 'WEB_DASHBOARD' })
  });
  if (!res.ok && res.status !== 202) throw new Error('Failed to trigger pipeline');
  return res.json();
}

export async function executeTextToSql(question: string): Promise<TextToSqlResult> {
  const res = await fetch(`${API_BASE}/ai/text-to-sql`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question })
  });
  if (!res.ok) throw new Error('Failed to execute Text-to-SQL query');
  return res.json();
}

export async function queryRag(query: string, topK = 5): Promise<RagResult> {
  const res = await fetch(`${API_BASE}/ai/rag`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, topK })
  });
  if (!res.ok) throw new Error('Failed to query RAG assistant');
  return res.json();
}

export async function fetchSystemHealth(): Promise<SystemHealth> {
  const res = await fetch(`${API_BASE}/system/health`);
  if (!res.ok) throw new Error('Failed to fetch system health');
  return res.json();
}
