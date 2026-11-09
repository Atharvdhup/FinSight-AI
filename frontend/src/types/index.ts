export type TransactionType = 'BUY' | 'SELL';

export interface Holding {
  id: number;
  portfolioId: number;
  symbol: string;
  quantity: number;
  averageBuyPrice: number;
  currentPrice: number;
  totalInvested: number;
  currentValue: number;
  unrealizedPnL: number;
  unrealizedPnLPercent: number;
  portfolioWeightPercent: number;
  sector: string;
  updatedAt: string;
}

export interface PortfolioSummary {
  id: number;
  userId: string;
  name: string;
  currency: string;
  cashBalance: number;
  totalInvested: number;
  totalCurrentValue: number;
  totalUnrealizedPnL: number;
  totalUnrealizedPnLPercent: number;
  holdings: Holding[];
  createdAt: string;
  updatedAt: string;
}

export interface PortfolioPerformance {
  portfolioId: number;
  portfolioName: string;
  currency: string;
  cashBalance: number;
  totalInvested: number;
  totalCurrentValue: number;
  totalPortfolioValue: number;
  totalUnrealizedPnL: number;
  totalUnrealizedPnLPercent: number;
  dailyChangeAmount: number;
  dailyChangePercent: number;
  asOf: string;
}

export interface SectorAllocation {
  sector: string;
  value: number;
  weightPercent: number;
}

export interface StockAllocation {
  symbol: string;
  value: number;
  weightPercent: number;
}

export interface PortfolioAllocation {
  portfolioId: number;
  totalPortfolioValue: number;
  cashBalance: number;
  cashWeightPercent: number;
  holdingsWeightPercent: number;
  sectorAllocations: SectorAllocation[];
  stockAllocations: StockAllocation[];
  asOf: string;
}

export interface TradeRequest {
  symbol: string;
  type: TransactionType;
  quantity: number;
  price: number;
  fees?: number;
}

export interface TransactionResponse {
  id: number;
  portfolioId: number;
  idempotencyKey: string;
  symbol: string;
  transactionType: TransactionType;
  quantity: number;
  price: number;
  totalAmount: number;
  fees: number;
  executedAt: string;
  status: 'EXECUTED' | 'ALREADY_PROCESSED';
}

export interface StockDetail {
  symbol: string;
  companyName: string;
  sector: string;
  industry: string;
  currency: string;
  currentPrice: number;
  change: number;
  changePercent: number;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  previousClose: number;
  volume: number;
  marketTime: string;
}

export interface HistoricalCandle {
  symbol: string;
  priceDate: string;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  closePrice: number;
  volume: number;
}

export interface UpdatedQuote {
  symbol: string;
  previousPrice: number;
  newPrice: number;
  percentChange: number;
  timestamp: string;
}

export interface SimulateTickResponse {
  simulatedTicks: number;
  updatedQuotes: UpdatedQuote[];
  timestamp: string;
}

