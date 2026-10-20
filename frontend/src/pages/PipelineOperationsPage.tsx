import React, { useEffect, useState } from 'react';
import { PipelineJob } from '../types';
import { fetchPipelines, triggerPipeline } from '../services/api';

export const PipelineOperationsPage: React.FC = () => {
  const [pipelines, setPipelines] = useState<PipelineJob[]>([]);
  const [loading, setLoading] = useState(true);
  const [triggering, setTriggering] = useState(false);
  const [selectedJob, setSelectedJob] = useState<PipelineJob | null>(null);

  const loadData = async () => {
    try {
      const data = await fetchPipelines();
      setPipelines(data);
    } catch {
      setPipelines([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    const timer = setInterval(loadData, 5000);
    return () => clearInterval(timer);
  }, []);

  const handleRun = async (pipelineType: string) => {
    setTriggering(true);
    try {
      await triggerPipeline(pipelineType, 'data/samples/zomato_restaurants.csv');
      await loadData();
    } finally {
      setTriggering(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ margin: '0 0 0.25rem 0', fontSize: '1.75rem', fontWeight: 800, color: '#f8fafc' }}>
            Pipeline Operations & Job History
          </h1>
          <p style={{ margin: 0, color: '#94a3b8', fontSize: '0.875rem' }}>
            Monitor asynchronous Spark ETL execution runs, inspect data quality quarantine gates, and trace job attempts.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button
            className="btn-primary"
            onClick={() => handleRun('BATCH_ETL')}
            disabled={triggering}
          >
            <span>{triggering ? 'Submitting...' : '⚡ Run Batch ETL (Spark)'}</span>
          </button>
        </div>
      </div>

      {/* Main Execution History Table */}
      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>Job ID / Run ID</th>
              <th>Pipeline Type</th>
              <th>Status</th>
              <th>Input</th>
              <th>Valid (Lake)</th>
              <th>Quarantine</th>
              <th>Duplicates</th>
              <th>Duration</th>
              <th>Started At</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={10} style={{ textAlign: 'center', padding: '3rem', color: '#94a3b8' }}>
                  Loading pipeline jobs...
                </td>
              </tr>
            ) : pipelines.length === 0 ? (
              <tr>
                <td colSpan={10} style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
                  No pipeline execution history recorded. Click "Run Batch ETL (Spark)" to initiate the first job.
                </td>
              </tr>
            ) : (
              pipelines.map((job) => (
                <tr key={job.id}>
                  <td>
                    <div style={{ fontFamily: 'monospace', fontWeight: 600, color: '#38bdf8' }}>{job.id}</div>
                    <div style={{ fontSize: '0.7rem', color: '#64748b', fontFamily: 'monospace' }}>
                      {job.runId.substring(0, 18)}...
                    </div>
                  </td>
                  <td>
                    <span className="badge badge-info">{job.pipelineType}</span>
                  </td>
                  <td>
                    <span className={`badge ${
                      job.status === 'SUCCEEDED' ? 'badge-success' :
                      job.status === 'RUNNING' ? 'badge-warning' :
                      job.status === 'FAILED' ? 'badge-danger' : 'badge-info'
                    }`}>
                      {job.status}
                    </span>
                  </td>
                  <td>{job.inputRecords?.toLocaleString() || 0}</td>
                  <td style={{ color: '#34d399', fontWeight: 600 }}>
                    {job.validRecords?.toLocaleString() || 0}
                  </td>
                  <td style={{ color: job.quarantinedRecords > 0 ? '#f87171' : '#94a3b8', fontWeight: 600 }}>
                    {job.quarantinedRecords?.toLocaleString() || 0}
                  </td>
                  <td style={{ color: job.duplicateRecords > 0 ? '#fbbf24' : '#94a3b8' }}>
                    {job.duplicateRecords?.toLocaleString() || 0}
                  </td>
                  <td>{job.durationMs ? `${job.durationMs}ms` : '-'}</td>
                  <td style={{ fontSize: '0.8rem', color: '#94a3b8' }}>
                    {new Date(job.startedAt).toLocaleTimeString()}
                  </td>
                  <td>
                    <button
                      className="btn-secondary"
                      style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                      onClick={() => setSelectedJob(job)}
                    >
                      Audit
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Audit Detail Modal */}
      {selectedJob && (
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
          <div className="card" style={{ maxWidth: '650px', width: '100%', backgroundColor: '#0f172a' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h2 style={{ margin: 0, fontSize: '1.25rem', color: '#f8fafc' }}>
                Pipeline Run Audit: {selectedJob.id}
              </h2>
              <button
                onClick={() => setSelectedJob(null)}
                style={{ background: 'none', border: 'none', color: '#94a3b8', fontSize: '1.25rem', cursor: 'pointer' }}
              >
                ✕
              </button>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.875rem' }}>
              <div><strong>Status:</strong> <span className={`badge ${selectedJob.status === 'SUCCEEDED' ? 'badge-success' : 'badge-danger'}`}>{selectedJob.status}</span></div>
              <div><strong>Run Ticket UUID:</strong> <code>{selectedJob.runId}</code></div>
              <div><strong>Source File:</strong> <code>{selectedJob.sourcePath}</code></div>
              <div><strong>Target S3 / Lake Path:</strong> <code>{selectedJob.targetPath || 'target/lake/processed/restaurants'}</code></div>
              <div><strong>Total Input Records:</strong> {selectedJob.inputRecords?.toLocaleString()}</div>
              <div><strong>Clean Valid Records:</strong> {selectedJob.validRecords?.toLocaleString()}</div>
              <div><strong>Quarantined Defective Records:</strong> {selectedJob.quarantinedRecords?.toLocaleString()}</div>
              <div><strong>Duplicate Business Keys Detected:</strong> {selectedJob.duplicateRecords?.toLocaleString()}</div>
              <div><strong>Execution Wall Clock:</strong> {selectedJob.durationMs} milliseconds</div>
              <div><strong>Triggered By:</strong> {selectedJob.triggeredBy}</div>
              {selectedJob.errorMessage && (
                <div style={{ padding: '0.75rem', backgroundColor: 'rgba(239, 68, 68, 0.15)', border: '1px solid #ef4444', borderRadius: '0.5rem', color: '#f87171' }}>
                  <strong>Error Trace:</strong> {selectedJob.errorMessage}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
