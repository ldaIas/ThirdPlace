import { defineConfig } from 'vite';

export default defineConfig({
    root: './', // wherever index.html is
    build: {
        outDir: 'dist'
    },
    resolve: {
        dedupe: ['@kiltprotocol/core', '@kiltprotocol/did']
    }
})