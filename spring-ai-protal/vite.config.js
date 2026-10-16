import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    headers: {
      'Cross-Origin-Opener-Policy': 'same-origin',
      'Cross-Origin-Embedder-Policy': 'require-corp'
    },
    proxy: {
      // 开发环境下代理 AI 绘图接口，避免 COEP 拦截跨域图片
      '/text-to-image': {
        target: 'https://trae-api-cn.mchost.guru',
        changeOrigin: true,
        followRedirects: true,
        rewrite: (path) => path.replace(/^\/text-to-image/, '/api/ide/v1/text_to_image')
      }
    }
  },
  optimizeDeps: {
    exclude: ['@pdftron/webviewer']
  }
}) 