import React from 'react';
import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip } from 'recharts';
import type { PortfolioAllocation } from '../types';

interface AllocationChartProps {
  allocation: PortfolioAllocation | null;
}

const COLORS = [
  '#6366f1', // Indigo
  '#10b981', // Emerald
  '#06b6d4', // Cyan
  '#f59e0b', // Amber
  '#ec4899', // Pink
  '#8b5cf6', // Purple
  '#3b82f6', // Blue
  '#14b8a6', // Teal
];

interface CustomTooltipProps {
  active?: boolean;
  payload?: Array<{
    name: string;
    value: number;
    payload: {
      sector: string;
      value: number;
      weightPercent: number;
    };
  }>;
}

const CustomTooltip = ({ active, payload }: CustomTooltipProps) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className="rounded-lg border border-slate-700 bg-slate-900/95 p-3 shadow-xl backdrop-blur-md">
        <p className="text-xs font-semibold text-white">{data.sector}</p>
        <p className="mt-1 text-sm font-bold text-indigo-400">
          ${Number(data.value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
        </p>
        <p className="text-xs text-slate-400">
          Weight: <span className="text-slate-200 font-medium">{Number(data.weightPercent).toFixed(2)}%</span>
        </p>
      </div>
    );
  }
  return null;
};

export const AllocationChart: React.FC<AllocationChartProps> = ({ allocation }) => {
  if (!allocation || (!allocation.sectorAllocations?.length && allocation.cashBalance <= 0)) {
    return (
      <div className="flex h-72 flex-col items-center justify-center rounded-xl border border-slate-800 bg-slate-900/80 p-6 text-center shadow-lg">
        <p className="text-sm font-semibold text-slate-300">No Allocation Data</p>
        <p className="mt-1 text-xs text-slate-400">Execute trades or fund portfolio to view allocation breakdown.</p>
      </div>
    );
  }

  // Combine sector allocations with Cash allocation for a complete portfolio view
  const chartData = [
    ...(allocation.sectorAllocations || []).map((s) => ({
      sector: s.sector,
      value: Number(s.value),
      weightPercent: Number(s.weightPercent),
    })),
    ...(allocation.cashBalance > 0
      ? [
          {
            sector: 'Cash & Equivalents',
            value: Number(allocation.cashBalance),
            weightPercent: Number(allocation.cashWeightPercent || 0),
          },
        ]
      : []),
  ];

  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900/80 p-5 shadow-lg backdrop-blur-sm">
      <div className="flex items-center justify-between border-b border-slate-800/80 pb-3 mb-4">
        <div>
          <h2 className="text-base font-semibold text-white">Asset & Sector Allocation</h2>
          <p className="text-xs text-slate-400">Diversification across economic sectors & cash</p>
        </div>
        <span className="text-xs font-medium text-slate-400">
          Cash: <span className="text-white font-semibold">{Number(allocation.cashWeightPercent || 0).toFixed(1)}%</span>
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-4 items-center">
        {/* Donut chart */}
        <div className="h-64 md:col-span-7">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={chartData}
                dataKey="value"
                nameKey="sector"
                cx="50%"
                cy="50%"
                innerRadius={65}
                outerRadius={95}
                paddingAngle={3}
                stroke="#0f172a"
                strokeWidth={2}
              >
                {chartData.map((_, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Legend Breakdown */}
        <div className="md:col-span-5 flex flex-col gap-2 max-h-60 overflow-y-auto pr-1">
          {chartData.map((item, index) => {
            const color = COLORS[index % COLORS.length];
            return (
              <div
                key={item.sector}
                className="flex items-center justify-between rounded-lg bg-slate-950/40 px-3 py-2 text-xs border border-slate-800/50"
              >
                <div className="flex items-center gap-2 truncate">
                  <span
                    className="h-2.5 w-2.5 rounded-full flex-shrink-0"
                    style={{ backgroundColor: color }}
                  />
                  <span className="truncate text-slate-300 font-medium">{item.sector}</span>
                </div>
                <div className="flex items-center gap-2 ml-2 flex-shrink-0">
                  <span className="font-semibold text-white">
                    {item.weightPercent.toFixed(1)}%
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

