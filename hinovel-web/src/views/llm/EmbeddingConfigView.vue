<template>
  <div class="embedding-config-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>嵌入式模型</h1>
        <p class="subtitle">配置用于向量存储的 Embedding 模型，每个用户独立配置，互不影响</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="create-btn" @click="openCreateDialog">
          <el-icon><Plus/></el-icon>
          <span>新增配置</span>
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated/>
    </div>

    <!-- 空状态 -->
    <div v-else-if="configs.length === 0" class="empty-state">
      <el-empty description="暂无嵌入式模型配置">
        <el-button type="primary" @click="openCreateDialog">添加第一个配置</el-button>
      </el-empty>
    </div>

    <!-- 配置卡片列表 -->
    <div v-else class="config-grid">
      <div
          v-for="config in configs"
          :key="config.id"
          class="config-card"
          :class="{ 'is-inactive': config.isActive !== 1 }"
      >
        <div class="card-header">
          <div class="config-icon">EM</div>
          <div class="config-title">
            <h3>{{ config.name }}</h3>
            <el-tag :type="config.isActive === 1 ? 'success' : 'info'" size="small">
              {{ config.isActive === 1 ? '已激活' : '未激活' }}
            </el-tag>
          </div>
          <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, config)">
            <el-button circle size="small" class="more-btn">
              <el-icon><More/></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">
                  <el-icon><Edit/></el-icon>
                  <span>编辑</span>
                </el-dropdown-item>
                <el-dropdown-item v-if="config.isActive === 1" command="deactivate">
                  <el-icon><CircleClose/></el-icon>
                  <span>停用</span>
                </el-dropdown-item>
                <el-dropdown-item v-else command="activate">
                  <el-icon><CircleCheck/></el-icon>
                  <span>激活</span>
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  <el-icon color="#ef4444"><Delete/></el-icon>
                  <span style="color: #ef4444;">删除</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="info-label">模型类型</span>
            <el-tag size="small" effect="plain">{{ config.modelType?.toUpperCase() }}</el-tag>
          </div>
          <div class="info-row">
            <span class="info-label">接口地址</span>
            <span class="info-value url-value">{{ config.baseUrl || '未配置' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">模型名称</span>
            <span class="info-value">{{ config.modelName || '未配置' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">向量维度</span>
            <span class="info-value">{{ config.dimensions }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">API 密钥</span>
            <span class="info-value">{{ maskApiKey(config.apiKey) }}</span>
          </div>
        </div>

        <div class="card-footer">
          <span class="footer-time">创建于 {{ formatTime(config.createTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 创建/编辑弹窗 -->
    <el-dialog
        v-model="showFormDialog"
        :title="isEditing ? '编辑嵌入式模型配置' : '新增嵌入式模型配置'"
        width="520px"
        align-center
        @closed="resetForm"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-position="top"
      >
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="formData.name" placeholder="例如：阿里云百炼 Embedding" maxlength="64" show-word-limit/>
        </el-form-item>

        <el-form-item label="模型类型" prop="modelType">
          <el-select v-model="formData.modelType" placeholder="选择模型类型" class="full-width">
            <el-option label="OpenAI 兼容接口（推荐）" value="openai"/>
            <el-option label="本地 ONNX 模型" value="onnx"/>
          </el-select>
        </el-form-item>

        <el-form-item label="API 基础地址" prop="baseUrl">
          <el-input v-model="formData.baseUrl" placeholder="例如：https://dashscope.aliyuncs.com/compatible-mode/v1"/>
        </el-form-item>

        <el-form-item label="API 密钥" prop="apiKey">
          <el-input
              v-model="formData.apiKey"
              type="password"
              show-password
              :placeholder="isEditing ? '留空则不修改原密钥' : '输入 API Key'"
          />
        </el-form-item>

        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="formData.modelName" placeholder="例如：text-embedding-v3"/>
        </el-form-item>

        <el-form-item label="向量维度" prop="dimensions">
          <el-input-number v-model="formData.dimensions" :min="64" :max="4096" :step="64" class="full-width"/>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showFormDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ isEditing ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {CircleCheck, CircleClose, Delete, Edit, More, Plus} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {
  activateEmbeddingConfig,
  createEmbeddingConfig,
  deactivateEmbeddingConfig,
  deleteEmbeddingConfig,
  type EmbeddingConfig,
  type EmbeddingConfigRequest,
  getEmbeddingConfigList,
  updateEmbeddingConfig
} from '@/api/embedding-config'

const configs = ref<EmbeddingConfig[]>([])
const loading = ref(false)

const showFormDialog = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const formData = ref<EmbeddingConfigRequest>({
  name: '',
  modelType: 'openai',
  baseUrl: '',
  apiKey: '',
  modelName: '',
  dimensions: 1024
})

const formRules: FormRules = {
  name: [{required: true, message: '请输入配置名称', trigger: 'blur'}],
  modelType: [{required: true, message: '请选择模型类型', trigger: 'change'}],
  baseUrl: [{required: true, message: '请输入 API 基础地址', trigger: 'blur'}],
  modelName: [{required: true, message: '请输入模型名称', trigger: 'blur'}],
  dimensions: [{required: true, message: '请输入向量维度', trigger: 'blur'}],
  apiKey: [
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (!isEditing.value && (!value || value.trim() === '')) {
          callback(new Error('请输入 API 密钥'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadConfigs = async () => {
  loading.value = true
  try {
    configs.value = await getEmbeddingConfigList()
  } catch {
    ElMessage.error('加载配置列表失败')
  } finally {
    loading.value = false
  }
}

const maskApiKey = (apiKey: string): string => {
  if (!apiKey) return '未配置'
  if (apiKey.length <= 8) return '****'
  return apiKey.substring(0, 4) + '****' + apiKey.substring(apiKey.length - 4)
}

const formatTime = (time: string): string => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'})
}

const openCreateDialog = () => {
  isEditing.value = false
  editingId.value = null
  formData.value = {name: '', modelType: 'openai', baseUrl: '', apiKey: '', modelName: '', dimensions: 1024}
  showFormDialog.value = true
}

const openEditDialog = (config: EmbeddingConfig) => {
  isEditing.value = true
  editingId.value = config.id
  formData.value = {
    name: config.name,
    modelType: config.modelType,
    baseUrl: config.baseUrl,
    apiKey: '',
    modelName: config.modelName,
    dimensions: config.dimensions
  }
  showFormDialog.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEditing.value && editingId.value) {
      await updateEmbeddingConfig(editingId.value, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createEmbeddingConfig(formData.value)
      ElMessage.success('创建成功')
    }
    showFormDialog.value = false
    await loadConfigs()
  } catch {
    ElMessage.error(isEditing.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

const handleCommand = async (command: string, config: EmbeddingConfig) => {
  switch (command) {
    case 'edit':
      openEditDialog(config)
      break
    case 'activate':
      await toggleActive(config.id, true)
      break
    case 'deactivate':
      await toggleActive(config.id, false)
      break
    case 'delete':
      await confirmDelete(config)
      break
  }
}

const toggleActive = async (id: number, activate: boolean) => {
  try {
    if (activate) {
      await activateEmbeddingConfig(id)
      ElMessage.success('已激活')
    } else {
      await deactivateEmbeddingConfig(id)
      ElMessage.success('已停用')
    }
    await loadConfigs()
  } catch {
    ElMessage.error('操作失败')
  }
}

const confirmDelete = async (config: EmbeddingConfig) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除配置「${config.name}」吗？删除后不可恢复。`,
        '确认删除',
        {confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'el-button--danger'}
    )
    await deleteEmbeddingConfig(config.id)
    ElMessage.success('删除成功')
    await loadConfigs()
  } catch {
    // 用户取消
  }
}

onMounted(loadConfigs)
</script>

<style scoped lang="scss">
.embedding-config-page {
  padding: 24px;
  padding-bottom: 40px;
  min-width: 900px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

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

.create-btn {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border: none;
  color: #fff;
  font-weight: 500;
  border-radius: 8px;
  padding: 10px 18px;
  height: auto;
  box-shadow: 0 2px 8px rgba(14, 165, 233, 0.3);

  .el-icon {
    font-size: 16px;
    margin-right: 6px;
  }

  &:hover {
    background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(14, 165, 233, 0.4);
  }
}

.loading-state, .empty-state {
  padding: 60px 0;
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.config-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
  transition: box-shadow 0.2s, border-color 0.2s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    border-color: #d1d5db;
  }

  &.is-inactive {
    opacity: 0.65;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.config-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.config-title {
  flex: 1;
  min-width: 0;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 4px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.more-btn {
  border: none;
  color: #9ca3af;

  &:hover {
    color: #6b7280;
    background: #f3f4f6;
  }
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
}

.info-label {
  color: #9ca3af;
  flex-shrink: 0;
  min-width: 60px;
}

.info-value {
  color: #4b5563;
  word-break: break-all;
}

.url-value {
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
}

.card-footer {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.footer-time {
  font-size: 12px;
  color: #9ca3af;
}

.full-width {
  width: 100%;
}
</style>
