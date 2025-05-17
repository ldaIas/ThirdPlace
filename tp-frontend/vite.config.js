import { defineConfig } from 'vite';

export default defineConfig({
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
    }
})