import {Mark, mergeAttributes} from '@tiptap/core'
import {Plugin, PluginKey} from '@tiptap/pm/state'

/**
 * 批注点击事件的回调类型
 */
export type AnnotationClickHandler = (annotationId: string, event: MouseEvent) => void

/**
 * Tiptap 自定义批注 Mark 扩展。
 *
 * <p>用于在编辑器中高亮标记被批注的文字区域，
 * 通过 data-annotation-id 属性关联后端批注数据。
 * 内置 ProseMirror 插件处理点击事件，确保点击高亮文字时可靠触发回调。</p>
 */
const AnnotationMark = Mark.create<{
    HTMLAttributes: Record<string, string>;
    onAnnotationClick?: AnnotationClickHandler
}>({
    name: 'annotation',

    addOptions() {
        return {
            HTMLAttributes: {},
            onAnnotationClick: undefined
        }
    },

    addAttributes() {
        return {
            /** 批注 ID，关联后端数据 */
            annotationId: {
                default: null,
                parseHTML: (element: HTMLElement) =>
                    element.getAttribute('data-annotation-id'),
                renderHTML: (attributes: Record<string, string>) => {
                    if (!attributes.annotationId) return {}
                    return {'data-annotation-id': attributes.annotationId}
                }
            },
            /** 批注状态，用于区分高亮颜色 */
            status: {
                default: 'PENDING',
                parseHTML: (element: HTMLElement) =>
                    element.getAttribute('data-annotation-status') || 'PENDING',
                renderHTML: (attributes: Record<string, string>) => {
                    return {'data-annotation-status': attributes.status || 'PENDING'}
                }
            }
        }
    },

    parseHTML() {
        return [{tag: 'span[data-annotation-id]'}]
    },

    renderHTML({HTMLAttributes}) {
        const status = HTMLAttributes['data-annotation-status'] || 'PENDING'
        const cssClass = status === 'RESOLVED'
            ? 'annotation-highlight annotation-resolved'
            : 'annotation-highlight'

        return [
            'span',
            mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, {class: cssClass}),
            0
        ]
    },

    /**
     * 通过 ProseMirror 插件处理批注点击事件（最可靠的方式）
     */
    addProseMirrorPlugins() {
        const options = this.options
        return [
            new Plugin({
                key: new PluginKey('annotationClick'),
                props: {
                    handleClick(_view, _pos, event) {
                        const target = event.target as HTMLElement
                        const highlightEl = target.closest('[data-annotation-id]') as HTMLElement | null
                        if (highlightEl && options.onAnnotationClick) {
                            const annotationIdStr = highlightEl.getAttribute('data-annotation-id')
                            if (annotationIdStr) {
                                options.onAnnotationClick(annotationIdStr, event)
                                return true
                            }
                        }
                        return false
                    }
                }
            })
        ]
    }
})

export default AnnotationMark
