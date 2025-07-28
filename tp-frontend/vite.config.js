import { defineConfig } from 'vite';

export default defineConfig({
    root: './', // wherever index.html is
    build: {
        outDir: 'dist',
        sourcemap: false
    },
    define: {
        global: 'globalThis',
    },
    resolve: {
        dedupe: ['@kiltprotocol/core', '@kiltprotocol/did'],
        alias: {
            events: 'events',
        },
    },
    optimizeDeps: {
        include: ['events']
    },
    server: {
        host: true
    },
    esbuild: {
        target: 'es2020'
    }
})