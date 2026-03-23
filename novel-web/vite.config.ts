import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      resolvers: [ElementPlusResolver()],
      dts: 'src/auto-imports.d.ts'
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    host: true,
    proxy: {
      // MinIO 文件服务代理
      '/minio': {
        target: 'http://127.0.0.1:9000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/minio/, '')
      },
      '/api/common-server': {
        target: 'http://localhost:5220',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/common-server/, '')
      },
      '/api/novel-server': {
        target: 'http://localhost:5250',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/novel-server/, '')
      },
      '/api/author-server': {
        target: 'http://localhost:5200',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/author-server/, '')
      },
      '/api/visitor-server': {
        target: 'http://localhost:5210',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/visitor-server/, '')
      },
      '/api/manager-server': {
        target: 'http://localhost:5280',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/manager-server/, '')
      },
      '/api/comment-server': {
        target: 'http://localhost:5260',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/comment-server/, '')
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'echarts': ['echarts', 'vue-echarts'],
          'vendor': ['vue', 'vue-router', 'pinia']
        }
      }
    }
  }
})