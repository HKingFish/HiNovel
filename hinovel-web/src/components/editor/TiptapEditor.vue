<template>
  <div class="tiptap-editor-wrapper" :class="{ 'is-focused': isFocused }">
    <editor-content :editor="editor" class="tiptap-editor-content"
                    :style="{ '--editor-bg-color': props.backgroundColor, '--editor-caret-color': caretColor }"/>

    <!-- 批注浮窗 -->
    <div
        v-if="showAnnotationPopup"
        ref="popupRef"
        class="annotation-popup"
        :style="annotationPopupStyle"
    >
      <!-- 拖拽调整大小的手柄 -->
      <div class="popup-resize-handle" @mousedown="startResize"></div>

      <!-- 新建批注模式 -->
      <template v-if="annotationMode === 'create'">
        <div class="popup-header" @mousedown="startDrag">
          <span class="popup-title">添加批注</span>
          <div class="popup-header-actions">
            <button
                class="popup-pin-btn"
                :class="{ 'is-pinned': isPopupPinned }"
                @click.stop="togglePin"
                title="置顶（置顶后点击其他地方不消失）"
            >📌
            </button>
            <button class="popup-close" @click.stop="closeAnnotationPopup">×</button>
          </div>
        </div>
        <textarea
            ref="annotationInputRef"
            v-model="annotationInputText"
            class="annotation-input"
            placeholder="输入你的批注..."
            rows="3"
            @keydown.enter.ctrl="submitAnnotation"
        ></textarea>
        <div class="popup-actions">
          <button class="popup-btn popup-btn-cancel" @click="closeAnnotationPopup">取消</button>
          <button
              class="popup-btn popup-btn-submit"
              :disabled="!annotationInputText.trim()"
              @click="submitAnnotation"
          >
            添加
          </button>
        </div>
      </template>

      <!-- 查看已有批注模式 -->
      <template v-else-if="annotationMode === 'view'">
        <div class="popup-header" @mousedown="startDrag">
          <span class="popup-title">批注详情</span>
          <div class="popup-header-actions">
            <button
                class="popup-pin-btn"
                :class="{ 'is-pinned': isPopupPinned }"
                @click.stop="togglePin"
                title="置顶（置顶后点击其他地方不消失）"
            >📌
            </button>
            <button class="popup-close" @click.stop="closeAnnotationPopup">×</button>
          </div>
        </div>
        <div class="popup-view-content">
          <!-- 批注人和时间 -->
          <div class="annotation-meta">
                        <span class="annotation-author">
                            <span class="meta-icon">👤</span>
                            {{ viewingAnnotation?.annotationType === 'SELF' ? '作者' : '审阅者' }}
                        </span>
            <span class="annotation-time">
                            {{ formatAnnotationTime(viewingAnnotation?.createTime) }}
                        </span>
          </div>
          <!-- 被批注的原文 -->
          <div class="annotation-original-text">
            "{{ viewingAnnotation?.originalText }}"
          </div>
          <!-- 批注内容 -->
          <div class="annotation-text">
            {{ viewingAnnotation?.annotationContent }}
          </div>

          <!-- AI 改写区域 -->
          <div class="ai-rewrite-section">
            <div class="ai-rewrite-header">
                            <span class="ai-rewrite-title">
                                <span class="meta-icon">✨</span>
                                AI 改写
                            </span>
              <!-- 引用批注：将批注内容填入改写提示词 -->
              <button
                  v-if="!isAiRewriting"
                  class="quote-annotation-btn"
                  @click="quoteAnnotationAsPrompt"
                  title="将批注内容作为改写提示词"
              >
                📋 引用批注
              </button>
            </div>
            <!-- 改写提示词输入框 -->
            <textarea
                v-model="rewritePromptText"
                class="rewrite-prompt-input"
                placeholder="输入改写要求，如：语气更紧张、增加细节描写..."
                rows="2"
                :disabled="isAiRewriting"
            ></textarea>
            <!-- AI 改写结果展示 -->
            <div v-if="aiRewriteResult || isAiRewriting" class="ai-rewrite-result">
              <div class="rewrite-content" :class="{ 'is-streaming': isAiRewriting }">
                {{ aiRewriteResult || '正在改写中...' }}
                <span v-if="isAiRewriting" class="typing-cursor">|</span>
              </div>
            </div>
            <!-- 改写操作按钮 -->
            <div class="ai-rewrite-actions">
              <button
                  v-if="!isAiRewriting"
                  class="popup-btn popup-btn-ai-sm"
                  :disabled="!rewritePromptText.trim()"
                  @click="handleAiRewrite"
              >
                ✨ 改写
              </button>
              <button
                  v-if="isAiRewriting"
                  class="popup-btn popup-btn-stop"
                  @click="stopAiRewrite"
              >
                ⏹ 停止
              </button>
              <template v-if="aiRewriteResult && !isAiRewriting">
                <button class="popup-btn popup-btn-accept" @click="handleAcceptRewrite">
                  ✅ 采纳
                </button>
                <button class="popup-btn popup-btn-retry" @click="handleAiRewrite">
                  🔄 重试
                </button>
              </template>
            </div>
          </div>
        </div>
        <div class="popup-actions">
          <button class="popup-btn popup-btn-delete" @click="handleDeleteAnnotation">删除</button>
          <button class="popup-btn popup-btn-cancel" @click="closeAnnotationPopup">关闭</button>
        </div>
      </template>
    </div>

    <!-- 选中文字后的快捷操作气泡 -->
    <div
        v-if="showSelectionBubble"
        class="selection-bubble"
        :style="selectionBubbleStyle"
    >
      <button class="bubble-btn" @click="openCreateAnnotation">
        <span class="bubble-icon">📝</span>
        批注
      </button>
      <div class="bubble-divider"></div>
      <button class="bubble-btn" @click="openQuickRewrite">
        <span class="bubble-icon">✨</span>
        AI 改写
      </button>
    </div>

    <!-- 快捷 AI 改写浮窗 -->
    <div
        v-if="showQuickRewritePopup"
        ref="quickRewriteRef"
        class="annotation-popup quick-rewrite-popup"
        :style="quickRewritePopupStyle"
    >
      <div class="popup-resize-handle" @mousedown="startResizeQuickRewrite"></div>
      <div class="popup-header" @mousedown="startDragQuickRewrite">
        <span class="popup-title">AI 改写</span>
        <div class="popup-header-actions">
          <button class="popup-close" @click.stop="closeQuickRewrite">×</button>
        </div>
      </div>
      <div class="popup-view-content">
        <div class="annotation-original-text">
          "{{ quickRewriteOriginalText }}"
        </div>
        <textarea
            ref="quickRewriteInputRef"
            v-model="quickRewritePrompt"
            class="rewrite-prompt-input"
            placeholder="输入改写要求，如：语气更紧张、增加细节描写..."
            rows="2"
            :disabled="isQuickRewriting"
        ></textarea>
        <div v-if="quickRewriteResult || isQuickRewriting" class="ai-rewrite-result">
          <div class="rewrite-content" :class="{ 'is-streaming': isQuickRewriting }">
            {{ quickRewriteResult || '正在改写中...' }}
            <span v-if="isQuickRewriting" class="typing-cursor">|</span>
          </div>
        </div>
        <div class="ai-rewrite-actions">
          <button
              v-if="!isQuickRewriting"
              class="popup-btn popup-btn-ai-sm"
              :disabled="!quickRewritePrompt.trim()"
              @click="handleQuickRewrite"
          >
            ✨ 改写
          </button>
          <button
              v-if="isQuickRewriting"
              class="popup-btn popup-btn-stop"
              @click="stopQuickRewrite"
          >
            ⏹ 停止
          </button>
          <template v-if="quickRewriteResult && !isQuickRewriting">
            <button class="popup-btn popup-btn-accept" @click="handleAcceptQuickRewrite">
              ✅ 采纳
            </button>
            <button class="popup-btn popup-btn-retry" @click="handleQuickRewrite">
              🔄 重试
            </button>
          </template>
        </div>
      </div>
      <div class="popup-actions">
        <button class="popup-btn popup-btn-cancel" @click="closeQuickRewrite">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue'
import {EditorContent, useEditor} from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import CharacterCount from '@tiptap/extension-character-count'
import AnnotationMark from './annotation-mark'
import type {ChapterAnnotation} from '@/api/annotation'

/**
 * 组件属性定义
 */
interface TiptapEditorProps {
  /** 编辑器内容（纯文本，双向绑定） */
  modelValue: string
  /** 占位提示文字 */
  placeholder?: string
  /** 是否只读模式 */
  readonly?: boolean
  /** 当前章节的批注列表 */
  annotations?: ChapterAnnotation[]
  /** 编辑器背景色 */
  backgroundColor?: string
}

const props = withDefaults(defineProps<TiptapEditorProps>(), {
  placeholder: '开始你的创作之旅...',
  readonly: false,
  annotations: () => [],
  backgroundColor: '#fefefe'
})

/**
 * 计算背景色的亮度，返回合适的 caret 颜色。
 * 亮度 > 0.5 返回深色，否则返回浅色。
 */
const caretColor = computed(() => {
  const hex = props.backgroundColor.replace('#', '')
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  // 计算亮度 (0-1)
  const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
  return luminance > 0.6 ? '#1a73e8' : '#ffffff'
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'blur': []
  'stats-change': [stats: { wordCount: number; lineCount: number; paragraphCount: number }]
  'create-annotation': [data: {
    startOffset: number
    endOffset: number
    originalText: string
    annotationContent: string
  }]
  'delete-annotation': [annotationId: string]
  /** AI 改写事件：传入批注信息和整章内容，由父组件调用流式接口 */
  'ai-rewrite-annotation': [data: {
    annotationId: string
    originalText: string
    annotationContent: string
    fullChapterContent: string
  }, callbacks: {
    onToken: (token: string) => void
    onComplete: (fullText: string) => void
    onError: (error: Error) => void
  }]
  /** 采纳 AI 改写结果 */
  'accept-rewrite': [data: {
    annotationId: string
    originalText: string
    rewriteResult: string
  }]
  /** 查看批注事件（用于标记已读） */
  'view-annotation': [annotationId: string]
  /** 快捷 AI 改写事件 */
  'quick-rewrite': [data: {
    originalText: string
    rewritePrompt: string
    fullChapterContent: string
  }, callbacks: {
    onToken: (token: string) => void
    onComplete: (fullText: string) => void
    onError: (error: Error) => void
  }]
  /** 快捷改写采纳事件 */
  'accept-quick-rewrite': [data: {
    originalText: string
    rewriteResult: string
  }]
}>()

// 编辑器聚焦状态
const isFocused = ref(false)
let isUpdatingFromProp = false

// 批注浮窗状态
const showAnnotationPopup = ref(false)
const annotationMode = ref<'create' | 'view'>('create')
const annotationPopupStyle = ref<Record<string, string>>({})
const annotationInputText = ref('')
const annotationInputRef = ref<HTMLTextAreaElement | null>(null)
const viewingAnnotation = ref<ChapterAnnotation | null>(null)
const popupRef = ref<HTMLDivElement | null>(null)

// 选中文字气泡
const showSelectionBubble = ref(false)
const selectionBubbleStyle = ref<Record<string, string>>({})
const selectedRange = ref<{ from: number; to: number; text: string } | null>(null)

// AI 改写状态
const isAiRewriting = ref(false)
const aiRewriteResult = ref('')
const rewritePromptText = ref('')
let aiRewriteAbortController: AbortController | null = null

// 弹窗置顶状态（置顶后点击编辑器其他位置不关闭弹窗）
const isPopupPinned = ref(false)

// 弹窗拖拽移动
let isDragging = false
let dragStartX = 0
let dragStartY = 0
let dragStartLeft = 0
let dragStartTop = 0

// 快捷 AI 改写状态
const showQuickRewritePopup = ref(false)
const quickRewritePopupStyle = ref<Record<string, string>>({})
const quickRewriteOriginalText = ref('')
const quickRewritePrompt = ref('')
const quickRewriteResult = ref('')
const isQuickRewriting = ref(false)
const quickRewriteInputRef = ref<HTMLTextAreaElement | null>(null)
const quickRewriteRef = ref<HTMLDivElement | null>(null)
let _quickRewriteSelectedRange: { from: number; to: number; text: string } | null = null

// 弹窗拖拽调整大小
let isResizing = false
let resizeStartX = 0
let resizeStartY = 0
let resizeStartWidth = 0
let resizeStartHeight = 0

/**
 * 切换弹窗置顶状态
 */
const togglePin = () => {
  isPopupPinned.value = !isPopupPinned.value
}

/**
 * 开始拖拽移动弹窗
 */
const startDrag = (event: MouseEvent) => {
  // 如果点击的是按钮区域，不触发拖拽
  const target = event.target as HTMLElement
  if (target.closest('.popup-header-actions') || target.closest('.popup-pin-btn') || target.closest('.popup-close')) {
    return
  }
  if (!popupRef.value) return
  isDragging = true
  dragStartX = event.clientX
  dragStartY = event.clientY
  const rect = popupRef.value.getBoundingClientRect()
  const parentRect = popupRef.value.offsetParent?.getBoundingClientRect() || {left: 0, top: 0}
  dragStartLeft = rect.left - parentRect.left
  dragStartTop = rect.top - parentRect.top
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  event.preventDefault()
}

const onDrag = (event: MouseEvent) => {
  if (!isDragging || !popupRef.value) return
  const deltaX = event.clientX - dragStartX
  const deltaY = event.clientY - dragStartY
  popupRef.value.style.left = `${dragStartLeft + deltaX}px`
  popupRef.value.style.top = `${dragStartTop + deltaY}px`
}

const stopDrag = () => {
  if (!isDragging) return
  isDragging = false
  // 将拖拽后的位置同步回响应式样式，避免 Vue 重新渲染时跳回初始位置
  if (popupRef.value) {
    annotationPopupStyle.value = {
      ...annotationPopupStyle.value,
      top: popupRef.value.style.top,
      left: popupRef.value.style.left
    }
  }
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

/**
 * 格式化批注时间
 */
const formatAnnotationTime = (timeStr?: string): string => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

const textToHtml = (text: string): string => {
  if (!text) return '<p></p>'
  return text.split('\n').map(line => `<p>${line}</p>`).join('')
}

const htmlToText = (html: string): string => {
  if (!html) return ''
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = html
  const paragraphs = tempDiv.querySelectorAll('p')
  if (paragraphs.length === 0) return tempDiv.textContent || ''
  return Array.from(paragraphs).map(p => p.textContent || '').join('\n')
}

const calculateStats = (text: string) => {
  const content = text || ''
  const wordCount = content.replace(/\s/g, '').length
  const lines = content.split('\n')
  return {
    wordCount,
    lineCount: lines.length,
    paragraphCount: lines.filter(line => line.trim().length > 0).length
  }
}

const calculateTextOffset = (docPosition: number): number => {
  if (!editor.value) return 0
  return editor.value.state.doc.textBetween(0, docPosition, '\n').length
}

// 初始化编辑器
const editor = useEditor({
  content: textToHtml(props.modelValue),
  editable: !props.readonly,
  extensions: [
    StarterKit.configure({
      heading: false, bold: false, italic: false, strike: false,
      code: false, codeBlock: false, blockquote: false,
      bulletList: false, orderedList: false, listItem: false, horizontalRule: false
    }),
    Placeholder.configure({placeholder: props.placeholder}),
    CharacterCount,
    AnnotationMark.configure({
      onAnnotationClick: (annotationId: string, event: MouseEvent) => {
        if (isPopupPinned.value) return
        const annotation = props.annotations.find(a => String(a.id) === annotationId)
        if (!annotation) return
        viewingAnnotation.value = annotation
        annotationMode.value = 'view'
        aiRewriteResult.value = annotation.aiRewriteResult || ''
        rewritePromptText.value = ''
        isAiRewriting.value = false
        // 用点击位置计算弹窗坐标
        const editorEl = document.querySelector('.tiptap-editor-wrapper')
        if (editorEl) {
          const editorRect = editorEl.getBoundingClientRect()
          const target = event.target as HTMLElement
          const rect = target.getBoundingClientRect()
          annotationPopupStyle.value = {
            top: `${rect.bottom - editorRect.top + 8}px`,
            left: `${rect.left - editorRect.left}px`,
            width: '340px'
          }
        }
        showAnnotationPopup.value = true
        emit('view-annotation', annotation.id)
      }
    })
  ],
  onUpdate: ({editor: editorInstance}) => {
    if (isUpdatingFromProp) return
    const text = htmlToText(editorInstance.getHTML())
    emit('update:modelValue', text)
    emit('stats-change', calculateStats(text))
  },
  onFocus: () => {
    isFocused.value = true
  },
  onBlur: () => {
    isFocused.value = false;
    emit('blur')
  },
  onSelectionUpdate: ({editor: editorInstance}) => {
    const {from, to} = editorInstance.state.selection
    if (from === to) {
      showSelectionBubble.value = false
      selectedRange.value = null
      return
    }
    const selectedText = editorInstance.state.doc.textBetween(from, to)
    if (selectedText.trim()) {
      selectedRange.value = {from, to, text: selectedText}
      showSelectionBubbleAtPosition()
    }
  }
})

const _checkAnnotationAtCursor = (position: number) => {
  if (!editor.value) return
  // 置顶状态下不重新定位弹窗，避免跳动
  if (isPopupPinned.value) return

  // 检查当前位置和前一个位置的 marks（处理 mark 边界情况）
  const doc = editor.value.state.doc
  let annotationMark = null

  // 优先检查当前位置
  const resolvedPos = doc.resolve(position)
  annotationMark = resolvedPos.marks().find(m => m.type.name === 'annotation') || null

  // 当前位置没有，检查前一个位置（光标可能在 mark 右边界）
  if (!annotationMark && position > 0) {
    const prevPos = doc.resolve(position - 1)
    annotationMark = prevPos.marks().find(m => m.type.name === 'annotation') || null
  }

  // 备选方案：通过 DOM 检测点击的高亮元素
  if (!annotationMark) {
    const domAtPos = editor.value.view.domAtPos(position)
    const element = domAtPos.node instanceof HTMLElement
        ? domAtPos.node
        : domAtPos.node.parentElement
    const highlightEl = element?.closest('.annotation-highlight') as HTMLElement | null
    if (highlightEl) {
      const annotationId = Number(highlightEl.getAttribute('data-annotation-id'))
      const annotation = props.annotations.find(a => a.id === annotationId)
      if (annotation) {
        viewingAnnotation.value = annotation
        annotationMode.value = 'view'
        aiRewriteResult.value = annotation.aiRewriteResult || ''
        isAiRewriting.value = false
        showAnnotationPopupAtCursor()
        showAnnotationPopup.value = true
        emit('view-annotation', annotation.id)
        return
      }
    }
  }

  if (annotationMark) {
    const annotationId = Number(annotationMark.attrs.annotationId)
    const annotation = props.annotations.find(a => a.id === annotationId)
    if (annotation) {
      viewingAnnotation.value = annotation
      annotationMode.value = 'view'
      aiRewriteResult.value = annotation.aiRewriteResult || ''
      isAiRewriting.value = false
      showAnnotationPopupAtCursor()
      showAnnotationPopup.value = true
      emit('view-annotation', annotation.id)
    }
  } else if (annotationMode.value === 'view' && !isPopupPinned.value) {
    showAnnotationPopup.value = false
  }
}

const showSelectionBubbleAtPosition = () => {
  nextTick(() => {
    const editorEl = document.querySelector('.tiptap-editor-wrapper')
    if (!editorEl) return
    const selection = window.getSelection()
    if (!selection || selection.rangeCount === 0) return
    const rect = selection.getRangeAt(0).getBoundingClientRect()
    const editorRect = editorEl.getBoundingClientRect()
    selectionBubbleStyle.value = {
      top: `${rect.top - editorRect.top - 40}px`,
      left: `${rect.left - editorRect.left + rect.width / 2}px`,
      transform: 'translateX(-50%)'
    }
    showSelectionBubble.value = true
  })
}

const showAnnotationPopupAtCursor = () => {
  nextTick(() => {
    const editorEl = document.querySelector('.tiptap-editor-wrapper')
    if (!editorEl) return
    const selection = window.getSelection()
    if (!selection || selection.rangeCount === 0) return
    const rect = selection.getRangeAt(0).getBoundingClientRect()
    const editorRect = editorEl.getBoundingClientRect()
    annotationPopupStyle.value = {
      top: `${rect.bottom - editorRect.top + 8}px`,
      left: `${rect.left - editorRect.left}px`,
      width: '340px'
    }
  })
}

const openCreateAnnotation = () => {
  showSelectionBubble.value = false
  annotationMode.value = 'create'
  annotationInputText.value = ''
  const editorEl = document.querySelector('.tiptap-editor-wrapper')
  if (!editorEl) return
  const selection = window.getSelection()
  if (!selection || selection.rangeCount === 0) return
  const rect = selection.getRangeAt(0).getBoundingClientRect()
  const editorRect = editorEl.getBoundingClientRect()
  annotationPopupStyle.value = {
    top: `${rect.bottom - editorRect.top + 8}px`,
    left: `${rect.left - editorRect.left}px`,
    width: '340px'
  }
  showAnnotationPopup.value = true
  nextTick(() => annotationInputRef.value?.focus())
}

const submitAnnotation = () => {
  if (!annotationInputText.value.trim() || !selectedRange.value || !editor.value) return
  const {from, to, text} = selectedRange.value
  emit('create-annotation', {
    startOffset: calculateTextOffset(from),
    endOffset: calculateTextOffset(to),
    originalText: text,
    annotationContent: annotationInputText.value.trim()
  })
  closeAnnotationPopup()
}

const handleDeleteAnnotation = () => {
  if (!viewingAnnotation.value) return
  emit('delete-annotation', viewingAnnotation.value.id)
  closeAnnotationPopup()
}

/**
 * 引用批注内容作为改写提示词
 */
const quoteAnnotationAsPrompt = () => {
  if (!viewingAnnotation.value) return
  rewritePromptText.value = viewingAnnotation.value.annotationContent
}

/**
 * 触发 AI 改写（使用用户输入的提示词）
 */
const handleAiRewrite = () => {
  if (!viewingAnnotation.value || !rewritePromptText.value.trim()) return
  aiRewriteResult.value = ''
  isAiRewriting.value = true

  emit('ai-rewrite-annotation', {
    annotationId: viewingAnnotation.value.id,
    originalText: viewingAnnotation.value.originalText,
    annotationContent: rewritePromptText.value.trim(),
    fullChapterContent: props.modelValue
  }, {
    onToken: (token: string) => {
      aiRewriteResult.value += token
    },
    onComplete: (fullText: string) => {
      aiRewriteResult.value = fullText
      isAiRewriting.value = false
    },
    onError: (error: Error) => {
      console.error('AI 改写失败:', error)
      isAiRewriting.value = false
    }
  })
}

/**
 * 停止 AI 改写
 */
const stopAiRewrite = () => {
  aiRewriteAbortController?.abort()
  aiRewriteAbortController = null
  isAiRewriting.value = false
}

/**
 * 采纳 AI 改写结果
 */
const handleAcceptRewrite = () => {
  if (!viewingAnnotation.value || !aiRewriteResult.value) return
  emit('accept-rewrite', {
    annotationId: viewingAnnotation.value.id,
    originalText: viewingAnnotation.value.originalText,
    rewriteResult: aiRewriteResult.value
  })
  closeAnnotationPopup()
}

/**
 * 开始拖拽调整弹窗大小
 */
const startResize = (event: MouseEvent) => {
  if (!popupRef.value) return
  isResizing = true
  resizeStartX = event.clientX
  resizeStartY = event.clientY
  const rect = popupRef.value.getBoundingClientRect()
  resizeStartWidth = rect.width
  resizeStartHeight = rect.height
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
  event.preventDefault()
}

const onResize = (event: MouseEvent) => {
  if (!isResizing || !popupRef.value) return
  const newWidth = Math.max(280, resizeStartWidth + (event.clientX - resizeStartX))
  const newHeight = Math.max(200, resizeStartHeight + (event.clientY - resizeStartY))
  popupRef.value.style.width = `${newWidth}px`
  popupRef.value.style.height = `${newHeight}px`
}

const stopResize = () => {
  isResizing = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}

const closeAnnotationPopup = () => {
  showAnnotationPopup.value = false
  isPopupPinned.value = false
  annotationInputText.value = ''
  viewingAnnotation.value = null
  selectedRange.value = null
  aiRewriteResult.value = ''
  rewritePromptText.value = ''
  isAiRewriting.value = false
  stopAiRewrite()
}

// ==================== 快捷 AI 改写 ====================

/**
 * 打开快捷 AI 改写浮窗
 */
const openQuickRewrite = () => {
  if (!selectedRange.value) return
  showSelectionBubble.value = false
  quickRewriteOriginalText.value = selectedRange.value.text
  quickRewritePrompt.value = ''
  quickRewriteResult.value = ''
  isQuickRewriting.value = false
  _quickRewriteSelectedRange = {...selectedRange.value}

  const editorEl = document.querySelector('.tiptap-editor-wrapper')
  if (!editorEl) return
  const selection = window.getSelection()
  if (!selection || selection.rangeCount === 0) return
  const rect = selection.getRangeAt(0).getBoundingClientRect()
  const editorRect = editorEl.getBoundingClientRect()
  quickRewritePopupStyle.value = {
    top: `${rect.bottom - editorRect.top + 8}px`,
    left: `${rect.left - editorRect.left}px`,
    width: '360px'
  }
  showQuickRewritePopup.value = true
  nextTick(() => quickRewriteInputRef.value?.focus())
}

/**
 * 执行快捷 AI 改写
 */
const handleQuickRewrite = () => {
  if (!quickRewritePrompt.value.trim()) return
  quickRewriteResult.value = ''
  isQuickRewriting.value = true

  emit('quick-rewrite', {
    originalText: quickRewriteOriginalText.value,
    rewritePrompt: quickRewritePrompt.value.trim(),
    fullChapterContent: props.modelValue
  }, {
    onToken: (token: string) => {
      quickRewriteResult.value += token
    },
    onComplete: (fullText: string) => {
      quickRewriteResult.value = fullText
      isQuickRewriting.value = false
    },
    onError: () => {
      isQuickRewriting.value = false
    }
  })
}

/**
 * 停止快捷改写
 */
const stopQuickRewrite = () => {
  isQuickRewriting.value = false
}

/**
 * 采纳快捷改写结果
 */
const handleAcceptQuickRewrite = () => {
  if (!quickRewriteResult.value) return
  emit('accept-quick-rewrite', {
    originalText: quickRewriteOriginalText.value,
    rewriteResult: quickRewriteResult.value
  })
  closeQuickRewrite()
}

/**
 * 关闭快捷改写浮窗
 */
const closeQuickRewrite = () => {
  showQuickRewritePopup.value = false
  quickRewriteOriginalText.value = ''
  quickRewritePrompt.value = ''
  quickRewriteResult.value = ''
  isQuickRewriting.value = false
  _quickRewriteSelectedRange = null
}

/**
 * 快捷改写浮窗拖拽
 */
const startDragQuickRewrite = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (target.closest('.popup-header-actions') || target.closest('.popup-close')) return
  if (!quickRewriteRef.value) return
  isDragging = true
  dragStartX = event.clientX
  dragStartY = event.clientY
  const rect = quickRewriteRef.value.getBoundingClientRect()
  const parentRect = quickRewriteRef.value.offsetParent?.getBoundingClientRect() || {left: 0, top: 0}
  dragStartLeft = rect.left - parentRect.left
  dragStartTop = rect.top - parentRect.top
  const el = quickRewriteRef.value
  document.addEventListener('mousemove', (e) => {
    if (!isDragging || !el) return
    el.style.left = `${dragStartLeft + (e.clientX - dragStartX)}px`
    el.style.top = `${dragStartTop + (e.clientY - dragStartY)}px`
  })
  document.addEventListener('mouseup', () => {
    isDragging = false
    if (el) {
      quickRewritePopupStyle.value = {
        ...quickRewritePopupStyle.value,
        top: el.style.top,
        left: el.style.left
      }
    }
  }, {once: true})
  event.preventDefault()
}

const startResizeQuickRewrite = (event: MouseEvent) => {
  if (!quickRewriteRef.value) return
  isResizing = true
  resizeStartX = event.clientX
  resizeStartY = event.clientY
  const rect = quickRewriteRef.value.getBoundingClientRect()
  resizeStartWidth = rect.width
  resizeStartHeight = rect.height
  const el = quickRewriteRef.value
  const onMove = (e: MouseEvent) => {
    if (!isResizing || !el) return
    el.style.width = `${Math.max(280, resizeStartWidth + (e.clientX - resizeStartX))}px`
    el.style.height = `${Math.max(200, resizeStartHeight + (e.clientY - resizeStartY))}px`
  }
  const onUp = () => {
    isResizing = false
    document.removeEventListener('mousemove', onMove)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp, {once: true})
  event.preventDefault()
}

const applyAnnotationMarks = () => {
  if (!editor.value) return
  const {doc, selection} = editor.value.state
  const docSize = doc.content.size
  const savedPosition = Math.min(selection.anchor, docSize - 1)

  // 先选中全文，清除所有批注标记
  editor.value.chain()
      .setTextSelection({from: 0, to: docSize})
      .unsetMark('annotation')
      .setTextSelection(savedPosition)
      .run()

  const fullText = doc.textBetween(0, docSize, '\n')

  // 通过原文内容在文档中搜索定位
  for (const annotation of props.annotations) {
    // 已采纳和已处理的批注不再高亮，只高亮待处理的
    if (annotation.status === 'ACCEPTED' || annotation.status === 'RESOLVED') continue

    const searchText = annotation.originalText
    if (!searchText) continue

    const textIndex = fullText.indexOf(searchText)
    if (textIndex < 0) continue

    // 构建位置映射表：纯文本偏移量 → 文档位置
    const fromPos = textOffsetToDocPos(doc, docSize, textIndex)
    const toPos = textOffsetToDocPos(doc, docSize, textIndex + searchText.length)

    if (fromPos >= 0 && toPos > fromPos && toPos <= docSize) {
      editor.value.chain()
          .setTextSelection({from: fromPos, to: toPos})
          .setMark('annotation', {
            annotationId: String(annotation.id),
            status: annotation.status
          })
          .run()
    }
  }

  // 恢复光标位置
  editor.value.commands.setTextSelection(Math.min(savedPosition, docSize - 1))
}

/**
 * 将纯文本偏移量转换为 Tiptap 文档位置。
 * 逐段落遍历，精确计算每个段落内的文档位置。
 */
const textOffsetToDocPos = (doc: {
  childCount: number;
  child: (index: number) => { textContent: string }
}, docSize: number, targetOffset: number): number => {
  let textOffset = 0
  let docPos = 0

  for (let i = 0; i < doc.childCount; i++) {
    const child = doc.child(i)
    // 段落开始标签占 1 个文档位置
    docPos += 1

    // 非首段前有换行符（纯文本中的 \n）
    if (i > 0) {
      textOffset += 1
    }

    const paraText = child.textContent
    const paraLen = paraText.length

    if (targetOffset >= textOffset && targetOffset <= textOffset + paraLen) {
      // 目标在这个段落内
      return docPos + (targetOffset - textOffset)
    }

    textOffset += paraLen
    // 段落内容 + 结束标签
    docPos += paraLen + 1
  }

  return -1
}

watch(() => props.annotations, () => {
  nextTick(() => applyAnnotationMarks())
}, {deep: true})

watch(() => props.modelValue, (newValue) => {
  if (!editor.value) return
  const currentText = htmlToText(editor.value.getHTML())
  if (newValue === currentText) return
  isUpdatingFromProp = true
  editor.value.commands.setContent(textToHtml(newValue), false)
  nextTick(() => {
    isUpdatingFromProp = false
    // 内容更新后重新应用批注标记（setContent 会清除所有 marks）
    if (props.annotations.length > 0) {
      applyAnnotationMarks()
    }
  })
})

watch(() => props.readonly, (newValue) => {
  editor.value?.setEditable(!newValue)
})

const scrollToBottom = () => {
  nextTick(() => {
    const editorEl = document.querySelector('.tiptap-editor-content .tiptap')
    if (editorEl) editorEl.scrollTop = editorEl.scrollHeight
  })
}

/**
 * 跳转到指定批注的位置（供批注列表点击调用）
 */
const scrollToAnnotation = (annotation: ChapterAnnotation) => {
  if (!editor.value) return

  // 通过原文内容在文档中搜索定位
  const doc = editor.value.state.doc
  const docSize = doc.content.size
  const fullText = doc.textBetween(0, docSize, '\n')
  const textIndex = fullText.indexOf(annotation.originalText)
  if (textIndex < 0) return

  const from = textOffsetToDocPos(doc, docSize, textIndex)
  const to = textOffsetToDocPos(doc, docSize, textIndex + annotation.originalText.length)
  if (from < 0 || to < 0) return

  // 选中批注对应的文本
  editor.value.chain().focus().setTextSelection({from, to}).run()

  // 等待 DOM 更新后滚动到可视区域的二分之一处
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      const editorEl = document.querySelector('.tiptap-editor-content .tiptap') as HTMLElement
      if (!editorEl) return

      const highlightEl = editorEl.querySelector(
          `span[data-annotation-id="${annotation.id}"]`
      ) as HTMLElement
      if (highlightEl) {
        const editorRect = editorEl.getBoundingClientRect()
        const highlightRect = highlightEl.getBoundingClientRect()
        const targetOffset = editorRect.height / 2
        const scrollTarget = editorEl.scrollTop + (highlightRect.top - editorRect.top) - targetOffset
        editorEl.scrollTo({top: Math.max(0, scrollTarget), behavior: 'smooth'})
        return
      }

      const selection = window.getSelection()
      if (!selection || selection.rangeCount === 0) return
      const range = selection.getRangeAt(0)
      const rect = range.getBoundingClientRect()
      const editorRect = editorEl.getBoundingClientRect()
      const targetOffset = editorRect.height / 2
      const scrollTarget = editorEl.scrollTop + (rect.top - editorRect.top) - targetOffset
      editorEl.scrollTo({top: Math.max(0, scrollTarget), behavior: 'smooth'})
    })
  })

  emit('view-annotation', annotation.id)
}

/**
 * 通过 DOM 点击事件直接检测批注高亮元素（最可靠的方案）
 */
const handleEditorClick = (event: MouseEvent) => {
  if (isPopupPinned.value) return
  const target = event.target as HTMLElement
  const highlightEl = target.closest('.annotation-highlight') as HTMLElement | null
  if (highlightEl) {
    const annotationId = Number(highlightEl.getAttribute('data-annotation-id'))
    const annotation = props.annotations.find(a => a.id === annotationId)
    if (annotation) {
      viewingAnnotation.value = annotation
      annotationMode.value = 'view'
      aiRewriteResult.value = annotation.aiRewriteResult || ''
      rewritePromptText.value = ''
      isAiRewriting.value = false

      // 计算弹窗位置
      const editorEl = document.querySelector('.tiptap-editor-wrapper')
      if (editorEl) {
        const rect = highlightEl.getBoundingClientRect()
        const editorRect = editorEl.getBoundingClientRect()
        annotationPopupStyle.value = {
          top: `${rect.bottom - editorRect.top + 8}px`,
          left: `${rect.left - editorRect.left}px`,
          width: '340px'
        }
      }
      showAnnotationPopup.value = true
      emit('view-annotation', annotation.id)
    }
  }
}

// 挂载 DOM 点击事件（绑定到 wrapper，使用事件委托）
onMounted(() => {
  nextTick(() => {
    const wrapperEl = document.querySelector('.tiptap-editor-wrapper')
    wrapperEl?.addEventListener('click', handleEditorClick as EventListener)
  })
})

/**
 * 跳转到批注位置并自动弹出详情弹窗（供批注列表"采纳"按钮调用）
 */
const scrollToAnnotationAndShowPopup = (annotation: ChapterAnnotation) => {
  scrollToAnnotation(annotation)
  // 延迟弹出详情弹窗，等待滚动完成
  setTimeout(() => {
    viewingAnnotation.value = annotation
    annotationMode.value = 'view'
    aiRewriteResult.value = annotation.aiRewriteResult || ''
    rewritePromptText.value = ''
    isAiRewriting.value = false
    // 通过高亮元素定位弹窗
    const editorEl = document.querySelector('.tiptap-editor-wrapper')
    const highlightEl = document.querySelector(
        `.tiptap-editor-content .tiptap span[data-annotation-id="${annotation.id}"]`
    ) as HTMLElement | null
    if (editorEl && highlightEl) {
      const editorRect = editorEl.getBoundingClientRect()
      const rect = highlightEl.getBoundingClientRect()
      annotationPopupStyle.value = {
        top: `${rect.bottom - editorRect.top + 8}px`,
        left: `${rect.left - editorRect.left}px`,
        width: '340px'
      }
    }
    showAnnotationPopup.value = true
    emit('view-annotation', annotation.id)
  }, 500)
}

defineExpose({
  scrollToBottom, editor, applyAnnotationMarks,
  scrollToAnnotation, scrollToAnnotationAndShowPopup
})

onBeforeUnmount(() => {
  stopAiRewrite()
  stopDrag()
  stopResize()
  const wrapperEl = document.querySelector('.tiptap-editor-wrapper')
  wrapperEl?.removeEventListener('click', handleEditorClick as EventListener)
  editor.value?.destroy()
})
</script>

<style scoped>
.tiptap-editor-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  transition: all 0.2s;
  background: #f8f9fa;
  border: 1px solid #dadce0;
  margin: 4px 16px;
  position: relative;
}

.tiptap-editor-wrapper.is-focused {
  border-color: #1a73e8;
  background: #fff;
  box-shadow: 0 0 0 2px rgba(26, 115, 232, 0.1);
}

.tiptap-editor-content {
  flex: 1;
  overflow: hidden;
}

.tiptap-editor-content :deep(.tiptap) {
  height: 100%;
  overflow-y: auto;
  outline: none;
  font-size: 17px;
  line-height: 1.9;
  padding: 20px 24px;
  color: #202124;
  cursor: default;
  background: var(--editor-bg-color, #fefefe);
  caret-color: var(--editor-caret-color, #1a73e8);
}

.tiptap-editor-content :deep(.tiptap p) {
  margin: 0;
  min-height: 1.9em;
}

.tiptap-editor-content :deep(.tiptap p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  float: left;
  color: #adb5bd;
  pointer-events: none;
  height: 0;
  font-size: 17px;
}

.tiptap-editor-content :deep(.annotation-highlight) {
  background: rgba(255, 213, 79, 0.35);
  border-bottom: 2px solid #ffc107;
  cursor: pointer;
  transition: background 0.2s;
  border-radius: 2px;
  padding: 1px 0;
}

.tiptap-editor-content :deep(.annotation-highlight:hover) {
  background: rgba(255, 213, 79, 0.55);
}

/* 已解决批注：淡绿色高亮，视觉上更柔和 */
.tiptap-editor-content :deep(.annotation-resolved) {
  background: rgba(52, 211, 153, 0.2);
  border-bottom: 2px solid #6ee7b7;
}

.tiptap-editor-content :deep(.annotation-resolved:hover) {
  background: rgba(52, 211, 153, 0.35);
}

.tiptap-editor-content :deep(.tiptap::-webkit-scrollbar) {
  width: 6px;
}

.tiptap-editor-content :deep(.tiptap::-webkit-scrollbar-track) {
  background: transparent;
}

.tiptap-editor-content :deep(.tiptap::-webkit-scrollbar-thumb) {
  background: #dadce0;
  border-radius: 3px;
}

.tiptap-editor-content :deep(.tiptap::-webkit-scrollbar-thumb:hover) {
  background: #bdc1c6;
}

/* 选中文字气泡 */
.selection-bubble {
  position: absolute;
  z-index: 20;
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  padding: 4px;
}

.bubble-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 13px;
  color: #5f6368;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s;
}

.bubble-btn:hover {
  background: #f0f0f0;
  color: #202124;
}

.bubble-icon {
  font-size: 14px;
}

.bubble-divider {
  width: 1px;
  height: 20px;
  background: #e0e0e0;
  margin: 0 2px;
}

/* 批注浮窗 */
.annotation-popup {
  position: absolute;
  z-index: 30;
  min-width: 280px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 拖拽调整大小手柄 */
.popup-resize-handle {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 16px;
  height: 16px;
  cursor: nwse-resize;
  z-index: 5;
}

.popup-resize-handle::after {
  content: '';
  position: absolute;
  bottom: 3px;
  right: 3px;
  width: 8px;
  height: 8px;
  border-right: 2px solid #c0c4cc;
  border-bottom: 2px solid #c0c4cc;
  opacity: 0.6;
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid #d8dae0;
  background: #e4e6ec;
  flex-shrink: 0;
  cursor: grab;
  user-select: none;
}

.popup-header:active {
  cursor: grabbing;
  background: #d8dae0;
}

.popup-header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.popup-pin-btn {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
  opacity: 0.4;
}

.popup-pin-btn:hover {
  opacity: 0.7;
  background: #f0f0f0;
}

.popup-pin-btn.is-pinned {
  opacity: 1;
  background: #eef0ff;
  transform: rotate(45deg);
}

.popup-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.popup-close {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 4px;
  font-size: 16px;
  color: #9aa0a6;
  cursor: pointer;
  transition: all 0.15s;
}

.popup-close:hover {
  background: #f0f0f0;
  color: #5f6368;
}

.annotation-input {
  width: 100%;
  border: none;
  outline: none;
  padding: 12px 14px;
  font-size: 13px;
  line-height: 1.6;
  resize: none;
  font-family: inherit;
  color: #303133;
  box-sizing: border-box;
  flex: 1;
  min-height: 60px;
}

.annotation-input::placeholder {
  color: #bdc1c6;
}

.popup-view-content {
  padding: 12px 14px;
  flex: 1;
  overflow-y: auto;
}

/* 批注人和时间 */
.annotation-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f5f5f5;
}

.annotation-author {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #667eea;
}

.annotation-time {
  font-size: 11px;
  color: #9aa0a6;
}

.meta-icon {
  font-size: 13px;
}

.annotation-original-text {
  font-size: 12px;
  color: #9aa0a6;
  font-style: italic;
  margin-bottom: 8px;
  padding: 6px 10px;
  background: #f8f9fa;
  border-radius: 6px;
  line-height: 1.5;
  max-height: 60px;
  overflow-y: auto;
}

.annotation-text {
  font-size: 13px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 12px;
}

/* AI 改写区域 */
.ai-rewrite-section {
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
  margin-top: 4px;
}

.ai-rewrite-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.ai-rewrite-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #667eea;
}

.ai-rewrite-result {
  padding: 10px 12px;
  background: #f8f9fe;
  border-radius: 8px;
  border: 1px solid #eef0f8;
  margin-bottom: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.rewrite-content {
  font-size: 13px;
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
}

.rewrite-content.is-streaming {
  color: #667eea;
}

.typing-cursor {
  animation: blink 0.8s infinite;
  font-weight: 300;
  color: #667eea;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

.ai-rewrite-actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.popup-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 10px 14px;
  border-top: 1px solid #f0f0f0;
  background: #fafbfc;
  flex-shrink: 0;
}

.popup-btn {
  padding: 5px 14px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.popup-btn-cancel {
  background: #f0f0f0;
  color: #5f6368;
}

.popup-btn-cancel:hover {
  background: #e0e0e0;
}

.popup-btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.popup-btn-submit:hover {
  opacity: 0.9;
}

.popup-btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.popup-btn-delete {
  background: #fff0f0;
  color: #ef4444;
}

.popup-btn-delete:hover {
  background: #fee2e2;
}

.popup-btn-ai-sm {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.popup-btn-ai-sm:hover {
  opacity: 0.9;
}

.popup-btn-ai-sm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 引用批注按钮 */
.quote-annotation-btn {
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 3px 8px;
  border: 1px solid #e8eaed;
  border-radius: 4px;
  font-size: 11px;
  color: #667eea;
  background: #f8f9fe;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}

.quote-annotation-btn:hover {
  background: #eef0ff;
  border-color: #667eea;
}

/* 改写提示词输入框 */
.rewrite-prompt-input {
  width: 100%;
  border: 1px solid #e8eaed;
  outline: none;
  padding: 8px 10px;
  font-size: 12px;
  line-height: 1.5;
  resize: vertical;
  font-family: inherit;
  color: #303133;
  box-sizing: border-box;
  border-radius: 6px;
  background: #fafbfc;
  margin-bottom: 8px;
  min-height: 48px;
}

.rewrite-prompt-input:focus {
  border-color: #667eea;
  background: #fff;
}

.rewrite-prompt-input::placeholder {
  color: #bdc1c6;
}

.rewrite-prompt-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.popup-btn-stop {
  background: #fef3cd;
  color: #856404;
}

.popup-btn-stop:hover {
  background: #ffeeba;
}

.popup-btn-accept {
  background: #d4edda;
  color: #155724;
}

.popup-btn-accept:hover {
  background: #c3e6cb;
}

.popup-btn-retry {
  background: #f0f0f0;
  color: #5f6368;
}

.popup-btn-retry:hover {
  background: #e0e0e0;
}
</style>
