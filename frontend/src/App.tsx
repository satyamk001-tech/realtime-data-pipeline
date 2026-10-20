import React, { useState } from 'react';
import { Navbar } from './components/Navbar';
import { OverviewPage } from './pages/OverviewPage';
import { DataExplorerPage } from './pages/DataExplorerPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { PipelineOperationsPage } from './pages/PipelineOperationsPage';
import { RealTimeMonitoringPage } from './pages/RealTimeMonitoringPage';
import { AiReviewAssistantPage } from './pages/AiReviewAssistantPage';
import { TextToSqlPage } from './pages/TextToSqlPage';
import { SystemHealthPage } from './pages/SystemHealthPage';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState('overview');

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: '#0a0e17' }}>
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} systemStatus="UP" />

      <main style={{ maxWidth: '1440px', width: '100%', margin: '0 auto', padding: '2rem', flex: 1, boxSizing: 'border-box' }}>
        {activeTab === 'overview' && <OverviewPage />}
        {activeTab === 'explorer' && <DataExplorerPage />}
        {activeTab === 'analytics' && <AnalyticsPage />}
        {activeTab === 'pipelines' && <PipelineOperationsPage />}
        {activeTab === 'streaming' && <RealTimeMonitoringPage />}
        {activeTab === 'rag' && <AiReviewAssistantPage />}
        {activeTab === 'text-to-sql' && <TextToSqlPage />}
        {activeTab === 'health' && <SystemHealthPage />}
      </main>

      <footer style={{
        borderTop: '1px solid #1f2d44',
        padding: '1.25rem 2rem',
        textAlign: 'center',
        fontSize: '0.8rem',
        color: '#64748b',
        backgroundColor: '#0d131f'
      }}>
        <div>DataFlow Distributed Data Pipeline & Analytics Platform · Production-Inspired Architecture</div>
        <div style={{ marginTop: '0.25rem' }}>Java 21 · Apache Spark · Spring Boot · Apache Kafka · Snowflake · dbt · React + TypeScript</div>
      </footer>
    </div>
  );
};

export default App;
