<template>
    <div class="novel-ai-settings-form">
        <!-- 继承提示：仅当小说级别配置未创建时显示 -->
        <el-alert v-if="showInheritAlert" type="info" :closable="false" show-icon class="inherit-alert">
            当前显示的是全局默认配置，保存后将创建该小说的独立配置
        </el-alert>

        <!-- 自动操作 -->
        <div class="settings-group">
            <div class="group-title">
                <span class="group-dot dot-blue"></span>
                自动操作
            </div>
            <div class="switch-list">
                <div class="switch-item">
                    <div class="setting-info">
                        <span class="setting-label">自动审核</span>
                        <span class="setting-desc">改写完成后自动调用 AI 审核接口检查内容</span>
                    </div>
                    <el-switch
                        :active-value="1"
                        :inactive-value="0"
                        :model-value="modelValue.autoAuditAfterRewrite"
                        @update:model-value="updateSetting('autoAuditAfterRewrite', $event)"
                    />
                </div>
                <div class="switch-item">
                    <div class="setting-info">
                        <span class="setting-label">自动生成 AI 大纲</span>
                        <span class="setting-desc">发布章节后自动调用 AI 总结章节大纲</span>
                    </div>
                    <el-switch
                        :active-value="1"
                        :inactive-value="0"
                        :model-value="modelValue.autoOutlineAfterPublish"
                        @update:model-value="updateSetting('autoOutlineAfterPublish', $event)"
                    />
                </div>
                <div class="switch-item">
                    <div class="setting-info">
                        <span class="setting-label">自动存储向量</span>
                        <span class="setting-desc">发布章节后自动将内容存入向量数据库</span>
                    </div>
                    <el-tooltip :content="embeddingTooltip" :disabled="embeddingConfigured" placement="top">
                        <span>
                            <el-switch
                                :model-value="modelValue.autoVectorAfterPublish"
                                :active-value="1"
                                :inactive-value="0"
                                :disabled="!embeddingConfigured"
                                @update:model-value="updateSetting('autoVectorAfterPublish', $event)"
                            />
                        </span>
                    </el-tooltip>
                </div>
                <div class="switch-item">
                    <div class="setting-info">
                        <span class="setting-label">自动保存内容</span>
                        <span class="setting-desc">编辑器内容变化时自动保存到服务器</span>
                    </div>
                    <el-switch
                        :active-value="1"
                        :inactive-value="0"
                        :model-value="modelValue.autoSaveContent"
                        @update:model-value="updateSetting('autoSaveContent', $event)"
                    />
                </div>
            </div>
        </div>

        <!-- AI 改写设置 -->
        <div class="settings-group">
            <div class="group-title">
                <span class="group-dot dot-orange"></span>
                AI 改写设置
            </div>
            <div class="number-row">
                <span class="setting-label">携带前情提要章数</span>
                <el-input-number
                    :model-value="modelValue.rewriteContextChapters"
                    :min="0"
                    :max="10"
                    :step="1"
                    size="small"
                    controls-position="right"
                    @update:model-value="updateSetting('rewriteContextChapters', $event)"
                />
            </div>
            <div class="number-hint">AI 改写时携带前几章内容（0 表示不携带）</div>
            <div class="switch-list" style="margin-top: 10px">
                <div class="switch-item">
                    <div class="setting-info">
                        <span class="setting-label">改写时携带全文大纲</span>
                        <span class="setting-desc">AI 改写时将全文大纲作为参考上下文</span>
                    </div>
                    <el-switch
                        :active-value="1"
                        :inactive-value="0"
                        :model-value="modelValue.rewriteIncludeOutline"
                        @update:model-value="updateSetting('rewriteIncludeOutline', $event)"
                    />
                </div>
                <div class="switch-item">
                    <div class="setting-info">
                        <span class="setting-label">审核时携带全文大纲</span>
                        <span class="setting-desc">AI 审核时将全文大纲作为参考上下文</span>
                    </div>
                    <el-switch
                        :active-value="1"
                        :inactive-value="0"
                        :model-value="modelValue.auditIncludeOutline"
                        @update:model-value="updateSetting('auditIncludeOutline', $event)"
                    />
                </div>
            </div>
        </div>

        <!-- AI 问答设置 -->
        <div class="settings-group">
            <div class="group-title">
                <span class="group-dot dot-green"></span>
                AI 问答设置
            </div>
            <div class="switch-list">
                <div class="switch-item">
                    <div class="setting-info">
                        <span class="setting-label">问答时携带全文大纲</span>
                        <span class="setting-desc">AI 问答时将全文大纲作为参考上下文</span>
                    </div>
                    <el-switch
                        :active-value="1"
                        :inactive-value="0"
                        :model-value="modelValue.qaIncludeOutline"
                        @update:model-value="updateSetting('qaIncludeOutline', $event)"
                    />
                </div>
            </div>
            <div class="number-row" style="margin-top: 10px">
                <span class="setting-label">历史消息条数</span>
                <el-input-number
                    :model-value="modelValue.qaContextLength"
                    :min="0"
                    :max="50"
                    :step="1"
                    size="small"
                    controls-position="right"
                    @update:model-value="updateSetting('qaContextLength', $event)"
                />
            </div>
            <div class="number-hint">AI 问答时携带的历史对话条数（0 表示不携带）</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getEmbeddingConfigList } from '@/api/embedding-config'
import type { NovelSettings } from '@/api/novel-settings'

/**
 * 小说 AI 功能配置共享组件。
 *
 * <p>统一小说列表、创作页侧边栏、首页默认设置三处的 AI 配置表单，
 * 避免重复实现导致字段不一致和难以同步修改的问题。</p>
 *
 * <p>内部封装 Embedding 配置检测：用户未配置 Embedding 模型时，
 * 自动禁用"自动存储向量"开关并给出提示，避免开启后无效调用。</p>
 */
const props = withDefaults(
    defineProps<{
        /** 配置数据（v-model） */
        modelValue: NovelSettings
        /** 是否显示"当前显示全局默认配置"的继承提示（小说级别场景使用） */
        showInheritAlert?: boolean
    }>(),
    {
        showInheritAlert: false,
    },
)

const emit = defineEmits<{
    (e: 'update:modelValue', value: NovelSettings): void
}>()

type SettingField = Exclude<keyof NovelSettings, 'id' | 'novelId' | 'userId' | 'createTime' | 'updateTime'>

function updateSetting(key: SettingField, value: unknown) {
    if (typeof value === 'number' && Number.isFinite(value) && value !== props.modelValue[key]) {
        emit('update:modelValue', { ...props.modelValue, [key]: value })
    }
}

/** 用户是否已配置 Embedding 模型 */
const embeddingConfigured = ref(false)

/** Embedding 未配置时的提示文案 */
const embeddingTooltip = computed(() =>
    embeddingConfigured.value ? '' : '请先在「模型管理」的嵌入式模型页中添加并激活一个模型后再开启此功能',
)

/**
 * 加载用户 Embedding 配置状态。
 *
 * <p>检查用户是否至少有一个激活的 Embedding 配置，
 * 用于决定"自动存储向量"开关是否可用。</p>
 */
const loadEmbeddingStatus = async () => {
    try {
        const list = await getEmbeddingConfigList()
        embeddingConfigured.value = list.some((item) => item.isActive === 1)
    } catch (error) {
        console.error('加载 Embedding 配置状态失败', error)
        embeddingConfigured.value = false
    }
}

onMounted(() => {
    loadEmbeddingStatus()
})
</script>

<style scoped>
.novel-ai-settings-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.inherit-alert {
    margin-bottom: 8px;
}

.settings-group {
    padding: 16px;
    background: #fafbfc;
    border-radius: 8px;
    border: 1px solid #f0f0f0;
}

.group-title {
    display: flex;
    align-items: center;
    font-size: 14px;
    font-weight: 600;
    color: var(--ink);
    margin-bottom: 14px;
}

.group-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 8px;
}

.dot-blue {
    background: var(--pine);
}

.dot-orange {
    background: #f59e0b;
}

.dot-green {
    background: #10b981;
}

.switch-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.switch-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
}

.setting-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.setting-label {
    font-size: 14px;
    color: var(--ink);
    font-weight: 500;
}

.setting-desc {
    font-size: 12px;
    color: #909399;
}

.number-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4px 0;
}

.number-hint {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
}
</style>
