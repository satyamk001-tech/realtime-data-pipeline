import React, { useState } from 'react';
import { executeTextToSql } from '../services/api';
import { TextToSqlResult } from '../types';

export const TextToSqlPage: React.FC = () => {
  const [question, setQuestion] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<TextToSqlResult | null>(null);

  const sampleQueries = [
    'Which cuisines have the highest average ratings?',
    'How many restaurants are present in each city?',
    'Which locations have the highest average cost?',
    'Show online delivery availability across cities'
  ];

  const handleAsk = async (q: string) => {
    if (!q.trim()) return;
    setLoading(true);
    setQuestion(q);
    try {
      const res = await executeTextToSql(q);
      setResult(res);
    } catch (e: any) {
      setResult({
        question: q,
        generatedSql: '',
        safe: false,
        rejectionReason: e.message || 'Execution error',
        executionDurationMs: 0,
        columnNames: [],
        rows: [],
        rowCount: 0,
        explanation: 'Failed to process request.'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleTestInjection = () => {
    handleAsk('DROP TABLE restaurants; -- malicious payload');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ margin: '0 0 0.25rem 0', fontSize: '1.75rem', fontWeight: 800, color: '#f8fafc' }}>
          Secure Natural-Language Text-to-SQL
        </h1>
        <p style={{ margin: 0, color: '#94a3b8', fontSize: '0.875rem' }}>
          Ask analytical questions in plain English. Queries are parsed into an Abstract Syntax Tree (AST), validated against table whitelists, and executed under a read-only policy.
        </p>
      </div>

      {/* Preset Queries */}
      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
        {sampleQueries.map((q, idx) => (
          <button
            key={idx}
            className="btn-secondary"
            style={{ fontSize: '0.8rem', borderRadius: '9999px', padding: '0.35rem 0.85rem' }}
            onClick={() => handleAsk(q)}
          >
            📊 {q}
          </button>
        ))}
        <button
          className="btn-secondary"
          style={{ fontSize: '0.8rem', borderRadius: '9999px', padding: '0.35rem 0.85rem', borderColor: '#ef4444', color: '#f87171' }}
          onClick={handleTestInjection}
        >
          🛡️ Test Security Guard (Inject DROP TABLE)
        </button>
      </div>

      {/* Input Form */}
      <div className="card" style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
        <input
          type="text"
          placeholder="Ask an analytical question (e.g. Which city has the highest average meal cost?)..."
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleAsk(question)}
          style={{ flex: 1, padding: '0.75rem 1rem', fontSize: '0.95rem' }}
        />
        <button
          className="btn-primary"
          onClick={() => handleAsk(question)}
          disabled={loading || !question.trim()}
        >
          <span>{loading ? 'Validating...' : 'Generate & Run SQL'}</span>
        </button>
      </div>

      {/* Result Display */}
      {result && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* SQL & Security Card */}
          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span className={`badge ${result.safe ? 'badge-success' : 'badge-danger'}`}>
                  {result.safe ? '✓ APPROVED READ-ONLY SQL' : '✕ REJECTED SECURITY POLICY'}
                </span>
                <span style={{ fontSize: '0.8rem', color: '#64748b' }}>
                  Execution: {result.executionDurationMs}ms · AST Verified
                </span>
              </div>
            </div>

            {result.generatedSql && (
              <pre style={{
                margin: 0,
                padding: '1rem',
                backgroundColor: '#0a0e17',
                borderRadius: '0.5rem',
                color: '#38bdf8',
                fontSize: '0.85rem',
                overflowX: 'auto',
                border: '1px solid #1e293b'
              }}>
                {result.generatedSql}
              </pre>
            )}

            {result.rejectionReason && (
              <div style={{
                marginTop: '1rem',
                padding: '0.75rem 1rem',
                backgroundColor: 'rgba(239, 68, 68, 0.15)',
                border: '1px solid #ef4444',
                borderRadius: '0.5rem',
                color: '#f87171',
                fontSize: '0.875rem'
              }}>
                <strong>Rejection Reason:</strong> {result.rejectionReason}
              </div>
            )}

            {result.explanation && (
              <div style={{ marginTop: '0.75rem', fontSize: '0.85rem', color: '#94a3b8' }}>
                💡 <em>{result.explanation}</em>
              </div>
            )}
          </div>

          {/* Results Table */}
          {result.safe && result.rows.length > 0 && (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    {result.columnNames.map(col => (
                      <th key={col}>{col}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {result.rows.map((row, idx) => (
                    <tr key={idx}>
                      {result.columnNames.map(col => (
                        <td key={col}>
                          {row[col] !== null && row[col] !== undefined ? String(row[col]) : '-'}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
