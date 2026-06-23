import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import markdown from '@vavt/vite-plugin-import-markdown'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue({
      template: {
        compilerOptions: {
          isCustomElement: (tag) => tag === 'md-editor-element'
        }
      }
    }),
    markdown(),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5174,
    allowedHosts: true,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8082',
        changeOrigin: true,
        ws: true,
        rewrite: (path) => {
          if (path.startsWith('/api/auth') || path.startsWith('/api/admin') || path.startsWith('/api/lowcode')) {
            return path.replace(/^\/api/, '')
          }
          return path
        }
      }
    }
  }
})
