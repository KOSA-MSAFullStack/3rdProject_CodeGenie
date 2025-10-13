import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import monacoEditorPlugin from 'vite-plugin-monaco-editor'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    monacoEditorPlugin.default({
      languageWorkers: ['editorWorkerService', 'css', 'html', 'json', 'typescript'],
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'), // src를 @로 인식시킴
    },
  },
  server: {
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
