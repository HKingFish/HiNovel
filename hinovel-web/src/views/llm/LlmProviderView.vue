<template>
  <div class="model-manage-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>模型管理</h1>
        <p class="subtitle">统一管理 LLM 对话模型、Embedding 嵌入式模型等各类 AI 模型配置</p>
      </div>
    </div>

    <!-- Tab 切换区域（数据驱动，便于扩展） -->
    <el-tabs v-model="activeTab" class="model-tabs">
      <el-tab-pane
          v-for="tab in modelTabs"
          :key="tab.key"
          :label="tab.label"
          :name="tab.key"
      >
        <component :is="tab.component"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import {markRaw, ref, type Component} from 'vue'
import LlmProviderTab from './LlmProviderTab.vue'
import EmbeddingConfigTab from './EmbeddingConfigTab.vue'

/**
 * 模型 Tab 配置项接口。
 *
 * <p>后续新增模型类型（如重排序模型）时，只需在 modelTabs 数组中追加一项即可。</p>
 */
interface ModelTabConfig {
    /** Tab 唯一标识 */
    key: string
    /** Tab 显示名称 */
    label: string
    /** Tab 对应的子组件 */
    component: Component
}

/**
 * 模型类型 Tab 配置列表。
 *
 * <p>扩展方式：新增模型类型时，创建对应的 Tab 子组件，然后在此数组追加配置项。</p>
 * <p>例如新增重排序模型：{ key: 'reranker', label: '重排序模型', component: markRaw(RerankerConfigTab) }</p>
 */
const modelTabs: ModelTabConfig[] = [
    {key: 'llm', label: 'LLM 对话模型', component: markRaw(LlmProviderTab)},
    {key: 'embedding', label: '嵌入式模型', component: markRaw(EmbeddingConfigTab)}
]

/** 当前激活的 Tab */
const activeTab = ref<string>(modelTabs[0].key)
</script>

<style scoped lang="scss">
.model-manage-page {
  padding: 24px;
  padding-bottom: 40px;
  min-width: 900px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;

  .header-content {
    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 6px;
    }

    .subtitle {
      font-size: 14px;
      color: #6b7280;
      margin: 0;
    }
  }
}

.model-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }

  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
    padding: 0 20px;
    height: 42px;
    line-height: 42px;
  }

  :deep(.el-tabs__item.is-active) {
    color: #6366f1;
  }

  :deep(.el-tabs__active-bar) {
    background-color: #6366f1;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background-color: #e5e7eb;
  }
}
</style>
