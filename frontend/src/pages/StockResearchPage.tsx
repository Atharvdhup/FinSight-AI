import React, { useEffect, useState, useCallback } from 'react';
import {
  TrendingUp,
  TrendingDown,
  RefreshCw,
  Zap,
  ArrowRightLeft,
} from 'lucide-react';
import {
  ResponsiveContainer,
  ComposedChart,
  Area,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts';
import { marketApi, portfolioApi } from '../api/client';
import { TradeModal } from '../components/TradeModal';
import type {
  StockDetail,
  HistoricalCandle,
  TradeRequest,
  TransactionResponse,
  PortfolioSummary,
} from '../types';

export const StockResearchPage: React.FC = () => {
  const [stocks, setStocks] = useState<StockDetail[]>([]);
  const [selectedSymbol, setSelectedSymbol] = useState<string>('AAPL');
  const [stockDetail, setStockDetail] = useState<StockDetail | null>(null);
  const [history, setHistory] = useState<HistoricalCandle[]>([]);
  const [days, setDays] = useState<number>(90);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [ticking, setTicking] = useState(false);
  const [customTickPrice, setCustomTickPrice] = useState<string>('');

  // Trade modal state
  const [isTradeModalOpen, setIsTradeModalOpen] = useState(false);
  const [portfolioSummary, setPortfolioSummary] = useState<PortfolioSummary | null>(null);

  // Fetch list of stocks on initial load
  useEffect(() => {
    const fetchStocks = async () => {
      try {
        const res = await marketApi.getAllStocks();
        if (res && res.length > 0) {
          setStocks(res);
          if (!selectedSymbol) setSelectedSymbol(res[0].symbol);
        }
      } catch (err) {
        console.error('Failed to load stock list:', err);
      }
    };
    fetchStocks();
  }, [selectedSymbol]);

  // Fetch portfolio summary for cash & holdings context when opening TradeModal
  const loadPortfolio = useCallback(async () => {
    try {
      const summary = await portfolioApi.getSummary(1);
      setPortfolioSummary(summary);
    } catch (err) {
      console.warn('Could not load portfolio summary:', err);
    }
  }, []);

  useEffect(() => {
    loadPortfolio();
  }, [loadPortfolio]);

  // Fetch stock details and OHLCV history whenever symbol or days change
  const loadStockData = useCallback(async (isInitial = false) => {
    if (isInitial) setLoading(true);
    else setRefreshing(true);

    try {
      const [detailRes, histRes] = await Promise.allSettled([
        marketApi.getStockDetails(selectedSymbol),
        marketApi.getStockHistory(selectedSymbol, days),
      ]);

      if (detailRes.status === 'fulfilled') {
        setStockDetail(detailRes.value);
        setCustomTickPrice(Number(detailRes.value.currentPrice).toFixed(2));
      }
      if (histRes.status === 'fulfilled') {
        // Sort history chronologically (oldest to newest) for charting
        const sorted = [...histRes.value].sort(
          (a, b) => new Date(a.priceDate).getTime() - new Date(b.priceDate).getTime()
        );
        setHistory(sorted);
      }
    } catch (err) {
      console.error('Error fetching stock data:', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [selectedSymbol, days]);

  useEffect(() => {
    loadStockData(true);
  }, [loadStockData]);

  const handleSimulateTick = async (priceOverride?: number) => {
    setTicking(true);
    try {
      await marketApi.triggerSimulatorTick(selectedSymbol, priceOverride);
      await loadStockData(false);
    } catch (err) {
      console.error('Failed to simulate tick:', err);
    } finally {
      setTicking(false);
    }
  };

  const handleExecuteTrade = async (
    trade: TradeRequest,
    idempotencyKey: string
  ): Promise<TransactionResponse> => {
    const res = await portfolioApi.executeTrade(1, trade, idempotencyKey);
    await loadPortfolio();
    return res;
  };

  const formatCurrency = (val?: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(val || 0);
  };

  const isUp = (stockDetail?.change ?? 0) >= 0;

  // Chart data preparation
  const chartData = history.map((candle) => ({
    date: candle.priceDate,
    close: Number(candle.closePrice),
    open: Number(candle.openPrice),
    high: Number(candle.highPrice),
    low: Number(candle.lowPrice),
    volume: Number(candle.volume),
  }));

  const minPrice = chartData.length > 0 ? Math.min(...chartData.map((d) => d.low)) * 0.98 : 0;
  const maxPrice = chartData.length > 0 ? Math.max(...chartData.map((d) => d.high)) * 1.02 : 100;

  if (loading && !stockDetail) {
    return (
      <div className="flex h-96 items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="h-10 w-10 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
          <p className="text-xs font-medium text-slate-400">Loading stock research intelligence...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header & Quick Ticker Selector */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-black tracking-tight text-white sm:text-3xl">
            Stock Research & Intelligence
          </h1>
          <p className="text-xs text-slate-400">
            Historical OHLCV Candle Analysis • Technical Indicators • Synthetic Order Book
          </p>
        </div>

        {/* Action Controls */}
        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => loadStockData(false)}
            disabled={refreshing}
            className="inline-flex items-center gap-1.5 rounded-xl border border-slate-700 bg-slate-800/80 px-3.5 py-2 text-xs font-semibold text-slate-300 hover:bg-slate-700 hover:text-white transition-all shadow-md active:scale-95 disabled:opacity-50"
          >
            <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>

          <button
            type="button"
            onClick={() => setIsTradeModalOpen(true)}
            className="inline-flex items-center gap-1.5 rounded-xl bg-indigo-600 px-4 py-2 text-xs font-bold uppercase tracking-wider text-white shadow-lg shadow-indigo-600/30 hover:bg-indigo-500 transition-all active:scale-95"
          >
            <ArrowRightLeft className="h-4 w-4" />
            <span>Trade {selectedSymbol}</span>
          </button>
        </div>
      </div>

      {/* Ticker Selector Pills Bar */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        {stocks.length > 0 ? (
          stocks.map((s) => (
            <button
              key={s.symbol}
              type="button"
              onClick={() => setSelectedSymbol(s.symbol)}
              className={`flex items-center gap-2 rounded-xl px-3.5 py-2 text-xs font-bold transition-all flex-shrink-0 ${
                selectedSymbol === s.symbol
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-500/25 ring-2 ring-indigo-400/50'
                  : 'bg-slate-900 border border-slate-800 text-slate-400 hover:bg-slate-800 hover:text-slate-200'
              }`}
            >
              <span>{s.symbol}</span>
              <span className={`text-[10px] font-medium ${selectedSymbol === s.symbol ? 'text-indigo-200' : 'text-slate-500'}`}>
                ${Number(s.currentPrice).toFixed(2)}
              </span>
            </button>
          ))
        ) : (
          ['AAPL', 'MSFT', 'GOOGL', 'NVDA', 'AMZN', 'INFY', 'TCS', 'RELIANCE'].map((sym) => (
            <button
              key={sym}
              type="button"
              onClick={() => setSelectedSymbol(sym)}
              className={`rounded-xl px-3.5 py-2 text-xs font-bold transition-all flex-shrink-0 ${
                selectedSymbol === sym
                  ? 'bg-indigo-600 text-white shadow-md'
                  : 'bg-slate-900 border border-slate-800 text-slate-400 hover:bg-slate-800'
              }`}
            >
              {sym}
            </button>
          ))
        )}
      </div>

      {/* Stock Profile & Price Snapshot Banner */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-6 shadow-xl backdrop-blur-sm">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <div className="flex items-center gap-3">
              <span className="rounded-lg bg-indigo-500/20 px-2.5 py-1 text-xs font-extrabold text-indigo-400 border border-indigo-500/30">
                {selectedSymbol}
              </span>
              <h2 className="text-xl font-bold text-white sm:text-2xl">
                {stockDetail?.companyName || selectedSymbol}
              </h2>
            </div>
            <p className="mt-1 text-xs text-slate-400">
              Sector: <span className="text-slate-200 font-medium">{stockDetail?.sector || 'Technology'}</span> • Industry: <span className="text-slate-200 font-medium">{stockDetail?.industry || 'Software'}</span> • Currency: <span className="text-slate-200 font-medium">{stockDetail?.currency || 'USD'}</span>
            </p>
          </div>

          <div className="flex items-baseline gap-3">
            <span className="text-3xl font-extrabold text-white">
              {formatCurrency(stockDetail?.currentPrice)}
            </span>
            <span
              className={`inline-flex items-center gap-1 rounded-lg px-2.5 py-1 text-xs font-bold ${
                isUp
                  ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/20'
                  : 'bg-rose-500/15 text-rose-400 border border-rose-500/20'
              }`}
            >
              {isUp ? <TrendingUp className="h-3.5 w-3.5" /> : <TrendingDown className="h-3.5 w-3.5" />}
              {isUp ? '+' : ''}{Number(stockDetail?.change || 0).toFixed(2)} ({isUp ? '+' : ''}{Number(stockDetail?.changePercent || 0).toFixed(2)}%)
            </span>
          </div>
        </div>

        {/* Valuation Key Metrics Strip */}
        <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6 border-t border-slate-800/80 pt-4 text-xs">
          <div>
            <span className="text-slate-400">Day Open</span>
            <p className="mt-0.5 font-semibold text-white">{formatCurrency(stockDetail?.openPrice)}</p>
          </div>
          <div>
            <span className="text-slate-400">Day High</span>
            <p className="mt-0.5 font-semibold text-emerald-400">{formatCurrency(stockDetail?.highPrice)}</p>
          </div>
          <div>
            <span className="text-slate-400">Day Low</span>
            <p className="mt-0.5 font-semibold text-rose-400">{formatCurrency(stockDetail?.lowPrice)}</p>
          </div>
          <div>
            <span className="text-slate-400">Previous Close</span>
            <p className="mt-0.5 font-semibold text-slate-300">{formatCurrency(stockDetail?.previousClose)}</p>
          </div>
          <div>
            <span className="text-slate-400">Trading Volume</span>
            <p className="mt-0.5 font-semibold text-indigo-300">
              {stockDetail?.volume ? stockDetail.volume.toLocaleString() : 'N/A'}
            </p>
          </div>
          <div>
            <span className="text-slate-400">Last Synced</span>
            <p className="mt-0.5 font-semibold text-slate-300">
              {stockDetail?.marketTime ? new Date(stockDetail.marketTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Live'}
            </p>
          </div>
        </div>
      </div>

      {/* Simulator Tick Control Box */}
      <div className="rounded-xl border border-amber-500/20 bg-amber-950/10 p-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-2">
            <Zap className="h-5 w-5 text-amber-400" />
            <div>
              <h3 className="text-xs font-bold uppercase tracking-wider text-amber-300">
                Market Simulator Tick Tester
              </h3>
              <p className="text-xs text-slate-400">
                Inject custom price movement or simulate brownian random walk for {selectedSymbol}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <input
              type="number"
              step="0.01"
              placeholder="e.g. 195.50"
              value={customTickPrice}
              onChange={(e) => setCustomTickPrice(e.target.value)}
              className="w-28 rounded-lg border border-slate-700 bg-slate-900 px-2.5 py-1.5 text-xs text-white focus:border-amber-500 focus:outline-none"
            />
            <button
              type="button"
              onClick={() => handleSimulateTick(customTickPrice ? parseFloat(customTickPrice) : undefined)}
              disabled={ticking}
              className="rounded-lg bg-amber-600 px-3 py-1.5 text-xs font-bold text-white hover:bg-amber-500 transition-colors disabled:opacity-50 active:scale-95"
            >
              {ticking ? 'Ticking...' : 'Push Tick'}
            </button>
            <button
              type="button"
              onClick={() => handleSimulateTick(undefined)}
              disabled={ticking}
              className="rounded-lg border border-amber-500/40 bg-amber-500/20 px-3 py-1.5 text-xs font-semibold text-amber-300 hover:bg-amber-500/30 transition-colors disabled:opacity-50"
            >
              Random ±3%
            </button>
          </div>
        </div>
      </div>

      {/* Historical Price Chart */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-6 shadow-xl backdrop-blur-sm">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between border-b border-slate-800/80 pb-4 mb-6">
          <div>
            <h3 className="text-base font-bold text-white">OHLCV Candle & Volume Trend</h3>
            <p className="text-xs text-slate-400">
              Daily trading sessions • skipped weekends & holidays • synthetic geometric Brownian motion
            </p>
          </div>

          {/* Timeframe Toggle Buttons */}
          <div className="flex items-center gap-1 rounded-xl bg-slate-950 p-1 border border-slate-800 text-xs">
            <button
              type="button"
              onClick={() => setDays(7)}
              className={`rounded-lg px-3 py-1 font-semibold transition-all ${
                days === 7 ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              7D
            </button>
            <button
              type="button"
              onClick={() => setDays(30)}
              className={`rounded-lg px-3 py-1 font-semibold transition-all ${
                days === 30 ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              30D
            </button>
            <button
              type="button"
              onClick={() => setDays(90)}
              className={`rounded-lg px-3 py-1 font-semibold transition-all ${
                days === 90 ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              90D
            </button>
          </div>
        </div>

        {/* Chart View */}
        <div className="h-80 w-full">
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={chartData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="priceGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.35} />
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
                <XAxis
                  dataKey="date"
                  stroke="#64748b"
                  fontSize={11}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={(val) => {
                    const d = new Date(val);
                    return `${d.getMonth() + 1}/${d.getDate()}`;
                  }}
                />
                <YAxis
                  yAxisId="price"
                  domain={[minPrice, maxPrice]}
                  orientation="right"
                  stroke="#64748b"
                  fontSize={11}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={(val) => `$${Number(val).toFixed(0)}`}
                />
                <YAxis
                  yAxisId="volume"
                  domain={[0, 'dataMax * 3']}
                  orientation="left"
                  hide
                />
                <Tooltip
                  content={({ active, payload, label }) => {
                    if (active && payload && payload.length) {
                      const data = payload[0].payload;
                      return (
                        <div className="rounded-xl border border-slate-700 bg-slate-900/95 p-3.5 shadow-2xl backdrop-blur-md text-xs">
                          <p className="font-bold text-slate-200 border-b border-slate-800 pb-1 mb-2">
                            {label}
                          </p>
                          <div className="grid grid-cols-2 gap-x-4 gap-y-1">
                            <span className="text-slate-400">Open:</span>
                            <span className="font-semibold text-white">${data.open.toFixed(2)}</span>
                            <span className="text-slate-400">High:</span>
                            <span className="font-semibold text-emerald-400">${data.high.toFixed(2)}</span>
                            <span className="text-slate-400">Low:</span>
                            <span className="font-semibold text-rose-400">${data.low.toFixed(2)}</span>
                            <span className="text-slate-400">Close:</span>
                            <span className="font-semibold text-indigo-400">${data.close.toFixed(2)}</span>
                            <span className="text-slate-400">Volume:</span>
                            <span className="font-semibold text-slate-300">{data.volume.toLocaleString()}</span>
                          </div>
                        </div>
                      );
                    }
                    return null;
                  }}
                />
                <Bar
                  yAxisId="volume"
                  dataKey="volume"
                  fill="#334155"
                  opacity={0.4}
                  radius={[2, 2, 0, 0]}
                />
                <Area
                  yAxisId="price"
                  type="monotone"
                  dataKey="close"
                  stroke="#6366f1"
                  strokeWidth={2.5}
                  fillOpacity={1}
                  fill="url(#priceGradient)"
                />
              </ComposedChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex h-full items-center justify-center text-slate-500 text-xs">
              Loading historical candles...
            </div>
          )}
        </div>
      </div>

      {/* Trade Modal for selected stock */}
      <TradeModal
        isOpen={isTradeModalOpen}
        onClose={() => setIsTradeModalOpen(false)}
        onExecuteTrade={handleExecuteTrade}
        availableStocks={stocks}
        currentHoldings={portfolioSummary?.holdings || []}
        cashBalance={portfolioSummary?.cashBalance ?? 0}
        initialSymbol={selectedSymbol}
        initialType="BUY"
      />
    </div>
  );
};
