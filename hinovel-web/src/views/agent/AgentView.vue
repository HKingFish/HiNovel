<template>
  <div class="agent-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>Agent 管理</h1>
        <p class="subtitle">创建和管理 AI Agent，配置模型参数和系统提示词</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="create-btn" @click="openCreateDialog">
          <el-icon>
            <Plus/>
          </el-icon>
          <span>新建 Agent</span>
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated/>
    </div>

    <div v-else-if="userAgents.length === 0" class="empty-state">
      <el-empty description="暂无 Agent 配置">
        <el-button type="primary" @click="openCreateDialog">
          创建第一个 Agent
        </el-button>
      </el-empty>
    </div>

    <div v-else class="agent-grid">
      <div
          v-for="agent in userAgents"
          :key="agent.id"
          class="agent-card"
      >
        <div class="card-header">
          <div class="agent-icon">
            <el-icon :size="20">
              <Cpu/>
            </el-icon>
          </div>
          <div class="agent-title">
            <h3>{{ agent.name }}</h3>
          </div>
          <el-dropdown trigger="click"
                       @command="(cmd: string) => handleCommand(cmd, agent)">
            <el-button circle size="small" class="more-btn">
              <el-icon>
                <More/>
              </el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">
                  <el-icon>
                    <Edit/>
                  </el-icon>
                  <span>编辑</span>
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  <el-icon color="#ef4444">
                    <Delete/>
                  </el-icon>
                  <span style="color: #ef4444;">删除</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="info-label">描述</span>
            <span class="info-value">{{ agent.description || '暂无描述' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">模型</span>
            <div class="model-info">
              <span class="provider-name">{{ getProviderName(agent.llmProviderId) }}</span>
              <el-tag size="small" effect="plain" type="info">{{ agent.modelName || '未配置' }}</el-tag>
            </div>
          </div>
          <div class="info-row">
            <span class="info-label">参数</span>
            <div class="param-tags">
              <el-tag size="small" effect="plain">T: {{ agent.temperature ?? 0.7 }}</el-tag>
              <el-tag size="small" effect="plain">Tokens: {{ agent.maxTokens ?? 2048 }}</el-tag>
            </div>
          </div>
        </div>

        <div class="card-footer">
          <span class="footer-time">
            更新于 {{ formatDateTime(agent.updateTime) }}
          </span>
        </div>
      </div>
    </div>

    <el-dialog
        v-model="showFormDialog"
        :title="isEditing ? '编辑 Agent' : '新建 Agent'"
        width="650px"
        destroy-on-close
        align-center
        class="agent-edit-dialog"
    >
      <div class="agent-form-scroll">
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-position="top"
          class="compact-form"
      >
        <el-form-item v-if="defaultConfigs.length > 0" label="套用模板" class="apply-template-item">
          <el-select
              placeholder="选择模板快速填充配置"
              style="width: 100%"
              size="default"
              clearable
              @change="handleApplyTemplate"
          >
            <el-option
                v-for="config in defaultConfigs"
                :key="config.id"
                :label="config.name"
                :value="config.id"
            />
          </el-select>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Agent 名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入 Agent 名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="描述">
              <el-input v-model="formData.description" placeholder="描述这个 Agent 的用途"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="LLM 提供方" prop="llmProviderId">
              <el-select
                  v-model="formData.llmProviderId"
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="provider in llmProviders"
                    :key="provider.id"
                    :label="provider.name"
                    :value="provider.id"
                >
                  <div class="provider-option">
                    <span class="provider-name">{{ provider.name }}</span>
                    <span class="provider-type">{{ provider.providerType }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模型名称" prop="modelName">
              <el-select
                  v-model="formData.modelName"
                  placeholder="请先选择提供方"
                  style="width: 100%"
                  :disabled="!formData.llmProviderId"
                  allow-create
                  filterable
                  clearable
              >
                <el-option
                    v-for="model in currentProviderModels"
                    :key="model"
                    :label="model"
                    :value="model"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="自定义模型地址">
          <el-input
              v-model="formData.customBaseUrl"
              placeholder="可选，覆盖提供方默认地址"
              size="medium"
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item>
              <template #label>
                <span class="label-with-tip">Temperature
                  <el-tooltip content="范围 0~2，值越高输出越随机，越低越确定" placement="top">
                    <el-icon class="label-tip-icon"><InfoFilled/></el-icon>
                  </el-tooltip>
                </span>
              </template>
              <el-input-number v-model="formData.temperature" :min="0" :max="2" :step="0.1"
                               :controls="false" style="width: 100%" size="small"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <template #label>
                <span class="label-with-tip">Max Tokens
                  <el-tooltip content="范围 1~32768，单次生成的最大 Token 数" placement="top">
                    <el-icon class="label-tip-icon"><InfoFilled/></el-icon>
                  </el-tooltip>
                </span>
              </template>
              <el-input-number v-model="formData.maxTokens" :min="1" :max="32768" :step="256"
                               :controls="false" style="width: 100%" size="small"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <template #label>
                <span class="label-with-tip">Top-P
                  <el-tooltip content="范围 0~1，核采样概率，与 Temperature 配合控制输出多样性" placement="top">
                    <el-icon class="label-tip-icon"><InfoFilled/></el-icon>
                  </el-tooltip>
                </span>
              </template>
              <el-input-number v-model="formData.topP" :min="0" :max="1" :step="0.05"
                               :controls="false" style="width: 100%" size="small"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <template #label>
                <span class="label-with-tip">最大迭代
                  <el-tooltip content="范围 1~50，Agent 执行工具调用的最大循环次数" placement="top">
                    <el-icon class="label-tip-icon"><InfoFilled/></el-icon>
                  </el-tooltip>
                </span>
              </template>
              <el-input-number v-model="formData.maxIterations" :min="1" :max="50" :step="1"
                               :controls="false" style="width: 100%" size="small"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="系统提示词" prop="systemPrompt">
          <el-input
              v-model="formData.systemPrompt"
              type="textarea"
              :rows="8"
              placeholder="定义这个 Agent 的角色和行为..."
          />
        </el-form-item>
      </el-form>
      </div>

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
import {computed, onMounted, ref, watch} from 'vue'
import {Cpu, Delete, Edit, InfoFilled, More, Plus} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {
  type AgentConfig,
  type AgentRequestDto,
  createAgent as createAgentApi,
  deleteAgentById,
  getAgentDefaultConfigs,
  getAvailableAgentList,
  getLlmProviderList,
  type LlmProviderOption,
  updateAgent as updateAgentApi
} from '@/api/agent'

const loading = ref(false)
const agents = ref<AgentConfig[]>([])
const llmProviders = ref<LlmProviderOption[]>([])
const defaultConfigs = ref<AgentConfig[]>([])

const showFormDialog = ref(false)
const isEditing = ref(false)
const editingAgent = ref<AgentConfig | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const formData = ref<AgentRequestDto>({
  name: '',
  description: '',
  systemPrompt: '',
  llmProviderId: undefined,
  modelName: '',
  customBaseUrl: '',
  temperature: 0.7,
  maxTokens: 2048,
  topP: 1.0,
  maxIterations: 10
})

const formRules: FormRules = {
  name: [
    {required: true, message: '请输入 Agent 名称', trigger: 'blur'},
    {min: 2, max: 100, message: '名称长度在 2 到 100 个字符', trigger: 'blur'}
  ],
  systemPrompt: [
    {required: true, message: '请输入系统提示词', trigger: 'blur'}
  ],
  modelName: [
    {required: true, message: '请选择或输入模型名称', trigger: 'blur'}
  ]
}

const currentProviderModels = computed(() => {
  if (!formData.value.llmProviderId) {
    return []
  }
  const provider = llmProviders.value.find(p => p.id === formData.value.llmProviderId)
  return provider?.modelList || []
})

watch(() => formData.value.llmProviderId, (newVal, oldVal) => {
  if (oldVal !== undefined && newVal !== oldVal) {
    formData.value.modelName = ''
  }
})

const isBuiltinAgent = (agent: AgentConfig): boolean => {
  return agent.isBuiltin === 1
}

/**
 * 用户自建的 Agent 列表（过滤掉内置模板，内置 Agent 仅作为创建时的配置模板）
 */
const userAgents = computed(() => {
  return agents.value.filter(a => !isBuiltinAgent(a))
})

const getProviderName = (providerId?: number): string => {
  if (!providerId) return '未配置'
  const provider = llmProviders.value.find(p => p.id === providerId)
  return provider ? provider.name : `提供方#${providerId}`
}

const formatDateTime = (date: string): string => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

const loadAgents = async () => {
  loading.value = true
  try {
    const [agentList, providerList, configList] = await Promise.all([
      getAvailableAgentList(),
      getLlmProviderList(),
      getAgentDefaultConfigs()
    ])
    agents.value = agentList
    llmProviders.value = providerList
    defaultConfigs.value = configList
  } catch (error) {
    console.error('加载 Agent 列表失败', error)
    ElMessage.error('加载 Agent 列表失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  openCreateDialogWithDefaults()
}

/**
 * 在编辑/新建表单中套用模板（保留当前的 LLM 提供方和模型配置）
 */
const handleApplyTemplate = (configId: number) => {
  if (!configId) return
  const config = defaultConfigs.value.find(c => c.id === configId)
  if (!config) return
  // 覆盖名称、描述、提示词和参数，保留模型配置不变
  formData.value.name = config.name
  formData.value.description = config.description || ''
  formData.value.systemPrompt = config.systemPrompt || ''
  formData.value.temperature = config.temperature ?? 0.7
  formData.value.maxTokens = config.maxTokens ?? 2048
  formData.value.topP = config.topP ?? 1.0
  formData.value.maxIterations = config.maxIterations ?? 10
  ElMessage.success('已套用模板「' + config.name + '」的配置')
}

/**
 * 打开空白创建弹窗
 */
const openCreateDialogWithDefaults = () => {
  isEditing.value = false
  editingAgent.value = null
  formData.value = {
    name: '',
    description: '',
    systemPrompt: '',
    llmProviderId: undefined,
    modelName: '',
    customBaseUrl: '',
    temperature: 0.7,
    maxTokens: 2048,
    topP: 1.0,
    maxIterations: 10
  }
  showFormDialog.value = true
}

const openEditDialog = (agent: AgentConfig) => {
  isEditing.value = true
  editingAgent.value = agent
  formData.value = {
    name: agent.name,
    description: agent.description || '',
    systemPrompt: agent.systemPrompt || '',
    llmProviderId: agent.llmProviderId,
    modelName: agent.modelName || '',
    customBaseUrl: agent.customBaseUrl || '',
    temperature: agent.temperature ?? 0.7,
    maxTokens: agent.maxTokens ?? 2048,
    topP: agent.topP ?? 1.0,
    maxIterations: agent.maxIterations ?? 10
  }
  showFormDialog.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingAgent.value) {
      await updateAgentApi(editingAgent.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createAgentApi(formData.value)
      ElMessage.success('创建成功')
    }
    showFormDialog.value = false
    await loadAgents()
  } catch (error) {
    console.error('保存 Agent 失败', error)
    ElMessage.error(editingAgent.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

const handleCommand = async (command: string, agent: AgentConfig) => {
  switch (command) {
    case 'edit':
      openEditDialog(agent)
      break
    case 'delete':
      await confirmDelete(agent)
      break
  }
}

const confirmDelete = async (agent: AgentConfig) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除 Agent "${agent.name}" 吗？`,
        '确认删除',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    await deleteAgentById(agent.id)
    ElMessage.success('删除成功')
    await loadAgents()
  } catch {
    // 用户取消删除
  }
}

/**
 * 根据图标标识获取对应的图标组件（保留用于后续扩展）
 */

onMounted(() => {
  loadAgents()
})
</script>

<style scoped lang="scss">
.agent-page {
  padding: 24px;
  padding-bottom: 40px;
  min-width: 900px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0;
  background: transparent;
}

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

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
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

.agent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.agent-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
  transition: box-shadow 0.2s, border-color 0.2s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    border-color: #d1d5db;
  }

  &.is-builtin {
    border-color: #dcfce7;
    background: linear-gradient(to bottom, #f0fdf4 0%, #fff 100%);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.agent-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  flex-shrink: 0;
}

.agent-title {
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
  min-width: 40px;
}

.info-value {
  color: #4b5563;
  word-break: break-all;
}

.model-info {
  display: flex;
  align-items: center;
  gap: 8px;

  .provider-name {
    color: #6b7280;
    font-size: 13px;
  }
}

.param-tags {
  display: flex;
  gap: 6px;
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

.provider-option {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .provider-name {
    font-weight: 500;
  }

  .provider-type {
    font-size: 12px;
    color: #9ca3af;
  }
}

.form-hint {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

/* 紧凑表单样式 */
.agent-form-scroll {
  max-height: 60vh;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 8px;
}

.compact-form .el-form-item {
  margin-bottom: 14px;
}

.compact-form .el-form-item__label {
  padding-bottom: 4px;
  font-size: 13px;
}

.apply-template-item {
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px dashed #e5e7eb;
}

.label-with-tip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.label-tip-icon {
  font-size: 14px;
  color: #c0c4cc;
  cursor: help;
  vertical-align: middle;

  &:hover {
    color: #909399;
  }
}

</style>
