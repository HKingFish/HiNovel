<template>
  <div class="user-settings-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon>
            <Setting/>
          </el-icon>
        </div>
        <div class="header-title">
          <h1>默认设置</h1>
          <p class="subtitle">配置 AI 功能的全局默认行为，单个小说可覆盖这些设置</p>
        </div>
      </div>
    </div>

    <!-- 配置内容 -->
    <div v-loading="loading" class="settings-container">
      <el-form label-position="top" class="settings-form">
        <!-- 发布后自动操作 -->
        <div class="settings-card">
          <div class="card-header">
            <el-icon>
              <Upload/>
            </el-icon>
            <span>发布后自动操作</span>
          </div>
          <div class="card-body">
            <div class="setting-row">
              <div class="setting-info">
                <span class="setting-label">自动审核</span>
                <span class="setting-desc">改写完成后自动调用 AI 审核接口检查内容</span>
              </div>
              <el-switch
                  v-model="settingsForm.autoAuditAfterRewrite"
                  :active-value="1"
                  :inactive-value="0"
              />
            </div>
            <div class="setting-row">
              <div class="setting-info">
                <span class="setting-label">自动生成 AI 大纲</span>
                <span class="setting-desc">发布章节后自动调用 AI 总结章节大纲</span>
              </div>
              <el-switch
                  v-model="settingsForm.autoOutlineAfterPublish"
                  :active-value="1"
                  :inactive-value="0"
              />
            </div>
            <div class="setting-row">
              <div class="setting-info">
                <span class="setting-label">自动存储向量</span>
                <span class="setting-desc">发布章节后自动将内容存入向量数据库</span>
              </div>
              <el-switch
                  v-model="settingsForm.autoVectorAfterPublish"
                  :active-value="1"
                  :inactive-value="0"
              />
            </div>
            <div class="setting-row">
              <div class="setting-info">
                <span class="setting-label">自动保存内容</span>
                <span class="setting-desc">编辑器内容变化时自动保存到服务器，关闭后需手动点击保存</span>
              </div>
              <el-switch
                  v-model="settingsForm.autoSaveContent"
                  :active-value="1"
                  :inactive-value="0"
              />
            </div>
          </div>
        </div>

        <!-- AI 改写设置 -->
        <div class="settings-card">
          <div class="card-header">
            <el-icon>
              <MagicStick/>
            </el-icon>
            <span>AI 改写设置</span>
          </div>
          <div class="card-body">
            <div class="setting-row">
              <div class="setting-info">
                <span class="setting-label">携带前情提要章数</span>
                <span class="setting-desc">AI 改写时携带前几章内容作为前情提要（0 表示不携带）</span>
              </div>
              <el-input-number
                  v-model="settingsForm.rewriteContextChapters"
                  :min="0"
                  :max="10"
                  :step="1"
                  style="width: 140px"
              />
            </div>
            <div class="setting-row">
              <div class="setting-info">
                <span class="setting-label">改写时携带全文大纲</span>
                <span class="setting-desc">AI 改写时将全文大纲作为参考上下文</span>
              </div>
              <el-switch
                  v-model="settingsForm.rewriteIncludeOutline"
                  :active-value="1"
                  :inactive-value="0"
              />
            </div>
            <div class="setting-row">
              <div class="setting-info">
                <span class="setting-label">审核时携带全文大纲</span>
                <span class="setting-desc">AI 审核时将全文大纲作为参考上下文</span>
              </div>
              <el-switch
                  v-model="settingsForm.auditIncludeOutline"
                  :active-value="1"
                  :inactive-value="0"
              />
            </div>
          </div>
        </div>

        <!-- AI 问答设置 -->
        <div class="settings-card">
          <div class="card-header">
            <el-icon>
              <ChatDotRound/>
            </el-icon>
            <span>AI 问答设置</span>
          </div>
          <div class="card-body">
            <div class="setting-row">
              <div class="setting-info">
                <span class="setting-label">问答时携带全文大纲</span>
                <span class="setting-desc">AI 问答时将全文大纲作为参考上下文，帮助 AI 更准确地回答</span>
              </div>
              <el-switch
                  v-model="settingsForm.qaIncludeOutline"
                  :active-value="1"
                  :inactive-value="0"
              />
            </div>
            <div class="setting-row">
              <div class="setting-info">
                <span class="setting-label">历史消息条数</span>
                <span class="setting-desc">AI 问答时携带的历史对话条数（0 表示不携带）</span>
              </div>
              <el-input-number
                  v-model="settingsForm.qaContextLength"
                  :min="0"
                  :max="50"
                  :step="1"
                  style="width: 140px"
              />
            </div>
          </div>
        </div>
      </el-form>

      <!-- 底部操作栏 -->
      <div class="settings-footer">
        <div class="footer-tip">
          <el-icon>
            <InfoFilled/>
          </el-icon>
          <span>这些是全局默认配置，单个小说的设置会覆盖此处的配置</span>
        </div>
        <div class="footer-actions">
          <el-button @click="resetToDefault">恢复默认</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存设置
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {ChatDotRound, InfoFilled, MagicStick, Setting, Upload} from '@element-plus/icons-vue'
import {
  createDefaultSettings,
  getUserDefaultSettings,
  type NovelSettings,
  saveUserDefaultSettings
} from '@/api/novel-settings'

const loading = ref(false)
const saving = ref(false)
const settingsForm = ref<NovelSettings>(createDefaultSettings())

/**
 * 加载用户默认配置
 */
const loadSettings = async () => {
  loading.value = true
  try {
    const res = await getUserDefaultSettings()
    if (res.data) {
      settingsForm.value = res.data
    }
  } catch (error) {
    console.error('加载用户默认配置失败', error)
  } finally {
    loading.value = false
  }
}

/**
 * 保存用户默认配置
 */
const handleSave = async () => {
  saving.value = true
  try {
    await saveUserDefaultSettings(settingsForm.value)
    ElMessage.success('默认设置保存成功')
  } catch (error) {
    ElMessage.error('保存设置失败')
  } finally {
    saving.value = false
  }
}

/**
 * 恢复为系统默认值
 */
const resetToDefault = () => {
  settingsForm.value = createDefaultSettings()
  ElMessage.info('已恢复为默认值，请点击保存生效')
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped lang="scss">
.user-settings-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 14px;

  .el-icon {
    font-size: 28px;
    color: #fff;
  }
}

.header-title {
  h1 {
    font-size: 22px;
    font-weight: 600;
    color: #fff;
    margin: 0 0 4px;
  }

  .subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.85);
    margin: 0;
  }
}

.settings-container {
  min-height: 200px;
}

.settings-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8eaed;
  margin-bottom: 20px;
  overflow: hidden;
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 24px;
  background: #fafbfc;
  border-bottom: 1px solid #e8eaed;
  font-size: 15px;
  font-weight: 600;
  color: #374151;

  .el-icon {
    font-size: 18px;
    color: #6366f1;
  }
}

.card-body {
  padding: 8px 24px;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.setting-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.setting-desc {
  font-size: 12px;
  color: #9ca3af;
}

.settings-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8eaed;
}

.footer-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #9ca3af;

  .el-icon {
    font-size: 16px;
  }
}

.footer-actions {
  display: flex;
  gap: 12px;
}
</style>
