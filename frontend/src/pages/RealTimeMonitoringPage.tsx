import React, { useState, useEffect } from 'react';
import { StatCard } from '../components/StatCard';

interface LiveEvent {
  eventId: string;
  eventType: string;
  restaurantName: string;
  city: string;
  rating?: number;
  orderAmount?: number;
  timestamp: string;
}

export const RealTimeMonitoringPage: React.FC = () => {
  const [events, setEvents] = useState<LiveEvent[]>([]);
  const [isStreaming, setIsStreaming] = useState(true);
  const [throughput, setThroughput] = useState(145);
  const [totalProcessed, setTotalProcessed] = useState(18420);
  const [consumerLag, setConsumerLag] = useState(0);

  // Simulated live Kafka event receiver
  useEffect(() => {
    if (!isStreaming) return;

    const interval = setInterval(() => {
      const cities = ['Bangalore', 'New Delhi', 'Mumbai', 'Pune', 'Hyderabad'];
      const types = ['ORDER_PLACED', 'REVIEW_CREATED', 'RATING_UPDATED', 'TABLE_RESERVED'];
      const restaurants = ['Urban Bistro', 'Spice Kitchen', 'The Royal Grill', 'Copper Chimney', 'Cafe Coffee Day'];

      const newEvent: LiveEvent = {
        eventId: 'evt-' + Math.random().toString(36).substring(2, 9),
        eventType: types[Math.floor(Math.random() * types.length)],
        restaurantName: restaurants[Math.floor(Math.random() * restaurants.length)],
        city: cities[Math.floor(Math.random() * cities.length)],
        rating: +(3.5 + Math.random() * 1.5).toFixed(1),
        orderAmount: Math.floor(250 + Math.random() * 1800),
        timestamp: new Date().toLocaleTimeString()
      };

      setEvents(prev => [newEvent, ...prev.slice(0, 19)]);
      setTotalProcessed(p => p + 1);
      setThroughput(Math.floor(120 + Math.random() * 60));
      setConsumerLag(Math.random() > 0.8 ? Math.floor(Math.random() * 4) : 0);
    }, 1200);

    return () => clearInterval(interval);
  }, [isStreaming]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ margin: '0 0 0.25rem 0', fontSize: '1.75rem', fontWeight: 800, color: '#f8fafc' }}>
            Real-Time Streaming Operations
          </h1>
          <p style={{ margin: 0, color: '#94a3b8', fontSize: '0.875rem' }}>
            Live event processing over Apache Kafka & Spark Structured Streaming with 10-minute event-time watermarking.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button
            className="btn-secondary"
            onClick={() => setIsStreaming(!isStreaming)}
          >
            {isStreaming ? '⏸ Pause Stream' : '▶ Resume Stream'}
          </button>
        </div>
      </div>

      {/* Streaming KPIs */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem' }}>
        <StatCard
          title="Streaming Throughput"
          value={`${throughput} evt/s`}
          subtext="Kafka topic ingestion rate"
          icon="⚡"
          accentColor="#06b6d4"
        />
        <StatCard
          title="Total Events Streamed"
          value={totalProcessed.toLocaleString()}
          subtext="Processed in current session"
          icon="📦"
          accentColor="#10b981"
        />
        <StatCard
          title="Max Consumer Lag"
          value={`${consumerLag} msgs`}
          subtext="Consumer group lag buffer"
          icon="⏱️"
          accentColor={consumerLag > 10 ? '#ef4444' : '#3b82f6'}
        />
        <StatCard
          title="Spark Micro-Batch"
          value="10s Trigger"
          subtext="Tumbling 1-minute window"
          icon="🌊"
          accentColor="#8b5cf6"
        />
      </div>

      {/* Live Stream Event Feed */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{
              width: '10px',
              height: '10px',
              borderRadius: '50%',
              backgroundColor: '#10b981',
              display: 'inline-block'
            }}></span>
            <h3 style={{ margin: 0, fontSize: '1.1rem', fontWeight: 700, color: '#f8fafc' }}>
              Live Kafka Ingestion Feed (`dataflow.restaurant.events`)
            </h3>
          </div>
          <span className="badge badge-info">{isStreaming ? 'Streaming Active' : 'Paused'}</span>
        </div>

        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Event ID</th>
                <th>Type</th>
                <th>Restaurant</th>
                <th>Market</th>
                <th>Order Amount</th>
                <th>Rating</th>
                <th>Event Time</th>
              </tr>
            </thead>
            <tbody>
              {events.map((evt) => (
                <tr key={evt.eventId}>
                  <td style={{ fontFamily: 'monospace', color: '#38bdf8' }}>{evt.eventId}</td>
                  <td>
                    <span className={`badge ${
                      evt.eventType === 'ORDER_PLACED' ? 'badge-success' :
                      evt.eventType === 'REVIEW_CREATED' ? 'badge-info' : 'badge-warning'
                    }`}>
                      {evt.eventType}
                    </span>
                  </td>
                  <td style={{ fontWeight: 600 }}>{evt.restaurantName}</td>
                  <td>{evt.city}</td>
                  <td style={{ fontWeight: 600 }}>₹{evt.orderAmount}</td>
                  <td style={{ color: '#fbbf24' }}>★ {evt.rating}</td>
                  <td style={{ color: '#94a3b8', fontSize: '0.8rem' }}>{evt.timestamp}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
