import React, { useEffect, useRef, useState } from 'react';
import { X, ArrowRightLeft, AlertCircle, CheckCircle2 } from 'lucide-react';
import type { Holding, StockDetail, TradeRequest, TransactionResponse } from '../types';

interface TradeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onExecuteTrade: (trade: TradeRequest, idempotencyKey: string) => Promise<TransactionResponse>;
  availableStocks: StockDetail[];
  currentHoldings: Holding[];
  cashBalance: number;
  initialSymbol?: string;
  initialType?: 'BUY' | 'SELL';
}

export const TradeModal: React.FC<TradeModalProps> = ({
  isOpen,
  onClose,
  onExecuteTrade,
  availableStocks,
  currentHoldings,
  cashBalance,
  initialSymbol = 'AAPL',
  initialType = 'BUY',
}) => {
  const dialogRef = useRef<HTMLDialogElement>(null);

  const [symbol, setSymbol] = useState(initialSymbol);
  const [tradeType, setTradeType] = useState<'BUY' | 'SELL'>(initialType);
  const [quantity, setQuantity] = useState<string>('10');
  const [price, setPrice] = useState<string>('150.00');
  const [fees, setFees] = useState<string>('0.00');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Sync dialog visibility using standard HTMLDialogElement modal API
  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;

    if (isOpen) {
      if (!dialog.open) {
        dialog.showModal();
      }
      // Reset form state with passed props
      setSymbol(initialSymbol);
      setTradeType(initialType);
      setError(null);
      setSuccess(null);

      // Pre-fill price from available stocks
      const stock = availableStocks.find((s) => s.symbol === initialSymbol);
      if (stock && stock.currentPrice) {
        setPrice(stock.currentPrice.toString());
      }
    } else {
      if (dialog.open) {
        dialog.close();
      }
    }
  }, [isOpen, initialSymbol, initialType, availableStocks]);

  // Update price when selected symbol changes
  const handleSymbolChange = (newSymbol: string) => {
    setSymbol(newSymbol);
    const stock = availableStocks.find((s) => s.symbol === newSymbol);
    if (stock && stock.currentPrice) {
      setPrice(stock.currentPrice.toString());
    }
  };

  // Find user's current holding for this symbol (if any)
  const currentHolding = currentHoldings.find((h) => h.symbol === symbol);
  const sharesOwned = currentHolding ? Number(currentHolding.quantity) : 0;

  const numQuantity = parseFloat(quantity) || 0;
  const numPrice = parseFloat(price) || 0;
  const numFees = parseFloat(fees) || 0;
  const totalAmount = numQuantity * numPrice + (tradeType === 'BUY' ? numFees : -numFees);

  // Validation
  const validateOrder = (): string | null => {
    if (!symbol) return 'Please select a ticker symbol.';
    if (numQuantity <= 0) return 'Quantity must be strictly greater than 0.';
    if (numPrice <= 0) return 'Execution price must be greater than 0.';
    if (numFees < 0) return 'Fees cannot be negative.';

    if (tradeType === 'BUY' && totalAmount > cashBalance) {
      return `Insufficient cash. Total cost ($${totalAmount.toFixed(2)}) exceeds cash balance ($${cashBalance.toFixed(2)}).`;
    }

    if (tradeType === 'SELL' && numQuantity > sharesOwned) {
      return `Insufficient shares. You own ${sharesOwned} shares of ${symbol}, cannot sell ${numQuantity}.`;
    }

    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const validationError = validateOrder();
    if (validationError) {
      setError(validationError);
      return;
    }

    setError(null);
    setIsSubmitting(true);

    // Generate unique idempotency key for network resilience
    const idempotencyKey = crypto.randomUUID();

    try {
      const res = await onExecuteTrade(
        {
          symbol,
          type: tradeType,
          quantity: numQuantity,
          price: numPrice,
          fees: numFees,
        },
        idempotencyKey
      );

      setSuccess(`Order ${res.status === 'ALREADY_PROCESSED' ? '(Replayed) ' : ''}Executed! ID: ${res.id}`);
      setTimeout(() => {
        setIsSubmitting(false);
        onClose();
      }, 1200);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Trade execution failed';
      setError(msg);
      setIsSubmitting(false);
    }
  };

  return (
    <dialog
      ref={dialogRef}
      aria-labelledby="trade-modal-title"
      onClose={onClose}
      className="m-auto rounded-2xl border border-slate-700 bg-slate-900 p-0 text-slate-100 shadow-2xl backdrop:bg-black/75 backdrop:backdrop-blur-sm w-full max-w-lg"
    >
      <div className="p-6">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-800 pb-4">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">
              <ArrowRightLeft className="h-5 w-5" />
            </div>
            <div>
              <h2 id="trade-modal-title" className="text-lg font-bold text-white">
                Execute Order
              </h2>
              <p className="text-xs text-slate-400">Idempotent trade routing with instant valuation</p>
            </div>
          </div>
          <button
            type="button"
            aria-label="Close modal"
            onClick={onClose}
            className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-800 hover:text-white transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Order Type Toggle (BUY / SELL) */}
        <div className="mt-5 grid grid-cols-2 gap-2 rounded-xl bg-slate-950 p-1 border border-slate-800">
          <button
            type="button"
            onClick={() => setTradeType('BUY')}
            className={`rounded-lg py-2 text-xs font-bold uppercase tracking-wider transition-all ${
              tradeType === 'BUY'
                ? 'bg-emerald-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Buy
          </button>
          <button
            type="button"
            onClick={() => setTradeType('SELL')}
            className={`rounded-lg py-2 text-xs font-bold uppercase tracking-wider transition-all ${
              tradeType === 'SELL'
                ? 'bg-rose-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Sell
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="mt-5 space-y-4">
          {/* Ticker Symbol Select */}
          <div>
            <label className="block text-xs font-medium text-slate-300">Ticker Symbol</label>
            <select
              value={symbol}
              onChange={(e) => handleSymbolChange(e.target.value)}
              className="mt-1 block w-full rounded-lg border border-slate-700 bg-slate-800/80 px-3.5 py-2.5 text-sm text-white focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            >
              {availableStocks.length > 0 ? (
                availableStocks.map((stock) => (
                  <option key={stock.symbol} value={stock.symbol}>
                    {stock.symbol} — {stock.companyName} (${Number(stock.currentPrice).toFixed(2)})
                  </option>
                ))
              ) : (
                <>
                  <option value="AAPL">AAPL — Apple Inc.</option>
                  <option value="MSFT">MSFT — Microsoft Corp.</option>
                  <option value="GOOGL">GOOGL — Alphabet Inc.</option>
                  <option value="NVDA">NVDA — NVIDIA Corp.</option>
                  <option value="AMZN">AMZN — Amazon.com Inc.</option>
                  <option value="INFY">INFY — Infosys Ltd.</option>
                  <option value="TCS">TCS — Tata Consultancy Services</option>
                  <option value="RELIANCE">RELIANCE — Reliance Industries</option>
                </>
              )}
            </select>
          </div>

          {/* Holdings & Buying Power Context Bar */}
          <div className="flex items-center justify-between rounded-lg bg-slate-950/60 px-3.5 py-2 border border-slate-800/80 text-xs">
            <span className="text-slate-400">
              Shares Owned:{' '}
              <span className="font-semibold text-slate-200">
                {sharesOwned.toLocaleString()}
              </span>
            </span>
            <span className="text-slate-400">
              Cash Power:{' '}
              <span className="font-semibold text-emerald-400">
                ${cashBalance.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
            </span>
          </div>

          {/* Quantity & Price Grid */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-slate-300">Shares Quantity</label>
              <input
                type="number"
                step="any"
                min="0.0001"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                required
                className="mt-1 block w-full rounded-lg border border-slate-700 bg-slate-800/80 px-3.5 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-slate-300">Execution Price ($)</label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                required
                className="mt-1 block w-full rounded-lg border border-slate-700 bg-slate-800/80 px-3.5 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>
          </div>

          {/* Fees Input */}
          <div>
            <label className="block text-xs font-medium text-slate-300">Brokerage / Trading Fees ($)</label>
            <input
              type="number"
              step="0.01"
              min="0"
              value={fees}
              onChange={(e) => setFees(e.target.value)}
              className="mt-1 block w-full rounded-lg border border-slate-700 bg-slate-800/80 px-3.5 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </div>

          {/* Order Summary Box */}
          <div className="rounded-xl border border-indigo-500/20 bg-indigo-950/20 p-4">
            <div className="flex items-center justify-between text-xs text-slate-400">
              <span>Gross Order Value</span>
              <span className="text-slate-200">${(numQuantity * numPrice).toFixed(2)}</span>
            </div>
            <div className="mt-1 flex items-center justify-between text-xs text-slate-400">
              <span>Estimated Fees</span>
              <span className="text-slate-200">${numFees.toFixed(2)}</span>
            </div>
            <div className="mt-2.5 flex items-center justify-between border-t border-indigo-500/20 pt-2 text-sm font-semibold">
              <span className="text-indigo-300">Net Estimated Total</span>
              <span className="text-lg font-bold text-white">
                ${Math.max(0, totalAmount).toFixed(2)}
              </span>
            </div>
          </div>

          {/* Validation Error Message */}
          {error && (
            <div className="flex items-center gap-2 rounded-lg bg-rose-500/10 p-3 text-xs text-rose-400 border border-rose-500/20">
              <AlertCircle className="h-4 w-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Success Message */}
          {success && (
            <div className="flex items-center gap-2 rounded-lg bg-emerald-500/10 p-3 text-xs text-emerald-400 border border-emerald-500/20">
              <CheckCircle2 className="h-4 w-4 flex-shrink-0" />
              <span>{success}</span>
            </div>
          )}

          {/* Action Buttons */}
          <div className="mt-6 flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="rounded-lg border border-slate-700 px-4 py-2 text-xs font-semibold text-slate-300 hover:bg-slate-800 transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className={`rounded-lg px-5 py-2 text-xs font-bold uppercase tracking-wider text-white shadow-lg transition-all disabled:opacity-50 ${
                tradeType === 'BUY'
                  ? 'bg-emerald-600 hover:bg-emerald-500 shadow-emerald-900/30'
                  : 'bg-rose-600 hover:bg-rose-500 shadow-rose-900/30'
              }`}
            >
              {isSubmitting ? 'Routing Trade...' : `Confirm ${tradeType}`}
            </button>
          </div>
        </form>
      </div>
    </dialog>
  );
};

