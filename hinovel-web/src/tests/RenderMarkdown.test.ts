import {marked} from 'marked'
import {afterEach, expect, it, vi} from 'vitest'
import {renderMarkdown} from '@/utils/render-markdown'

afterEach(() => {
    vi.restoreAllMocks()
})

it('保留 Markdown 格式并移除不允许的 HTML 元素及链接协议', () => {
    const html = renderMarkdown(
        '**章节**\n[资料](https://example.com) [其他](unsupported:reference)\n\n' +
        '<script type="application/json">{"key": "value"}</script>'
    )
    const output = document.createElement('div')
    output.innerHTML = html

    expect(output.querySelector('strong')?.textContent).toBe('章节')
    expect(output.querySelectorAll('a')[0].getAttribute('href')).toBe('https://example.com')
    expect(output.querySelectorAll('a')[1].hasAttribute('href')).toBe(false)
    expect(output.querySelector('script')).toBeNull()
})

it('解析出错时作为纯文本保留原始内容和换行', () => {
    vi.spyOn(marked, 'parse').mockImplementation(() => {
        throw new Error('Markdown 解析失败')
    })

    expect(renderMarkdown('<aside>原文</aside>\n下一行')).toBe('&lt;aside&gt;原文&lt;/aside&gt;<br>下一行')
})
