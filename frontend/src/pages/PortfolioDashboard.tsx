import React, { useEffect, useState, useCallback } from 'react';
import {
  Wallet,
  DollarSign,
  TrendingUp,
  Activity,
  PlusCircle,
  RefreshCw,
  Zap,
  AlertTriangle,
} from 'lucide-react';
import { MetricCard } from '../components/MetricCard';
import { HoldingsTable } from '../components/HoldingsTable';
import { AllocationChart } from '../components/AllocationChart';
import { TradeModal } from '../components/TradeModal';
import { portfolioApi, marketApi } from '../api/client';
import type {
  PortfolioPerformance,
  PortfolioAllocation,
  PortfolioSummary,
  StockDetail,
  TradeRequest,
  TransactionResponse,
} from '../types';

export const PortfolioDashboard: React.FC = () => {
  const portfolioId = 1;

  const [performance, setPerformance] = useState<PortfolioPerformance | null>(null);
  const [allocation, setAllocation] = useState<PortfolioAllocation | null>(null);
  const [summary, setSummary] = useState<PortfolioSummary | null>(null);
  const [stocks, setStocks] = useState<StockDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [simulatingTick, setSimulatingTick] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Trade Modal State
  const [isTradeModalOpen, setIsTradeModalOpen] = useState(false);
  const [selectedTradeSymbol, setSelectedTradeSymbol] = useState('AAPL');
  const [selectedTradeType, setSelectedTradeType] = useState<'BUY' | 'SELL'>('BUY');

  const loadData = useCallback(async (isInitial = false) => {
    if (isInitial) setLoading(true);
    else setRefreshing(true);
    setError(null);

    try {
      // Parallel API calls using Promise.allSettled for fault tolerance
      const [perfRes, allocRes, sumRes, stocksRes] = await Promise.allSettled([
        portfolioApi.getPerformance(portfolioId),
        portfolioApi.getAllocation(portfolioId),
        portfolioApi.getSummary(portfolioId),
        marketApi.getAllStocks(),
      ]);

      if (perfRes.status === 'fulfilled') setPerformance(perfRes.value);
      if (allocRes.status === 'fulfilled') setAllocation(allocRes.value);
      if (sumRes.status === 'fulfilled') setSummary(sumRes.value);
      if (stocksRes.status === 'fulfilled') setStocks(stocksRes.value);

      // If backend portfolio 1 doesn't exist yet (e.g. fresh DB), create default demo portfolio
      if (perfRes.status === 'rejected' && sumRes.status === 'rejected') {
        try {
          const created = await portfolioApi.createPortfolio('Alpha Growth Fund', 'user-001', 100000.0);
          setSummary(created);
          const newPerf = await portfolioApi.getPerformance(created.id);
          setPerformance(newPerf);
          const newAlloc = await portfolioApi.getAllocation(created.id);
          setAllocation(newAlloc);
        } catch {
          // If creation fails (e.g. backend offline), show clear warning
          setError('Backend services unreachable. Ensure portfolio-service (8081) and market-data-service (8082) are running.');
        }
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load portfolio metrics';
      setError(msg);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [portfolioId]);

  useEffect(() => {
    loadData(true);
  }, [loadData]);

  // Execute trade callback passed to TradeModal
  const handleExecuteTrade = async (
    trade: TradeRequest,
    idempotencyKey: string
  ): Promise<TransactionResponse> => {
    const res = await portfolioApi.executeTrade(portfolioId, trade, idempotencyKey);
    // Instant server-reconciliation: fetch refreshed valuation models
    await loadData(false);
    return res;
  };

  // Trigger simulated market price tick
  const handleSimulateTick = async () => {
    setSimulatingTick(true);
    try {
      await marketApi.triggerSimulatorTick();
      // Price ticks trigger changes in stock quotes; re-calculate portfolio performance
      await loadData(false);
    } catch (err: unknown) {
      console.error('Simulator tick error:', err);
    } finally {
      setSimulatingTick(false);
    }
  };

  const openTradeModal = (symbol = 'AAPL', type: 'BUY' | 'SELL' = 'BUY') => {
    setSelectedTradeSymbol(symbol);
    setSelectedTradeType(type);
    setIsTradeModalOpen(true);
  };

  const formatCurrency = (val?: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(val || 0);
  };

  const formatPercent = (val?: number) => {
    const formatted = Math.abs(val || 0).toFixed(2);
    return `${(val || 0) >= 0 ? '+' : '-'}${formatted}%`;
  };

  const isProfit = (performance?.totalUnrealizedPnL || 0) >= 0;
  const isDailyProfit = (performance?.dailyChangeAmount || 0) >= 0;

  if (loading && !performance && !summary) {
    return (
      <div className="flex h-96 items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="h-10 w-10 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
          <p className="text-xs font-medium text-slate-400">Loading portfolio valuation models...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page Title & Action Bar */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-black tracking-tight text-white sm:text-3xl">
            {performance?.portfolioName || 'Investment Portfolio'}
          </h1>
          <p className="text-xs text-slate-400">
            Real-time event-driven valuation • USD Base Currency • Virtual Sandbox
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <button
            type="button"
            onClick={handleSimulateTick}
            disabled={simulatingTick}
            className="inline-flex items-center gap-1.5 rounded-xl border border-amber-500/30 bg-amber-500/10 px-3.5 py-2 text-xs font-semibold text-amber-300 hover:bg-amber-500/20 transition-all shadow-md active:scale-95 disabled:opacity-50"
            title="Trigger synthetic price changes across all securities"
          >
            <Zap className={`h-4 w-4 ${simulatingTick ? 'animate-bounce text-amber-400' : ''}`} />
            <span>{simulatingTick ? 'Simulating...' : 'Simulate Market Tick'}</span>
          </button>

          <button
            type="button"
            onClick={() => loadData(false)}
            disabled={refreshing}
            className="inline-flex items-center gap-1.5 rounded-xl border border-slate-700 bg-slate-800/80 px-3.5 py-2 text-xs font-semibold text-slate-300 hover:bg-slate-700 hover:text-white transition-all shadow-md active:scale-95 disabled:opacity-50"
          >
            <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>

          <button
            type="button"
            onClick={() => openTradeModal('AAPL', 'BUY')}
            className="inline-flex items-center gap-1.5 rounded-xl bg-indigo-600 px-4 py-2 text-xs font-bold uppercase tracking-wider text-white shadow-lg shadow-indigo-600/30 hover:bg-indigo-500 transition-all active:scale-95"
          >
            <PlusCircle className="h-4 w-4" />
            <span>Trade</span>
          </button>
        </div>
      </div>

      {/* Network or Service Error Notification */}
      {error && (
        <div className="flex items-center justify-between rounded-xl border border-amber-500/30 bg-amber-950/20 p-4 text-xs text-amber-300">
          <div className="flex items-center gap-2">
            <AlertTriangle className="h-4 w-4 flex-shrink-0 text-amber-400" />
            <span>{error}</span>
          </div>
          <button
            onClick={() => loadData(true)}
            className="ml-4 font-semibold underline hover:text-amber-200"
          >
            Retry Connection
          </button>
        </div>
      )}

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <MetricCard
          title="Total Portfolio Value"
          value={formatCurrency(performance?.totalPortfolioValue ?? summary?.totalCurrentValue)}
          icon={Wallet}
          subtitle={`Cash: ${formatCurrency(performance?.cashBalance ?? summary?.cashBalance)}`}
        />

        <MetricCard
          title="Invested Capital"
          value={formatCurrency(performance?.totalInvested ?? summary?.totalInvested)}
          icon={DollarSign}
          subtitle={`${summary?.holdings?.length || 0} active asset positions`}
        />

        <MetricCard
          title="Unrealized P&L"
          value={formatCurrency(performance?.totalUnrealizedPnL ?? summary?.totalUnrealizedPnL)}
          change={formatPercent(performance?.totalUnrealizedPnLPercent ?? summary?.totalUnrealizedPnLPercent)}
          isPositive={isProfit}
          icon={TrendingUp}
          subtitle="Net return on open positions"
        />

        <MetricCard
          title="Daily Return"
          value={formatCurrency(performance?.dailyChangeAmount)}
          change={formatPercent(performance?.dailyChangePercent)}
          isPositive={isDailyProfit}
          icon={Activity}
          subtitle="Session price movement"
        />
      </div>

      {/* Middle Section: Donut Chart & Position Overview */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <div className="lg:col-span-12">
          <AllocationChart allocation={allocation} />
        </div>
      </div>

      {/* Bottom Section: Holdings Data Table */}
      <div>
        <HoldingsTable
          holdings={summary?.holdings || []}
          onTradeClick={(symbol, action) => openTradeModal(symbol, action)}
        />
      </div>

      {/* Buy/Sell Order Modal */}
      <TradeModal
        isOpen={isTradeModalOpen}
        onClose={() => setIsTradeModalOpen(false)}
        onExecuteTrade={handleExecuteTrade}
        availableStocks={stocks}
        currentHoldings={summary?.holdings || []}
        cashBalance={performance?.cashBalance ?? summary?.cashBalance ?? 0}
        initialSymbol={selectedTradeSymbol}
        initialType={selectedTradeType}
      />
    </div>
  );
};
