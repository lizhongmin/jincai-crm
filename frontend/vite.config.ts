import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  test: {
    environment: 'happy-dom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/test/stores/**/*.test.ts', 'src/test/utils/**/*.test.ts'],
    exclude: ['tests/**'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      reportsDirectory: './coverage/unit',
      include: ['src/stores/**/*.ts', 'src/utils/**/*.ts'],
      exclude: ['src/test/**'],
      thresholds: {
        lines: 70,
        functions: 70,
        branches: 70,
        statements: 70
      }
    }
  },
  plugins: [vue()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return;
          }
          if (id.includes('ant-design-vue') || id.includes('@ant-design/icons-vue')) {
            return 'ui-vendor';
          }
          if (id.includes('vue-router') || id.includes('pinia') || id.includes('/vue/')) {
            return 'vue-vendor';
          }
          if (id.includes('axios')) {
            return 'http-vendor';
          }
          return 'vendor';
        }
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
});
