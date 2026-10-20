import React, { useState } from 'react';
import { queryRag } from '../services/api';
import { RagResult } from '../types';

export const AiReviewAssistantPage: React.FC = () => {
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<RagResult | null>(null);

  const sampleQueries = [
    'What are the most frequent customer complaints about delivery?',
    'What do customers praise about biryani and North Indian restaurants?',
    'Are there issues with table reservations in high-end diners?',
    'Summarize sentiment on food packaging and temperature'
  ];

  const handleAsk = async (text: string) => {
    if (!text.trim()) return;
    setLoading(true);
    setQuery(text);
    try {
      const res = await queryRag(text, 5);
      setResult(res);
    } catch {
      setResult({
        query: text,
        answer: 'Failed to retrieve review context. Ensure the backend RAG service is running.',
        sourceReviews: [],
        searchDurationMs: 0
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ margin: '0 0 0.25rem 0', fontSize: '1.75rem', fontWeight: 800, color: '#f8fafc' }}>
          AI Customer Review Intelligence (RAG)
        </h1>
        <p style={{ margin: 0, color: '#94a3b8', fontSize: '0.875rem' }}>
          Semantic vector retrieval over customer reviews using PostgreSQL + pgvector, grounded context construction, and source citations.
        </p>
      </div>

      {/* Suggested Questions */}
      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
        {sampleQueries.map((q, idx) => (
          <button
            key={idx}
            className="btn-secondary"
            style={{ fontSize: '0.8rem', borderRadius: '9999px', padding: '0.35rem 0.85rem' }}
            onClick={() => handleAsk(q)}
          >
            💡 {q}
          </button>
        ))}
      </div>

      {/* Input Box */}
      <div className="card" style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
        <input
          type="text"
          placeholder="Ask any natural-language question about customer reviews..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleAsk(query)}
          style={{ flex: 1, padding: '0.75rem 1rem', fontSize: '0.95rem' }}
        />
        <button
          className="btn-primary"
          onClick={() => handleAsk(query)}
          disabled={loading || !query.trim()}
        >
          <span>{loading ? 'Synthesizing...' : 'Ask Assistant'}</span>
        </button>
      </div>

      {/* Answer & Citations */}
      {result && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="card" style={{ borderLeft: '4px solid #10b981' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ fontSize: '1.25rem' }}>🤖</span>
                <h3 style={{ margin: 0, fontSize: '1.1rem', fontWeight: 700, color: '#f8fafc' }}>
                  Grounded AI Synthesis
                </h3>
              </div>
              <span style={{ fontSize: '0.75rem', color: '#64748b' }}>
                Retrieved in {result.searchDurationMs}ms · {result.sourceReviews.length} citations
              </span>
            </div>
            <div style={{ color: '#e2e8f0', lineHeight: 1.6, whiteSpace: 'pre-line', fontSize: '0.95rem' }}>
              {result.answer}
            </div>
          </div>

          {/* Citations Card Grid */}
          <div>
            <h3 style={{ margin: '0 0 1rem 0', fontSize: '1.1rem', fontWeight: 700, color: '#f8fafc' }}>
              📚 Retrieved Source Citations (pgvector Cosine Matches)
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1rem' }}>
              {result.sourceReviews.map((rev) => (
                <div key={rev.reviewId} className="card" style={{ fontSize: '0.85rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                    <strong style={{ color: '#38bdf8' }}>{rev.restaurantName}</strong>
                    <span className="badge badge-info">{rev.city}</span>
                  </div>
                  <div style={{ color: '#cbd5e1', fontStyle: 'italic', marginBottom: '0.75rem' }}>
                    "{rev.reviewText}"
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: '#64748b', fontSize: '0.75rem' }}>
                    <span>Rating: ★ {rev.rating}</span>
                    <span>Similarity: {(rev.similarityScore * 100).toFixed(1)}%</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
