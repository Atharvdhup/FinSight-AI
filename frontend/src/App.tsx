import React from 'react';
import { BrowserRouter, Routes, Route, NavLink, Navigate } from 'react-router-dom';
import { LayoutDashboard, LineChart, Cpu, Radio } from 'lucide-react';
import { PortfolioDashboard } from './pages/PortfolioDashboard';
import { StockResearchPage } from './pages/StockResearchPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col selection:bg-indigo-500 selection:text-white">
        {/* Top Navigation Bar */}
        <header className="sticky top-0 z-40 border-b border-slate-800 bg-slate-950/85 backdrop-blur-md">
          <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
            {/* Logo and Brand */}
            <div className="flex items-center gap-6">
              <NavLink to="/" className="flex items-center gap-2.5 group">
                <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-500 text-white shadow-lg shadow-indigo-500/25 group-hover:scale-105 transition-transform">
                  <Cpu className="h-5 w-5" />
                </div>
                <div>
                  <div className="flex items-center gap-1.5">
                    <span className="font-black text-lg tracking-tight text-white">FinSight</span>
                    <span className="font-light text-lg text-indigo-400">AI</span>
                    <span className="ml-1 rounded-md bg-indigo-500/10 px-1.5 py-0.5 text-[10px] font-bold text-indigo-400 border border-indigo-500/20">
                      v1.0
                    </span>
                  </div>
                  <p className="text-[10px] text-slate-400 leading-none">Event-Driven Intelligence</p>
                </div>
              </NavLink>

              {/* Navigation Links */}
              <nav className="hidden md:flex items-center gap-1">
                <NavLink
                  to="/"
                  end
                  className={({ isActive }) =>
                    `flex items-center gap-2 rounded-lg px-3.5 py-2 text-xs font-semibold transition-all ${
                      isActive
                        ? 'bg-indigo-600/15 text-indigo-400 border border-indigo-500/30'
                        : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                    }`
                  }
                >
                  <LayoutDashboard className="h-4 w-4" />
                  <span>Portfolio Dashboard</span>
                </NavLink>

                <NavLink
                  to="/research"
                  className={({ isActive }) =>
                    `flex items-center gap-2 rounded-lg px-3.5 py-2 text-xs font-semibold transition-all ${
                      isActive
                        ? 'bg-indigo-600/15 text-indigo-400 border border-indigo-500/30'
                        : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                    }`
                  }
                >
                  <LineChart className="h-4 w-4" />
                  <span>Stock Research</span>
                </NavLink>
              </nav>
            </div>

            {/* System Status Indicator */}
            <div className="flex items-center gap-3">
              <div className="hidden sm:flex items-center gap-2 rounded-full bg-slate-900 border border-slate-800 px-3 py-1 text-xs">
                <Radio className="h-3.5 w-3.5 text-emerald-400 animate-pulse" />
                <span className="text-slate-400">Stream:</span>
                <span className="font-semibold text-emerald-400">Kafka Active</span>
              </div>
            </div>
          </div>

          {/* Mobile Navigation bar */}
          <div className="flex md:hidden border-t border-slate-800/80 px-4 py-2 gap-2 bg-slate-900/50">
            <NavLink
              to="/"
              end
              className={({ isActive }) =>
                `flex-1 text-center py-1.5 rounded-lg text-xs font-semibold ${
                  isActive ? 'bg-indigo-600 text-white' : 'text-slate-400'
                }`
              }
            >
              Dashboard
            </NavLink>
            <NavLink
              to="/research"
              className={({ isActive }) =>
                `flex-1 text-center py-1.5 rounded-lg text-xs font-semibold ${
                  isActive ? 'bg-indigo-600 text-white' : 'text-slate-400'
                }`
              }
            >
              Research
            </NavLink>
          </div>
        </header>

        {/* Main Body */}
        <main className="flex-1 mx-auto w-full max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
          <Routes>
            <Route path="/" element={<PortfolioDashboard />} />
            <Route path="/research" element={<StockResearchPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>

        {/* Footer */}
        <footer className="border-t border-slate-800/80 bg-slate-950 py-6 text-center text-xs text-slate-500">
          <div className="mx-auto max-w-7xl px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
            <span>FinSight AI • Production Event-Driven Investment Platform</span>
            <span>Spring Boot 3.3 • Java 21 • Apache Kafka • React 18 • TypeScript</span>
          </div>
        </footer>
      </div>
    </BrowserRouter>
  );
};

export default App;
