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
    resolve: {
        dedupe: ['@kiltprotocol/core', '@kiltprotocol/did']
    },
    server: {
        host: true
    },
    esbuild: {
        target: 'es2020'
    },
    define: {
        global: 'globalThis'
    }
})