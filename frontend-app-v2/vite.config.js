import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3001,
    proxy: {
      '/api/meda': {
        target: 'http://localhost:8085',
        changeOrigin: true,
        secure: false,
      },
      '/api/dette-interieur': {
        target: 'http://localhost:8086',
        changeOrigin: true,
        secure: false,
      },
      '/api/users': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
      },
      '/api/roles': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
