import axios from 'axios';
import type {
  PortfolioSummary,
  PortfolioPerformance,
  PortfolioAllocation,
  TradeRequest,
  TransactionResponse,
  StockDetail,
  HistoricalCandle,
  SimulateTickResponse,
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Interceptor for logging & standard error formatting
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const errorMsg =
      error.response?.data?.detail ||
      error.response?.data?.message ||
      error.message ||
      'An unexpected network error occurred';
    console.error('[API Error]:', errorMsg, error);
    return Promise.reject(new Error(errorMsg));
  }
);

/**
 * Portfolio API calls
 */
export const portfolioApi = {
  getSummary: async (portfolioId: number): Promise<PortfolioSummary> => {
    const res = await apiClient.get<PortfolioSummary>(`/api/v1/portfolios/${portfolioId}`);
    return res.data;
  },

  getPerformance: async (portfolioId: number): Promise<PortfolioPerformance> => {
    const res = await apiClient.get<PortfolioPerformance>(`/api/v1/portfolios/${portfolioId}/performance`);
    return res.data;
  },

  getAllocation: async (portfolioId: number): Promise<PortfolioAllocation> => {
    const res = await apiClient.get<PortfolioAllocation>(`/api/v1/portfolios/${portfolioId}/allocation`);
    return res.data;
  },

  executeTrade: async (
    portfolioId: number,
    trade: TradeRequest,
    idempotencyKey?: string
  ): Promise<TransactionResponse> => {
    const effectiveKey = idempotencyKey || crypto.randomUUID();
    const res = await apiClient.post<TransactionResponse>(
      `/api/v1/portfolios/${portfolioId}/transactions`,
      trade,
      {
        headers: {
          'Idempotency-Key': effectiveKey,
        },
      }
    );
    return res.data;
  },

  createPortfolio: async (
    name: string,
    userId: string = 'user-001',
    initialCash: number = 100000.0
  ): Promise<PortfolioSummary> => {
    const res = await apiClient.post<PortfolioSummary>('/api/v1/portfolios', {
      name,
      userId,
      currency: 'USD',
      initialCash,
    });
    return res.data;
  },
};

/**
 * Market Data API calls
 */
export const marketApi = {
  getAllStocks: async (): Promise<StockDetail[]> => {
    const res = await apiClient.get<StockDetail[]>('/api/v1/market/stocks');
    return res.data;
  },

  getStockDetails: async (symbol: string): Promise<StockDetail> => {
    const res = await apiClient.get<StockDetail>(`/api/v1/market/stocks/${symbol}`);
    return res.data;
  },

  getStockHistory: async (symbol: string, days: number = 90): Promise<HistoricalCandle[]> => {
    const res = await apiClient.get<HistoricalCandle[]>(`/api/v1/market/stocks/${symbol}/history`, {
      params: { days },
    });
    return res.data;
  },

  triggerSimulatorTick: async (symbol?: string, price?: number): Promise<SimulateTickResponse> => {
    if (symbol && price !== undefined) {
      const res = await apiClient.post<SimulateTickResponse>(
        `/api/v1/market/simulator/tick/${symbol}`,
        null,
        { params: { price } }
      );
      return res.data;
    } else if (symbol) {
      const res = await apiClient.post<SimulateTickResponse>(
        `/api/v1/market/simulator/tick/${symbol}`
      );
      return res.data;
    } else {
      const res = await apiClient.post<SimulateTickResponse>('/api/v1/market/simulator/tick');
      return res.data;
    }
  },
};

