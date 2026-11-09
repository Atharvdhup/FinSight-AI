import React from 'react';
import { ArrowUpRight, ArrowDownRight, TrendingUp } from 'lucide-react';
import type { Holding } from '../types';

interface HoldingsTableProps {
  holdings: Holding[];
  onTradeClick?: (symbol: string, defaultAction?: 'BUY' | 'SELL') => void;
}

export const HoldingsTable: React.FC<HoldingsTableProps> = ({ holdings, onTradeClick }) => {
  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(val || 0);
  };

  const formatPercent = (val: number) => {
    const formatted = Math.abs(val || 0).toFixed(2);
    return `${val >= 0 ? '+' : '-'}${formatted}%`;
  };

  if (!holdings || holdings.length === 0) {
    return (
      <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-8 text-center">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-slate-800/80 text-slate-400">
          <TrendingUp className="h-6 w-6" />
        </div>
        <h3 className="mt-3 text-sm font-semibold text-white">No Holdings Found</h3>
        <p className="mt-1 text-xs text-slate-400">
          Execute your first trade above to build your investment portfolio.
        </p>
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-xl border border-slate-800 bg-slate-900/80 shadow-lg backdrop-blur-sm">
      <div className="border-b border-slate-800/80 px-6 py-4 flex items-center justify-between">
        <div>
          <h2 className="text-base font-semibold text-white">Portfolio Positions</h2>
          <p className="text-xs text-slate-400">Real-time valuation based on market simulator feed</p>
        </div>
        <span className="rounded-full bg-slate-800 px-3 py-1 text-xs font-medium text-slate-300">
          {holdings.length} {holdings.length === 1 ? 'Position' : 'Positions'}
        </span>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm text-slate-300">
          <thead className="bg-slate-950/60 text-xs uppercase tracking-wider text-slate-400">
            <tr>
              <th scope="col" className="px-6 py-3.5">Asset</th>
              <th scope="col" className="px-6 py-3.5 text-right">Shares</th>
              <th scope="col" className="px-6 py-3.5 text-right">Avg Buy Price</th>
              <th scope="col" className="px-6 py-3.5 text-right">Current Price</th>
              <th scope="col" className="px-6 py-3.5 text-right">Market Value</th>
              <th scope="col" className="px-6 py-3.5 text-right">Unrealized P&L</th>
              <th scope="col" className="px-6 py-3.5 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {holdings.map((holding) => {
              const isProfit = (holding.unrealizedPnL || 0) >= 0;
              return (
                <tr
                  key={holding.id || holding.symbol}
                  className="transition-colors hover:bg-slate-800/40"
                >
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-500/10 font-bold text-indigo-400 border border-indigo-500/20 text-xs">
                        {holding.symbol}
                      </div>
                      <div>
                        <div className="font-semibold text-white">{holding.symbol}</div>
                        <div className="text-xs text-slate-400">
                          {holding.sector || 'Equities'} • {(holding.portfolioWeightPercent || 0).toFixed(1)}% weight
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-right font-medium text-slate-200">
                    {Number(holding.quantity).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 4 })}
                  </td>
                  <td className="px-6 py-4 text-right text-slate-400">
                    {formatCurrency(holding.averageBuyPrice)}
                  </td>
                  <td className="px-6 py-4 text-right font-medium text-slate-100">
                    {formatCurrency(holding.currentPrice)}
                  </td>
                  <td className="px-6 py-4 text-right font-semibold text-white">
                    {formatCurrency(holding.currentValue)}
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="flex flex-col items-end">
                      <span
                        className={`inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-xs font-semibold ${
                          isProfit
                            ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/20'
                            : 'bg-rose-500/15 text-rose-400 border border-rose-500/20'
                        }`}
                      >
                        {isProfit ? (
                          <ArrowUpRight className="h-3 w-3" />
                        ) : (
                          <ArrowDownRight className="h-3 w-3" />
                        )}
                        {formatCurrency(holding.unrealizedPnL)}
                      </span>
                      <span className={`text-[11px] mt-0.5 ${isProfit ? 'text-emerald-500' : 'text-rose-500'}`}>
                        {formatPercent(holding.unrealizedPnLPercent)}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-center">
                    <div className="flex items-center justify-center gap-1.5">
                      <button
                        type="button"
                        onClick={() => onTradeClick && onTradeClick(holding.symbol, 'BUY')}
                        className="rounded-md bg-emerald-600/20 px-2.5 py-1 text-xs font-medium text-emerald-400 border border-emerald-500/30 hover:bg-emerald-600/30 transition-colors"
                      >
                        Buy
                      </button>
                      <button
                        type="button"
                        onClick={() => onTradeClick && onTradeClick(holding.symbol, 'SELL')}
                        className="rounded-md bg-rose-600/20 px-2.5 py-1 text-xs font-medium text-rose-400 border border-rose-500/30 hover:bg-rose-600/30 transition-colors"
                      >
                        Sell
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

