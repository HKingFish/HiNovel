<template>
  <div class="llm-provider-tab">
    <!-- 操作栏 -->
    <div class="tab-actions">
      <el-button type="primary" class="create-btn" @click="openCreateDialog">
        <el-icon><Plus/></el-icon>
        <span>新增提供方</span>
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated/>
    </div>

    <!-- 空状态 -->
    <div v-else-if="providers.length === 0" class="empty-state">
      <el-empty description="暂无模型">
        <el-button type="primary" @click="openCreateDialog">添加第一个模型</el-button>
      </el-empty>
    </div>

    <!-- 提供方卡片列表 -->
    <div v-else class="provider-grid">
      <div
          v-for="provider in providers"
          :key="provider.id"
          class="provider-card"
          :class="{ 'is-inactive': provider.isActive !== 1 }"
      >
        <div class="card-header">
          <div class="provider-icon" :style="{ background: getProviderColor(provider.providerType) }">
            {{ getProviderIcon(provider.providerType) }}
          </div>
          <div class="provider-title">
            <h3>{{ provider.name }}</h3>
            <el-tag :type="provider.isActive === 1 ? 'success' : 'info'" size="small">
              {{ provider.isActive === 1 ? '已激活' : '未激活' }}
            </el-tag>
          </div>
          <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, provider)">
            <el-button circle size="small" class="more-btn">
              <el-icon><More/></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">
                  <el-icon><Edit/></el-icon>
                  <span>编辑</span>
                </el-dropdown-item>
                <el-dropdown-item v-if="provider.isActive === 1" command="deactivate">
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
            <span class="info-label">类型</span>
            <el-tag size="small" effect="plain">{{ provider.providerType }}</el-tag>
          </div>
          <div class="info-row">
            <span class="info-label">接口地址</span>
            <span class="info-value url-value">{{ provider.baseUrl || '未配置' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">API 密钥</span>
            <span class="info-value">{{ maskApiKey(provider.apiKey) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">模型列表</span>
            <div class="model-tags">
              <el-tag
                  v-for="model in parseModels(provider.models)"
                  :key="model"
                  size="small"
                  effect="plain"
                  type="info"
                  class="model-tag"
              >
                {{ model }}
              </el-tag>
              <span v-if="parseModels(provider.models).length === 0" class="info-value">未配置</span>
            </div>
          </div>
        </div>

        <div class="card-footer">
          <span class="footer-time">创建于 {{ formatTime(provider.createTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 创建/编辑弹窗 -->
    <el-dialog
        v-model="showFormDialog"
        :title="isEditing ? '编辑提供方' : '新增提供方'"
        width="560px"
        align-center
        @closed="resetForm"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
          label-position="top"
      >
        <el-form-item label="提供方名称" prop="name">
          <el-input
              v-model="formData.name"
              placeholder="例如：我的 OpenAI、公司 DeepSeek"
              maxlength="64"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="提供方类型" prop="providerType">
          <el-select
              v-model="formData.providerType"
              placeholder="选择提供方类型"
              class="full-width"
              @change="onProviderTypeChange"
          >
            <el-option
                v-for="type in providerTypes"
                :key="type.code"
                :label="type.displayName"
                :value="type.code"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="API 基础地址" prop="baseUrl">
          <el-input v-model="formData.baseUrl" placeholder="例如：https://api.openai.com/v1"/>
        </el-form-item>

        <el-form-item label="API 密钥" prop="apiKey">
          <el-input
              v-model="formData.apiKey"
              type="password"
              show-password
              :placeholder="isEditing ? '留空则不修改原密钥' : '输入 API Key'"
          />
        </el-form-item>

        <el-form-item label="支持的模型">
          <div class="models-input-area">
            <div class="models-tags">
              <el-tag
                  v-for="(model, index) in formData.models"
                  :key="index"
                  closable
                  @close="removeModel(index)"
                  class="model-edit-tag"
              >
                {{ model }}
              </el-tag>
            </div>
            <div class="model-add-row">
              <el-input
                  v-model="newModelName"
                  placeholder="输入模型名称，回车添加"
                  size="small"
                  @keyup.enter="addModel"
              />
              <el-button size="small" type="primary" @click="addModel">添加</el-button>
            </div>
          </div>
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
  activateLlmProvider,
  createLlmProvider,
  deactivateLlmProvider,
  deleteLlmProvider,
  getLlmProviderList,
  getLlmProviderTypes,
  type LlmProvider,
  type LlmProviderRequest,
  type ProviderType,
  updateLlmProvider
} from '@/api/llm-provider'

const providers = ref<LlmProvider[]>([])
const loading = ref(false)
const providerTypes = ref<ProviderType[]>([])

const showFormDialog = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const newModelName = ref('')

const formData = ref<LlmProviderRequest>({
  name: '',
  providerType: '',
  baseUrl: '',
  apiKey: '',
  models: []
})

const formRules: FormRules = {
  name: [{required: true, message: '请输入提供方名称', trigger: 'blur'}],
  providerType: [{required: true, message: '请选择提供方类型', trigger: 'change'}],
  baseUrl: [{required: true, message: '请输入 API 基础地址', trigger: 'blur'}],
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

/** 提供方类型对应的卡片颜色 */
const PROVIDER_COLOR_MAP: Record<string, string> = {
  OPENAI: 'linear-gradient(135deg, #10a37f 0%, #1a7f64 100%)',
  QWEN: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
  DEEPSEEK: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
  GLM: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)'
}

/** 提供方类型对应的图标文字 */
const PROVIDER_ICON_MAP: Record<string, string> = {
  OPENAI: 'AI',
  QWEN: 'QW',
  DEEPSEEK: 'DS',
  GLM: 'GL'
}

/** 加载提供方列表 */
const loadProviders = async () => {
  loading.value = true
  try {
    providers.value = await getLlmProviderList()
  } catch {
    ElMessage.error('加载提供方列表失败')
  } finally {
    loading.value = false
  }
}

/** 加载提供方类型 */
const loadProviderTypes = async () => {
  try {
    providerTypes.value = await getLlmProviderTypes()
  } catch {
    providerTypes.value = []
  }
}

/** 提供方类型变更时，自动填充官方 API 地址 */
const onProviderTypeChange = (typeCode: string) => {
  if (!formData.value.baseUrl || formData.value.baseUrl.trim() === '') {
    const selectedType = providerTypes.value.find(t => t.code === typeCode)
    if (selectedType) {
      formData.value.baseUrl = selectedType.officialBaseUrl
    }
  }
}

const getProviderColor = (type: string): string => {
  return PROVIDER_COLOR_MAP[type] || 'linear-gradient(135deg, #6b7280 0%, #4b5563 100%)'
}

const getProviderIcon = (type: string): string => {
  return PROVIDER_ICON_MAP[type] || type.substring(0, 2)
}

const maskApiKey = (apiKey: string): string => {
  if (!apiKey) return '未配置'
  if (apiKey.length <= 8) return '****'
  return apiKey.substring(0, 4) + '****' + apiKey.substring(apiKey.length - 4)
}

const parseModels = (models: string): string[] => {
  if (!models || models === '[]') return []
  return models.split(',').map(m => m.trim()).filter(m => m.length > 0)
}

const formatTime = (time: string): string => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'})
}

const openCreateDialog = () => {
  isEditing.value = false
  editingId.value = null
  formData.value = {name: '', providerType: '', baseUrl: '', apiKey: '', models: []}
  showFormDialog.value = true
}

const openEditDialog = (provider: LlmProvider) => {
  isEditing.value = true
  editingId.value = provider.id
  formData.value = {
    name: provider.name,
    providerType: provider.providerType,
    baseUrl: provider.baseUrl,
    apiKey: '',
    models: parseModels(provider.models)
  }
  showFormDialog.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  newModelName.value = ''
}

const addModel = () => {
  const name = newModelName.value.trim()
  if (!name) return
  if (formData.value.models.includes(name)) {
    ElMessage.warning('该模型已存在')
    return
  }
  formData.value.models.push(name)
  newModelName.value = ''
}

const removeModel = (index: number) => {
  formData.value.models.splice(index, 1)
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEditing.value && editingId.value) {
      await updateLlmProvider(editingId.value, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createLlmProvider(formData.value)
      ElMessage.success('创建成功')
    }
    showFormDialog.value = false
    await loadProviders()
  } catch {
    ElMessage.error(isEditing.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

const handleCommand = async (command: string, provider: LlmProvider) => {
  switch (command) {
    case 'edit':
      openEditDialog(provider)
      break
    case 'activate':
      await toggleActive(provider.id, true)
      break
    case 'deactivate':
      await toggleActive(provider.id, false)
      break
    case 'delete':
      await confirmDelete(provider)
      break
  }
}

const toggleActive = async (id: number, activate: boolean) => {
  try {
    if (activate) {
      await activateLlmProvider(id)
      ElMessage.success('已激活')
    } else {
      await deactivateLlmProvider(id)
      ElMessage.success('已停用')
    }
    await loadProviders()
  } catch {
    ElMessage.error('操作失败')
  }
}

const confirmDelete = async (provider: LlmProvider) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除提供方「${provider.name}」吗？删除后不可恢复。`,
        '确认删除',
        {confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'el-button--danger'}
    )
    await deleteLlmProvider(provider.id)
    ElMessage.success('删除成功')
    await loadProviders()
  } catch {
    // 用户取消删除
  }
}

onMounted(async () => {
  await Promise.all([loadProviders(), loadProviderTypes()])
})
</script>

<style scoped lang="scss">
.llm-provider-tab {
  padding-top: 8px;
}

.tab-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.create-btn {
  background: linear-gradient(135deg, #6366f1 0%, #7c3aed 100%);
  border: none;
  color: #fff;
  font-weight: 500;
  border-radius: 8px;
  padding: 10px 18px;
  height: auto;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);

  .el-icon {
    font-size: 16px;
    margin-right: 6px;
  }

  &:hover {
    background: linear-gradient(135deg, #5a5ce6 0%, #6d35d1 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
  }
}

.loading-state,
.empty-state {
  padding: 60px 0;
}

.provider-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.provider-card {
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

.provider-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.provider-title {
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

.model-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.model-tag {
  font-size: 11px;
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

.models-input-area {
  width: 100%;
}

.models-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.model-edit-tag {
  font-size: 13px;
}

.model-add-row {
  display: flex;
  gap: 8px;
}
</style>
