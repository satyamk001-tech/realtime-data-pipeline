import React from 'react';

interface StatCardProps {
  title: string;
  value: string | number;
  subtext?: string;
  icon?: string;
  trend?: string;
  accentColor?: string;
}

export const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtext,
  icon,
  trend,
  accentColor = '#3b82f6'
}) => {
  return (
    <div className="card" style={{ position: 'relative', overflow: 'hidden' }}>
      <div style={{
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        height: '3px',
        backgroundColor: accentColor
      }}></div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
        <span style={{ fontSize: '0.8rem', fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
          {title}
        </span>
        {icon && <span style={{ fontSize: '1.25rem' }}>{icon}</span>}
      </div>
      <div style={{ fontSize: '1.875rem', fontWeight: 800, color: '#f8fafc', marginBottom: '0.25rem', letterSpacing: '-0.025em' }}>
        {value}
      </div>
      {(subtext || trend) && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem' }}>
          {trend && (
            <span style={{ color: trend.startsWith('+') ? '#34d399' : '#f87171', fontWeight: 600 }}>
              {trend}
            </span>
          )}
          {subtext && <span style={{ color: '#64748b' }}>{subtext}</span>}
        </div>
      )}
    </div>
  );
};
