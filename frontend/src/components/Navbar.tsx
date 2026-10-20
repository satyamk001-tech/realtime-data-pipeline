import React from 'react';

interface NavbarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  systemStatus: string;
}

export const Navbar: React.FC<NavbarProps> = ({ activeTab, setActiveTab, systemStatus }) => {
  const navItems = [
    { id: 'overview', label: 'Overview', icon: '📊' },
    { id: 'explorer', label: 'Data Explorer', icon: '🔍' },
    { id: 'analytics', label: 'Analytics', icon: '📈' },
    { id: 'pipelines', label: 'Pipelines', icon: '⚡' },
    { id: 'streaming', label: 'Real-Time', icon: '📡' },
    { id: 'rag', label: 'AI Reviews', icon: '🤖' },
    { id: 'text-to-sql', label: 'Text-to-SQL', icon: '💬' },
    { id: 'health', label: 'Health', icon: '🛡️' }
  ];

  return (
    <header style={{
      borderBottom: '1px solid #1f2d44',
      backgroundColor: '#0d131f',
      padding: '0.75rem 2rem',
      position: 'sticky',
      top: 0,
      zIndex: 50
    }}>
      <div style={{
        maxWidth: '1440px',
        margin: '0 auto',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '1rem'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer' }}
             onClick={() => setActiveTab('overview')}>
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: '8px',
            background: 'linear-gradient(135deg, #4f46e5 0%, #06b6d4 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 'bold',
            fontSize: '1.25rem',
            color: 'white'
          }}>
            ⚡
          </div>
          <div>
            <div style={{ fontWeight: '800', fontSize: '1.125rem', letterSpacing: '-0.025em', color: '#f8fafc' }}>
              DataFlow
            </div>
            <div style={{ fontSize: '0.7rem', color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Distributed Analytics Platform
            </div>
          </div>
        </div>

        <nav style={{ display: 'flex', gap: '0.35rem', flexWrap: 'wrap' }}>
          {navItems.map(item => (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.4rem',
                padding: '0.5rem 0.85rem',
                borderRadius: '0.5rem',
                border: 'none',
                background: activeTab === item.id ? '#1e293b' : 'transparent',
                color: activeTab === item.id ? '#38bdf8' : '#94a3b8',
                fontWeight: activeTab === item.id ? 600 : 500,
                fontSize: '0.85rem',
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              <span>{item.icon}</span>
              <span>{item.label}</span>
            </button>
          ))}
        </nav>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.35rem 0.75rem',
            borderRadius: '9999px',
            backgroundColor: systemStatus === 'UP' ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
            border: `1px solid ${systemStatus === 'UP' ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}`,
            fontSize: '0.75rem',
            color: systemStatus === 'UP' ? '#34d399' : '#f87171',
            fontWeight: 600
          }}>
            <span style={{
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              backgroundColor: systemStatus === 'UP' ? '#10b981' : '#ef4444',
              display: 'inline-block'
            }}></span>
            <span>{systemStatus === 'UP' ? 'CLUSTER ONLINE' : 'DEGRADED'}</span>
          </div>
        </div>
      </div>
    </header>
  );
};
