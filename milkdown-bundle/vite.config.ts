import { defineConfig } from 'vite'
import { resolve } from 'path'

export default defineConfig({
  build: {
    lib: {
      entry: resolve(__dirname, 'src/milkdown-init.ts'),
      name: 'MilkdownEditor',
      formats: ['iife'],
      fileName: () => 'milkdown-editor.js',
    },
    outDir: '../markdown-editor/src/androidMain/assets/offline',
    rollupOptions: {
      output: {
        inlineDynamicImports: true,
        assetFileNames: (assetInfo) => {
          if (assetInfo.name?.endsWith('.css')) return 'milkdown-editor.css'
          return assetInfo.name ?? 'asset'
        },
      },
    },
    minify: 'esbuild',
    cssCodeSplit: false,
  },
})
