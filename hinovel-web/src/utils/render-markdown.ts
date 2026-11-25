import DOMPurify from 'dompurify'
import {marked} from 'marked'

/** 将小说大纲、手记和问答中的 Markdown 安全地转换为 HTML。 */
export function renderMarkdown(content: string): string {
    if (!content) return ''

    try {
        const html = marked.parse(content, {
            async: false,
            breaks: true,
            gfm: true,
        })
        if (typeof html === 'string') {
            return DOMPurify.sanitize(html, {USE_PROFILES: {html: true}})
        }
    } catch {
        // Markdown 解析或净化失败时显示原文。
    }

    const fallback = document.createElement('span')
    fallback.textContent = content
    return fallback.innerHTML.replace(/\r?\n/g, '<br>')
}
