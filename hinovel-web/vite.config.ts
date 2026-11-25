import {defineConfig, loadEnv} from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'
import {resolve} from 'path'

export default defineConfig(({mode}) => {
    const env = loadEnv(mode, resolve(__dirname, '..'), ['WEB_PORT', 'SERVER_PORT'])
    const backend = `http://localhost:${env.SERVER_PORT || '8080'}`
    return {
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
            port: Number(env.WEB_PORT || '3000'),
            strictPort: true,
            proxy: {
                '/api': {
                    target: backend,
                    changeOrigin: true,
                },
                '/files': {
                    target: backend,
                    changeOrigin: true,
                },
            },
        },
        test: {
            environment: 'jsdom',
            globals: true,
        },
    }
})
