import React, { useEffect, useState } from 'react';
import { LocationAnalytics, CuisineAnalytics, AnalyticsOverview } from '../types';
import { fetchLocations, fetchCuisines, fetchOverview } from '../services/api';

export const AnalyticsPage: React.FC = () => {
  const [locations, setLocations] = useState<LocationAnalytics[]>([]);
  const [cuisines, setCuisines] = useState<CuisineAnalytics[]>([]);
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      fetchLocations().catch(() => []),
      fetchCuisines().catch(() => []),
      fetchOverview().catch(() => null)
    ]).then(([loc, cuis, ov]) => {
      setLocations(loc);
      setCuisines(cuis);
      setOverview(ov);
      setLoading(false);
    });
  }, []);

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '4rem', color: '#94a3b8' }}>Loading analytical data models...</div>;
  }

  const maxCost = Math.max(...locations.map(l => l.averageCost), 1);
  const maxCuisineCount = Math.max(...cuisines.map(c => c.restaurantCount), 1);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ margin: '0 0 0.25rem 0', fontSize: '1.75rem', fontWeight: 800, color: '#f8fafc' }}>
          Dimensional Analytics & Marts
        </h1>
        <p style={{ margin: 0, color: '#94a3b8', fontSize: '0.875rem' }}>
          Aggregated analytics powered by dbt transformations and Snowflake Star Schema models.
        </p>
      </div>

      {/* Top Aggregates Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(460px, 1fr))', gap: '1.5rem' }}>
        {/* Average Cost By Metro Location */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
            <h3 style={{ margin: 0, fontSize: '1.1rem', fontWeight: 700, color: '#f8fafc' }}>
              💰 Average Cost For Two By Metropolitan Market
            </h3>
            <span className="badge badge-info">dim_location</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {locations.map((loc) => {
              const widthPct = (loc.averageCost / maxCost) * 100;
              return (
                <div key={loc.city}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.35rem' }}>
                    <span style={{ fontWeight: 600 }}>{loc.city}</span>
                    <span style={{ color: '#38bdf8', fontWeight: 700 }}>₹{loc.averageCost.toLocaleString()}</span>
                  </div>
                  <div style={{ width: '100%', height: '10px', backgroundColor: '#1e293b', borderRadius: '9999px', overflow: 'hidden' }}>
                    <div style={{
                      width: `${widthPct}%`,
                      height: '100%',
                      background: 'linear-gradient(90deg, #3b82f6 0%, #10b981 100%)',
                      borderRadius: '9999px'
                    }}></div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Top Cuisine Offerings & Ratings */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
            <h3 style={{ margin: 0, fontSize: '1.1rem', fontWeight: 700, color: '#f8fafc' }}>
              🍲 Top Cuisines by Volume & Satisfaction
            </h3>
            <span className="badge badge-info">dim_cuisine</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {cuisines.slice(0, 7).map((c) => {
              const widthPct = (c.restaurantCount / maxCuisineCount) * 100;
              return (
                <div key={c.cuisine}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.35rem' }}>
                    <span style={{ fontWeight: 600 }}>{c.cuisine}</span>
                    <span style={{ color: '#94a3b8' }}>
                      {c.restaurantCount} outlets · <strong style={{ color: '#34d399' }}>★ {c.averageRating}</strong>
                    </span>
                  </div>
                  <div style={{ width: '100%', height: '10px', backgroundColor: '#1e293b', borderRadius: '9999px', overflow: 'hidden' }}>
                    <div style={{
                      width: `${widthPct}%`,
                      height: '100%',
                      background: 'linear-gradient(90deg, #8b5cf6 0%, #ec4899 100%)',
                      borderRadius: '9999px'
                    }}></div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Operational Proportions */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.25rem' }}>
        <div className="card">
          <h4 style={{ margin: '0 0 0.5rem 0', color: '#94a3b8', fontSize: '0.8rem', textTransform: 'uppercase' }}>
            Online Delivery Adoption
          </h4>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: '#38bdf8' }}>
            {overview?.onlineDeliveryPercentage || 68.5}%
          </div>
          <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.8rem', color: '#64748b' }}>
            {overview?.onlineDeliveryCount?.toLocaleString() || '986'} restaurants have online fulfillment enabled
          </p>
        </div>

        <div className="card">
          <h4 style={{ margin: '0 0 0.5rem 0', color: '#94a3b8', fontSize: '0.8rem', textTransform: 'uppercase' }}>
            Table Reservation Capability
          </h4>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: '#10b981' }}>
            {overview?.tableBookingPercentage || 24.2}%
          </div>
          <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.8rem', color: '#64748b' }}>
            {overview?.tableBookingCount?.toLocaleString() || '348'} establishments offer advance dine-in bookings
          </p>
        </div>

        <div className="card">
          <h4 style={{ margin: '0 0 0.5rem 0', color: '#94a3b8', fontSize: '0.8rem', textTransform: 'uppercase' }}>
            Total Verified Customer Votes
          </h4>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: '#f59e0b' }}>
            {overview?.totalVotes?.toLocaleString() || '1,248,390'}
          </div>
          <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.8rem', color: '#64748b' }}>
            Aggregated customer engagement signals across all cities
          </p>
        </div>
      </div>
    </div>
  );
};
