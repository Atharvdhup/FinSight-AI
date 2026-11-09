import React from 'react';
import type { LucideIcon } from 'lucide-react';

interface MetricCardProps {
  title: string;
  value: string;
  change?: string;
  isPositive?: boolean | null;
  icon: LucideIcon;
  subtitle?: string;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  change,
  isPositive,
  icon: Icon,
  subtitle,
}) => {
  return (
    <div className="relative overflow-hidden rounded-xl bg-slate-900/80 p-5 border border-slate-800 shadow-lg backdrop-blur-sm transition-all hover:border-slate-700">
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
          {title}
        </span>
        <div className="rounded-lg bg-slate-800/80 p-2 text-indigo-400">
          <Icon className="h-5 w-5" />
        </div>
      </div>

      <div className="mt-3 flex items-baseline gap-2">
        <span className="text-2xl font-bold tracking-tight text-white">{value}</span>
        {change && (
          <span
            className={`inline-flex items-center text-xs font-medium px-2 py-0.5 rounded-full ${
              isPositive === true
                ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                : isPositive === false
                ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                : 'bg-slate-700/30 text-slate-400 border border-slate-700'
            }`}
          >
            {change}
          </span>
        )}
      </div>

      {subtitle && <p className="mt-2 text-xs text-slate-400">{subtitle}</p>}
    </div>
  );
};

