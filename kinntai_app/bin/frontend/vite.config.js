import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: false,
    rollupOptions: {
      output: {
        entryFileNames: 'js/react-main.js',
        chunkFileNames: 'js/react-[name].[hash].js',
        assetFileNames: 'assets/react-[name].[ext]'
      }
    }
  }
})