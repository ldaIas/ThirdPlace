import { defineConfig } from 'vite';
import { nodePolyfills } from 'vite-plugin-node-polyfills';

export default defineConfig({
    plugins: [
        nodePolyfills({
            include: ['events']
        })
    ],
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