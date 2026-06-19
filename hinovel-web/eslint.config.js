import js from '@eslint/js'
import tseslint from 'typescript-eslint'
import pluginVue from 'eslint-plugin-vue'
import eslintConfigPrettier from 'eslint-config-prettier'

export default [
    js.configs.recommended,
    ...tseslint.configs.recommended,
    ...pluginVue.configs['flat/recommended'],
    eslintConfigPrettier,
    {
        files: ['src/**/*.{ts,vue}'],
        languageOptions: {
            parserOptions: {
                parser: tseslint.parser,
                ecmaVersion: 'latest',
                sourceType: 'module',
            },
            // 浏览器 / Vue 运行时全局变量
            globals: {
                window: 'readonly',
                document: 'readonly',
                console: 'readonly',
                localStorage: 'readonly',
                navigator: 'readonly',
                setTimeout: 'readonly',
                clearTimeout: 'readonly',
                setInterval: 'readonly',
                clearInterval: 'readonly',
                requestAnimationFrame: 'readonly',
                fetch: 'readonly',
                AbortController: 'readonly',
                Blob: 'readonly',
                URL: 'readonly',
                File: 'readonly',
                MouseEvent: 'readonly',
                KeyboardEvent: 'readonly',
                HTMLElement: 'readonly',
                HTMLDivElement: 'readonly',
                HTMLTextAreaElement: 'readonly',
                EventListener: 'readonly',
            },
        },
        rules: {
            // 禁止使用 any 类型（特殊场景需注释说明原因）
            '@typescript-eslint/no-explicit-any': 'warn',
            // 禁止未使用的变量（下划线前缀、catch 参数除外）
            '@typescript-eslint/no-unused-vars': [
                'error',
                { argsIgnorePattern: '^_', varsIgnorePattern: '^_', caughtErrors: 'none' },
            ],
            // 存量代码中存在未使用赋值，暂降级为警告
            'no-useless-assignment': 'warn',
            'no-unassigned-vars': 'warn',
            // Vue 组件名称必须多单词（排除 index.vue 等入口文件）
            'vue/multi-word-component-names': 'off',
            // 禁止使用 var
            'no-var': 'error',
            // 优先使用 const
            'prefer-const': 'error',
        },
    },
    {
        ignores: [
            'dist/**',
            'node_modules/**',
            'auto-imports.d.ts',
            'components.d.ts',
        ],
    },
]
