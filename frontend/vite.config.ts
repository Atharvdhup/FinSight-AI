import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      '/api/v1/portfolios': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/v1/market': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
    },
  },
})
