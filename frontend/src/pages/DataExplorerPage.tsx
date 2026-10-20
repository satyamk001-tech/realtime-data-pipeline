import React, { useEffect, useState } from 'react';
import { Restaurant } from '../types';
import { fetchRestaurants } from '../services/api';

export const DataExplorerPage: React.FC = () => {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [city, setCity] = useState('');
  const [loading, setLoading] = useState(true);
  const [selectedRestaurant, setSelectedRestaurant] = useState<Restaurant | null>(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const data = await fetchRestaurants(page, 15, keyword, city);
      setRestaurants(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch {
      // Fallback empty
      setRestaurants([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [page, city]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    loadData();
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ margin: '0 0 0.25rem 0', fontSize: '1.75rem', fontWeight: 800, color: '#f8fafc' }}>
            Restaurant Data Explorer
          </h1>
          <p style={{ margin: 0, color: '#94a3b8', fontSize: '0.875rem' }}>
            Query, filter, and inspect normalized restaurant dimensions ({totalElements.toLocaleString()} total outlets).
          </p>
        </div>

        {/* Filter controls */}
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <input
            type="text"
            placeholder="Search by name, cuisine, address..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ minWidth: '260px' }}
          />
          <select value={city} onChange={(e) => { setCity(e.target.value); setPage(0); }}>
            <option value="">All Metros</option>
            <option value="Bangalore">Bangalore</option>
            <option value="New Delhi">New Delhi</option>
            <option value="Mumbai">Mumbai</option>
            <option value="Hyderabad">Hyderabad</option>
            <option value="Pune">Pune</option>
            <option value="Chennai">Chennai</option>
            <option value="Kolkata">Kolkata</option>
          </select>
          <button type="submit" className="btn-secondary">Search</button>
        </form>
      </div>

      {/* Main Table */}
      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>City / Locality</th>
              <th>Cuisines</th>
              <th>Cost For Two</th>
              <th>Rating</th>
              <th>Delivery</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={8} style={{ textAlign: 'center', padding: '3rem', color: '#94a3b8' }}>
                  Loading restaurant dimensions...
                </td>
              </tr>
            ) : restaurants.length === 0 ? (
              <tr>
                <td colSpan={8} style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
                  No restaurants match your search query or database is not yet seeded. Trigger a batch pipeline to ingest records.
                </td>
              </tr>
            ) : (
              restaurants.map((r) => (
                <tr key={r.id}>
                  <td style={{ fontFamily: 'monospace', color: '#64748b' }}>{r.id}</td>
                  <td style={{ fontWeight: 600, color: '#f8fafc' }}>{r.name}</td>
                  <td>
                    <div>{r.city}</div>
                    <div style={{ fontSize: '0.75rem', color: '#64748b' }}>{r.locality}</div>
                  </td>
                  <td style={{ maxWidth: '240px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: '#cbd5e1' }}>
                    {r.cuisines}
                  </td>
                  <td style={{ fontWeight: 600 }}>₹{r.averageCostForTwo}</td>
                  <td>
                    <span className={`badge ${
                      r.aggregateRating >= 4.0 ? 'badge-success' :
                      r.aggregateRating >= 3.0 ? 'badge-warning' : 'badge-danger'
                    }`}>
                      ★ {r.aggregateRating.toFixed(1)}
                    </span>
                  </td>
                  <td>
                    {r.hasOnlineDelivery ? (
                      <span className="badge badge-info">Online</span>
                    ) : (
                      <span style={{ color: '#64748b', fontSize: '0.8rem' }}>Dine-in only</span>
                    )}
                  </td>
                  <td>
                    <button
                      className="btn-secondary"
                      style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                      onClick={() => setSelectedRestaurant(r)}
                    >
                      Inspect
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
          Page {page + 1} of {Math.max(totalPages, 1)} ({totalElements.toLocaleString()} total)
        </span>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            className="btn-secondary"
            disabled={page === 0}
            onClick={() => setPage(p => Math.max(0, p - 1))}
          >
            Previous
          </button>
          <button
            className="btn-secondary"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage(p => p + 1)}
          >
            Next
          </button>
        </div>
      </div>

      {/* Detail Modal */}
      {selectedRestaurant && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '1rem'
        }}>
          <div className="card" style={{ maxWidth: '600px', width: '100%', backgroundColor: '#0f172a' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h2 style={{ margin: 0, fontSize: '1.25rem', color: '#f8fafc' }}>{selectedRestaurant.name}</h2>
              <button
                onClick={() => setSelectedRestaurant(null)}
                style={{ background: 'none', border: 'none', color: '#94a3b8', fontSize: '1.25rem', cursor: 'pointer' }}
              >
                ✕
              </button>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.875rem' }}>
              <div><strong>ID:</strong> {selectedRestaurant.id}</div>
              <div><strong>City:</strong> {selectedRestaurant.city} ({selectedRestaurant.locality})</div>
              <div><strong>Address:</strong> {selectedRestaurant.address}</div>
              <div><strong>Cuisines:</strong> {selectedRestaurant.cuisines}</div>
              <div><strong>Average Cost For Two:</strong> ₹{selectedRestaurant.averageCostForTwo}</div>
              <div><strong>Aggregate Rating:</strong> ★ {selectedRestaurant.aggregateRating} ({selectedRestaurant.ratingText})</div>
              <div><strong>Total Customer Votes:</strong> {selectedRestaurant.votes?.toLocaleString()}</div>
              <div><strong>Online Delivery:</strong> {selectedRestaurant.hasOnlineDelivery ? 'Yes' : 'No'}</div>
              <div><strong>Table Reservation:</strong> {selectedRestaurant.hasTableBooking ? 'Yes' : 'No'}</div>
              <div><strong>Pipeline Run ID:</strong> <code style={{ color: '#38bdf8' }}>{selectedRestaurant.pipelineRunId || 'N/A'}</code></div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
