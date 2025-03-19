import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  base: "/",
  plugins: [react()],
  server: {
    historyApiFallback: true,
    watch: {
      usePolling: true,
    },
    host: true,
    strictPort: true,
  },
  css: {
    postcss: './postcss.config.js'
  }
})
