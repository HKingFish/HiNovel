import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'
import {resolve} from 'path'

export default defineConfig(({mode}) => ({
    plugins: [
        vue(),
        AutoImport({
            resolvers: [ElementPlusResolver({importStyle: mode !== 'test' && 'css'})],
            imports: ['vue', 'vue-router', 'pinia'],
        }),
        Components({
            resolvers: [ElementPlusResolver({importStyle: mode !== 'test' && 'css'})],
        }),
    ],
    resolve: {
        alias: {
            '@': resolve(__dirname, 'src'),
        },
    },
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            },
        },
    },
    test: {
        environment: 'jsdom',
        globals: true,
    },
}))
