<template>
  <div class="novel-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>我的小说</h1>
        <p class="subtitle">管理和创作您的小说作品</p>
      </div>
      <div class="header-actions">
        <el-button-group class="view-switch">
          <el-button
              :type="viewMode === 'card' ? 'primary' : 'default'"
              @click="viewMode = 'card'"
          >
            <el-icon><Grid /></el-icon>
          </el-button>
          <el-button
              :type="viewMode === 'list' ? 'primary' : 'default'"
              @click="viewMode = 'list'"
          >
            <el-icon><List /></el-icon>
          </el-button>
        </el-button-group>
        <el-button type="primary" class="create-btn" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          <span>新建小说</span>
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon novels">
          <el-icon>
            <Reading/>
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ novels.length }}</span>
          <span class="stat-label">总作品</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon words">
          <el-icon>
            <EditPen/>
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ formatWordCount(totalWordCount) }}</span>
          <span class="stat-label">总字数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon chapters">
          <el-icon>
            <Document/>
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ totalChapters }}</span>
          <span class="stat-label">总章节</span>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="novels.length === 0" class="empty-state">
      <el-empty description="还没有小说">
        <template #image>
          <div class="empty-icon">
            <el-icon :size="64" color="#d1d5db"><Reading /></el-icon>
          </div>
        </template>
        <el-button type="primary" @click="openCreateDialog">
          创建第一部小说
        </el-button>
      </el-empty>
    </div>

    <!-- 卡片视图 -->
    <div v-else-if="viewMode === 'card'" class="novel-grid">
      <div 
        v-for="novel in novels" 
        :key="novel.id" 
        class="novel-card"
        @click="goToNovel(novel.id)"
      >
        <div class="novel-cover">
          <img v-if="novel.coverUrl" :src="novel.coverUrl" :alt="novel.title">
          <div v-else class="cover-placeholder">
            <div class="cover-icon-wrapper">
              <el-icon :size="32">
                <Reading/>
              </el-icon>
            </div>
            <span class="cover-text">NOVEL</span>
          </div>
          <div class="novel-status" :class="novel.status.toLowerCase()">
            <span class="status-dot"></span>
            {{ statusText(novel.status) }}
          </div>
        </div>
        <div class="novel-info">
          <div class="novel-header">
            <h3 class="novel-title">{{ novel.title }}</h3>
          </div>
          <p class="novel-desc">{{ novel.description || '暂无简介，点击编辑添加...' }}</p>

          <div class="novel-stats">
            <div class="stat-item chapters">
              <el-icon><Document /></el-icon>
              <span class="stat-value">{{ novel.chapterCount }}</span>
              <span class="stat-label">章节</span>
            </div>
            <div class="stat-item words">
              <el-icon><EditPen /></el-icon>
              <span class="stat-value">{{ formatWordCount(novel.wordCount) }}</span>
              <span class="stat-label">字数</span>
            </div>
          </div>

          <div class="novel-agents" v-if="novel.authorAgentName || novel.editorAgentName || novel.qaAgentName">
            <div class="agent-label">
              <el-icon class="pulse-icon">
                <Cpu/>
              </el-icon>
              <span>智能助手</span>
            </div>
            <div class="agent-list">
              <div v-if="novel.authorAgentName" class="agent-item author">
                <span class="agent-role">
                  <el-icon><EditPen/></el-icon>
                  作者
                </span>
                <span class="agent-name">{{ novel.authorAgentName }}</span>
              </div>
              <div v-if="novel.editorAgentName" class="agent-item editor">
                <span class="agent-role">
                  <el-icon><Edit/></el-icon>
                  编辑
                </span>
                <span class="agent-name">{{ novel.editorAgentName }}</span>
              </div>
              <div v-if="novel.qaAgentName" class="agent-item qa">
                <span class="agent-role">
                  <el-icon><ChatDotRound/></el-icon>
                  问答
                </span>
                <span class="agent-name">{{ novel.qaAgentName }}</span>
              </div>
            </div>
          </div>
          <div class="novel-agents empty" v-else>
            <div class="agent-label">
              <el-icon>
                <Cpu/>
              </el-icon>
              <span>暂未配置 Agent</span>
            </div>
          </div>

          <div class="novel-footer">
            <span class="time-item">
              <el-icon><Clock/></el-icon>
              {{ formatDate(novel.updateTime) }}
            </span>
            <div class="footer-actions">
              <el-button type="primary" size="small" class="write-btn" @click.stop="goToNovel(novel.id)">
                <el-icon>
                  <EditPen/>
                </el-icon>
                开始写作
              </el-button>
              <el-dropdown trigger="click">
                <el-button circle class="more-btn" @click.stop>
                  <el-icon>
                    <More/>
                  </el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="openEditDialog(novel)">
                      <el-icon>
                        <Edit/>
                      </el-icon>
                      <span>编辑信息</span>
                    </el-dropdown-item>
                    <el-dropdown-item @click="openAgentConfigDialog(novel)">
                      <el-icon>
                        <Cpu/>
                      </el-icon>
                      <span>小说设置</span>
                    </el-dropdown-item>
                    <el-dropdown-item @click="goToCharacters(novel.id)">
                      <el-icon>
                        <User/>
                      </el-icon>
                      <span>人物图谱</span>
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="confirmDelete(novel)">
                      <el-icon color="#ef4444">
                        <Delete/>
                      </el-icon>
                      <span style="color: #ef4444;">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <div v-else class="novel-list">
      <el-table :data="novels" style="width: 100%" @row-click="(row: Novel) => goToNovel(row.id)">
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <div class="list-cover">
              <img v-if="row.coverUrl" :src="row.coverUrl" :alt="row.title">
              <div v-else class="list-cover-placeholder">
                <el-icon :size="24" color="#d1d5db"><Reading /></el-icon>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="小说信息" min-width="250">
          <template #default="{ row }">
            <div class="list-info">
              <h4 class="list-title">{{ row.title }}</h4>
              <p class="list-desc">{{ row.description || '暂无简介' }}</p>
              <el-tag :type="statusType(row.status)" size="small">
                {{ statusText(row.status) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="统计" width="180">
          <template #default="{ row }">
            <div class="list-stats">
              <span>{{ row.chapterCount }} 章</span>
              <span>{{ formatWordCount(row.wordCount) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="Agent 配置" min-width="220">
          <template #default="{ row }">
            <div class="list-agents" v-if="row.authorAgentName || row.editorAgentName || row.qaAgentName">
              <div v-if="row.authorAgentName" class="list-agent-item author">
                <span class="list-agent-role">
                  <el-icon :size="10"><EditPen/></el-icon>
                  作者
                </span>
                <span class="list-agent-name">{{ row.authorAgentName }}</span>
              </div>
              <div v-if="row.editorAgentName" class="list-agent-item editor">
                <span class="list-agent-role">
                  <el-icon :size="10"><Edit/></el-icon>
                  编辑
                </span>
                <span class="list-agent-name">{{ row.editorAgentName }}</span>
              </div>
              <div v-if="row.qaAgentName" class="list-agent-item qa">
                <span class="list-agent-role">
                  <el-icon :size="10"><ChatDotRound/></el-icon>
                  问答
                </span>
                <span class="list-agent-name">{{ row.qaAgentName }}</span>
              </div>
            </div>
            <div class="list-agents-empty" v-else>
              <el-icon :size="12">
                <Cpu/>
              </el-icon>
              <span>暂未配置</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="220">
          <template #default="{ row }">
            <div class="list-dates">
              <div>创建：{{ formatDateTime(row.createTime) }}</div>
              <div>更新：{{ formatDateTime(row.updateTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" @click.stop="openEditDialog(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button size="small" type="primary" @click.stop="goToNovel(row.id)">
                <el-icon><EditPen /></el-icon>
              </el-button>
              <el-button size="small" @click.stop="openAgentConfigDialog(row)">
                <el-icon>
                  <Cpu/>
                </el-icon>
              </el-button>
              <el-button size="small" @click.stop="goToCharacters(row.id)">
                <el-icon><User /></el-icon>
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 小说设置弹窗（Agent 配置 + AI 功能配置） -->
    <el-dialog
      v-model="showAgentConfigDialog"
      title="小说设置"
      width="600px"
      destroy-on-close
    >
      <el-skeleton v-if="agentConfigLoading" :rows="6" animated/>
      <el-tabs v-else v-model="settingsActiveTab">
        <!-- Agent 配置 Tab -->
        <el-tab-pane label="Agent 配置" name="agent">
          <el-form label-position="top">
            <el-form-item label="作者 Agent">
              <el-select
                  v-model="agentConfig.authorAgentId"
                  placeholder="选择作者 Agent"
                  clearable
                  style="width: 100%"
              >
                <el-option
                    v-for="agent in userAgentList"
                    :key="agent.id"
                    :label="agent.name"
                    :value="agent.id"
                />
              </el-select>
              <div class="form-hint">用于辅助小说创作的 Agent</div>
            </el-form-item>

            <el-form-item label="编辑 Agent">
              <el-select
                  v-model="agentConfig.editorAgentId"
                  placeholder="选择编辑 Agent"
                  clearable
                  style="width: 100%"
              >
                <el-option
                    v-for="agent in userAgentList"
                    :key="agent.id"
                    :label="agent.name"
                    :value="agent.id"
                />
              </el-select>
              <div class="form-hint">用于审核和优化内容的 Agent</div>
            </el-form-item>

            <el-form-item label="问答 Agent">
              <el-select
                  v-model="agentConfig.qaAgentId"
                  placeholder="选择问答 Agent"
                  clearable
                  style="width: 100%"
              >
                <el-option
                    v-for="agent in userAgentList"
                    :key="agent.id"
                    :label="agent.name"
                    :value="agent.id"
                />
              </el-select>
              <div class="form-hint">用于基于小说内容的智能问答 Agent</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- AI 功能配置 Tab -->
        <el-tab-pane label="AI 功能配置" name="ai-settings">
          <el-alert
              v-if="!hasOwnSettings"
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom: 16px"
          >
            当前显示的是全局默认配置，保存后将创建该小说的独立配置
          </el-alert>
          <el-form label-position="top" class="ai-settings-form">
            <div class="settings-section">
              <div class="settings-section-title">发布后自动操作</div>
              <el-form-item>
                <div class="setting-row">
                  <div class="setting-info">
                    <span class="setting-label">自动审核</span>
                    <span class="setting-desc">改写完成后自动调用 AI 审核接口检查内容</span>
                  </div>
                  <el-switch v-model="novelSettingsForm.autoAuditAfterRewrite" :active-value="1" :inactive-value="0"/>
                </div>
              </el-form-item>
              <el-form-item>
                <div class="setting-row">
                  <div class="setting-info">
                    <span class="setting-label">自动生成 AI 大纲</span>
                    <span class="setting-desc">发布章节后自动调用 AI 总结章节大纲</span>
                  </div>
                  <el-switch v-model="novelSettingsForm.autoOutlineAfterPublish" :active-value="1" :inactive-value="0"/>
                </div>
              </el-form-item>
              <el-form-item>
                <div class="setting-row">
                  <div class="setting-info">
                    <span class="setting-label">自动存储向量</span>
                    <span class="setting-desc">发布章节后自动将内容存入向量数据库</span>
                  </div>
                  <el-switch v-model="novelSettingsForm.autoVectorAfterPublish" :active-value="1" :inactive-value="0"/>
                </div>
              </el-form-item>
            </div>

            <div class="settings-section">
              <div class="settings-section-title">AI 改写设置</div>
              <el-form-item label="携带前情提要章数">
                <el-input-number
                    v-model="novelSettingsForm.rewriteContextChapters"
                    :min="0"
                    :max="10"
                    :step="1"
                    style="width: 200px"
                />
                <div class="form-hint">AI 改写时携带前几章内容作为前情提要（0 表示不携带）</div>
              </el-form-item>
              <el-form-item>
                <div class="setting-row">
                  <div class="setting-info">
                    <span class="setting-label">改写时携带全文大纲</span>
                    <span class="setting-desc">AI 改写时将全文大纲作为参考上下文</span>
                  </div>
                  <el-switch v-model="novelSettingsForm.rewriteIncludeOutline" :active-value="1" :inactive-value="0"/>
                </div>
              </el-form-item>
              <el-form-item>
                <div class="setting-row">
                  <div class="setting-info">
                    <span class="setting-label">审核时携带全文大纲</span>
                    <span class="setting-desc">AI 审核时将全文大纲作为参考上下文</span>
                  </div>
                  <el-switch v-model="novelSettingsForm.auditIncludeOutline" :active-value="1" :inactive-value="0"/>
                </div>
              </el-form-item>
            </div>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="showAgentConfigDialog = false">取消</el-button>
        <el-button type="primary" :loading="agentConfigLoading" @click="handleSaveAllSettings">
          保存设置
        </el-button>
      </template>
    </el-dialog>

    <!-- 创建/编辑小说对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑小说' : '新建小说'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <!-- 封面上传 -->
        <el-form-item label="封面">
          <div class="cover-upload">
            <el-upload
              class="cover-uploader"
              action="/api/oss/upload"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :before-upload="beforeCoverUpload"
              accept="image/*"
            >
              <img v-if="form.coverUrl" :src="form.coverUrl" class="cover-preview">
              <div v-else class="cover-upload-placeholder">
                <el-icon :size="28"><Plus /></el-icon>
                <span>点击上传封面</span>
              </div>
            </el-upload>
            <div v-if="form.coverUrl" class="cover-actions">
              <el-button type="danger" size="small" @click="form.coverUrl = ''" class="delete-cover-btn">
                <el-icon><Delete /></el-icon>
                删除封面
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="小说标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入小说标题" />
        </el-form-item>

        <el-form-item label="简介" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="简要描述您的小说内容"
          />
        </el-form-item>

        <el-form-item v-if="isEdit" label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button label="ONGOING">连载中</el-radio-button>
            <el-radio-button label="COMPLETED">已完结</el-radio-button>
            <el-radio-button label="PAUSED">暂停</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 编辑时显示时间信息 -->
        <template v-if="isEdit && currentNovel">
          <el-divider />
          <div class="time-info">
            <div class="time-item">
              <el-icon><Calendar /></el-icon>
              <span>创建时间：{{ formatDateTime(currentNovel.createTime) }}</span>
            </div>
            <div class="time-item">
              <el-icon><Clock /></el-icon>
              <span>更新时间：{{ formatDateTime(currentNovel.updateTime) }}</span>
            </div>
          </div>
        </template>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {
  Calendar,
  ChatDotRound,
  Clock,
  Cpu,
  Delete,
  Document,
  Edit,
  EditPen,
  Grid,
  List,
  More,
  Plus,
  Reading,
  User
} from '@element-plus/icons-vue'
import {
  createNovel,
  type CreateNovelRequest,
  deleteNovel,
  getNovelAgentConfig,
  listMyNovels,
  type Novel,
  type NovelAgentConfig,
  type NovelAgentConfigRequest,
  saveNovelAgentConfig,
  updateNovel,
  type UpdateNovelRequest
} from '@/api/novel-platform'
import {type AgentConfig, getAvailableAgentList} from '@/api/agent'
import {
  createDefaultSettings,
  getEffectiveSettings,
  getNovelLevelSettings,
  type NovelSettings,
  saveNovelSettings
} from '@/api/novel-settings'

const router = useRouter()
const loading = ref(false)
const novels = ref<Novel[]>([])

// 视图模式（默认列表，从 localStorage 恢复用户偏好）
const VIEW_MODE_STORAGE_KEY = 'novel_list_view_mode'
const savedViewMode = localStorage.getItem(VIEW_MODE_STORAGE_KEY) as 'card' | 'list' | null
const viewMode = ref<'card' | 'list'>(savedViewMode || 'list')

// 监听视图模式变化，缓存到 localStorage
watch(viewMode, (newMode) => {
  localStorage.setItem(VIEW_MODE_STORAGE_KEY, newMode)
})

// 对话框
const showDialog = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const currentNovel = ref<Novel | null>(null)

const form = ref<CreateNovelRequest & { status?: string }>({
  title: '',
  description: '',
  coverUrl: '',
  status: 'ONGOING'
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入小说标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const agents = ref<AgentConfig[]>([])

const showAgentConfigDialog = ref(false)
const agentConfigLoading = ref(false)
const currentNovelForConfig = ref<Novel | null>(null)
const agentConfig = ref<NovelAgentConfig>({
  novelId: 0,
  authorAgentId: undefined,
  editorAgentId: undefined,
  qaAgentId: undefined
})

// 小说设置弹窗 Tab 状态
const settingsActiveTab = ref('agent')

// AI 功能配置表单
const novelSettingsForm = ref<NovelSettings>(createDefaultSettings())

// 标记当前小说是否有自身配置（false 表示继承自默认配置）
const hasOwnSettings = ref(false)

const totalWordCount = computed(() => {
  return novels.value.reduce((sum, novel) => sum + Number(novel.wordCount || 0), 0)
})

const totalChapters = computed(() => {
  return novels.value.reduce((sum, novel) => sum + Number(novel.chapterCount || 0), 0)
})

const statusText = (status: string) => {
  const map: Record<string, string> = {
    'ONGOING': '连载中',
    'COMPLETED': '已完结',
    'PAUSED': '暂停'
  }
  return map[status] || status
}

const statusType = (status: string) => {
  const map: Record<string, any> = {
    'ONGOING': 'primary',
    'COMPLETED': 'success',
    'PAUSED': 'info'
  }
  return map[status] || ''
}

const formatWordCount = (count: number | string) => {
  const num = Number(count) || 0
  if (!num) return '0 '
  if (num < 10000) return `${num} `
  return `${(num / 10000).toFixed(1)} 万`
}

const formatDate = (date: string) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

const formatDateTime = (date: string) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

const fetchNovels = async () => {
  loading.value = true
  try {
    const res = await listMyNovels()
    novels.value = res.data
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEdit.value = false
  currentNovel.value = null
  form.value = {
    title: '',
    description: '',
    coverUrl: '',
    status: 'ONGOING'
  }
  showDialog.value = true
}

const openEditDialog = (novel: Novel) => {
  isEdit.value = true
  currentNovel.value = novel
  form.value = {
    title: novel.title,
    description: novel.description || '',
    coverUrl: novel.coverUrl || '',
    status: novel.status
  }
  showDialog.value = true
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value && currentNovel.value) {
      await updateNovel(currentNovel.value.id, form.value as UpdateNovelRequest)
      ElMessage.success('更新成功')
    } else {
      const res = await createNovel(form.value)
      ElMessage.success('创建成功')
      goToNovel(res.data.id)
    }
    showDialog.value = false
    fetchNovels()
  } finally {
    saving.value = false
  }
}

// 封面上传
const handleCoverSuccess = (response: any) => {
  if (response.data?.url) {
    form.value.coverUrl = response.data.url
    ElMessage.success('封面上传成功')
  }
}

const beforeCoverUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const confirmDelete = async (novel: Novel) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除《${novel.title}》吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteNovel(novel.id)
    ElMessage.success('删除成功')
    fetchNovels()
  } catch {
    // 取消
  }
}

const goToNovel = (id: number) => {
  router.push(`/novel/${id}`)
}

const goToCharacters = (id: number) => {
  router.push(`/novel/${id}/characters`)
}

// 打开小说设置弹窗（同时加载 Agent 配置和 AI 功能配置）
const openAgentConfigDialog = async (novel: Novel) => {
  currentNovelForConfig.value = novel
  settingsActiveTab.value = 'agent'
  showAgentConfigDialog.value = true
  agentConfigLoading.value = true
  
  try {
    // 并行加载 Agent 配置和小说级别配置
    const [agentRes, settingsRes] = await Promise.all([
      getNovelAgentConfig(novel.id),
      getNovelLevelSettings(novel.id)
    ])
    if (agentRes.data) {
      agentConfig.value = agentRes.data
    }
    if (settingsRes.data) {
      // 小说有自身配置，直接使用
      novelSettingsForm.value = settingsRes.data
      hasOwnSettings.value = true
    } else {
      // 小说没有自身配置，加载有效配置（合并后的结果）展示
      hasOwnSettings.value = false
      try {
        const effectiveRes = await getEffectiveSettings(novel.id)
        novelSettingsForm.value = effectiveRes.data ?? createDefaultSettings()
      } catch {
        novelSettingsForm.value = createDefaultSettings()
      }
    }
  } catch (error) {
    ElMessage.error('获取小说设置失败')
  } finally {
    agentConfigLoading.value = false
  }
}

// 保存全部设置（Agent 配置 + AI 功能配置）
const handleSaveAllSettings = async () => {
  if (!currentNovelForConfig.value) return
  
  agentConfigLoading.value = true
  try {
    const novelId = currentNovelForConfig.value.id

    // 并行保存 Agent 配置和小说配置
    const agentRequest: NovelAgentConfigRequest = {
      authorAgentId: agentConfig.value.authorAgentId,
      editorAgentId: agentConfig.value.editorAgentId,
      qaAgentId: agentConfig.value.qaAgentId
    }
    const [agentRes] = await Promise.all([
      saveNovelAgentConfig(novelId, agentRequest),
      saveNovelSettings(novelId, novelSettingsForm.value)
    ])

    // 更新本地小说数据中的 Agent 信息
    if (agentRes.data) {
      const index = novels.value.findIndex(n => n.id === novelId)
      if (index !== -1) {
        novels.value[index] = {
          ...novels.value[index],
          authorAgentId: agentRes.data.authorAgentId,
          authorAgentName: agentRes.data.authorAgentName,
          editorAgentId: agentRes.data.editorAgentId,
          editorAgentName: agentRes.data.editorAgentName,
          qaAgentId: agentRes.data.qaAgentId,
          qaAgentName: agentRes.data.qaAgentName
        }
      }
    }
    ElMessage.success('小说设置保存成功')
    showAgentConfigDialog.value = false
  } catch (error) {
    ElMessage.error('保存小说设置失败')
  } finally {
    agentConfigLoading.value = false
  }
}

onMounted(() => {
  fetchNovels()
  loadAgents()
})

// 加载 Agent 列表（从后端接口获取）
const loadAgents = async () => {
  try {
    agents.value = await getAvailableAgentList()
  } catch (error) {
    console.error('加载 Agent 列表失败', error)
    agents.value = []
  }
}

// 判断是否为内置 Agent
const isBuiltinAgent = (agent: AgentConfig): boolean => {
  return agent.isBuiltin === 1
}

/**
 * 用户自建的 Agent 列表（过滤掉内置模板）
 */
const userAgentList = computed(() => {
  return agents.value.filter(a => !isBuiltinAgent(a))
})
</script>

<style scoped lang="scss">
.novel-list-page {
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

.view-switch {
  .el-button {
    padding: 10px 14px;

    .el-icon {
      font-size: 16px;
    }
  }
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

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #e8eaed;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;

  .el-icon {
    font-size: 24px;
  }

  &.novels {
    background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
    color: #6366f1;
  }

  &.words {
    background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
    color: #22c55e;
  }

  &.chapters {
    background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
    color: #f59e0b;
  }
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

.loading-state {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  border: 1px solid #e8eaed;
}

.empty-state {
  background: #fff;
  border-radius: 16px;
  padding: 80px 40px;
  border: 1px solid #e8eaed;
  text-align: center;
}

.empty-icon {
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
}

/* 卡片视图 */
.novel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}

.novel-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  display: flex;
  border: 1px solid #e8eaed;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #6366f1, #8b5cf6);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    box-shadow: 0 12px 24px -8px rgba(99, 102, 241, 0.15), 0 8px 16px -6px rgba(0, 0, 0, 0.05);
    transform: translateY(-4px);
    border-color: #c7d2fe;

    &::before {
      opacity: 1;
    }

    .novel-cover img {
      transform: scale(1.05);
    }
  }
}

.novel-cover {
  width: 110px;
  min-height: 160px;
  background: linear-gradient(145deg, #f5f7ff 0%, #e8ecff 50%, #dde2ff 100%);
  position: relative;
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    background: linear-gradient(145deg, #f5f7ff 0%, #e8ecff 50%, #dde2ff 100%);
  }

  .cover-icon-wrapper {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(99, 102, 241, 0.12) 0%, rgba(139, 92, 246, 0.12) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #6366f1;

    .el-icon {
      font-size: 20px;
    }
  }

  .cover-text {
    font-size: 9px;
    font-weight: 600;
    letter-spacing: 2px;
    color: #a5b4fc;
  }
}

.novel-status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  border-radius: 16px;
  font-size: 10px;
  font-weight: 600;
  backdrop-filter: blur(12px);
  letter-spacing: 0.3px;
  display: flex;
  align-items: center;
  gap: 5px;

  .status-dot {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    animation: pulse 2s infinite;
  }

  &.ongoing {
    background: rgba(255, 255, 255, 0.95);
    color: #6366f1;
    box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);

    .status-dot {
      background: #6366f1;
    }
  }

  &.completed {
    background: rgba(255, 255, 255, 0.95);
    color: #10b981;
    box-shadow: 0 2px 8px rgba(16, 185, 129, 0.2);

    .status-dot {
      background: #10b981;
      animation: none;
    }
  }

  &.paused {
    background: rgba(255, 255, 255, 0.95);
    color: #6b7280;
    box-shadow: 0 2px 8px rgba(107, 114, 128, 0.2);

    .status-dot {
      background: #6b7280;
      animation: none;
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}

.novel-info {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.novel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 6px;
}

.novel-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.novel-desc {
  font-size: 11px;
  color: #6b7280;
  margin: 0 0 8px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 33px;
}

.novel-stats {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: #475569;
    background: #fff;
    padding: 4px 8px;
    border-radius: 8px;
    border: 1px solid #e8eaed;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);

    .el-icon {
      color: #6366f1;
      font-size: 12px;
    }

    .stat-value {
      font-weight: 600;
      color: #1e293b;
      font-size: 12px;
    }

    .stat-label {
      font-size: 10px;
      color: #9ca3af;
    }

    &.chapters {
      .el-icon {
        color: #6366f1;
      }
    }

    &.words {
      .el-icon {
        color: #8b5cf6;
      }
    }
  }
}

.novel-agents {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-radius: 10px;
  padding: 8px 10px;
  margin-top: auto;
  border: 1px solid rgba(99, 102, 241, 0.12);

  &.empty {
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-color: #e2e8f0;

    .agent-label {
      color: #94a3b8;

      .el-icon {
        color: #94a3b8;
      }
    }
  }

  .agent-label {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 10px;
    color: #6366f1;
    font-weight: 600;
    margin-bottom: 6px;
    letter-spacing: 0.5px;

    .el-icon {
      font-size: 11px;
    }

    .pulse-icon {
      animation: pulse 2s infinite;
    }
  }

  .agent-list {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .agent-item {
    display: flex;
    align-items: center;
    gap: 6px;

    .agent-role {
      display: flex;
      align-items: center;
      gap: 3px;
      font-size: 10px;
      font-weight: 600;
      padding: 3px 8px;
      border-radius: 6px;
      flex-shrink: 0;

      .el-icon {
        font-size: 10px;
      }
    }

    .agent-name {
      font-size: 12px;
      color: #4b5563;
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &.author {
      .agent-role {
        background: linear-gradient(135deg, #6366f1 0%, #7c3aed 100%);
        color: #fff;
      }
    }

    &.editor {
      .agent-role {
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        color: #fff;
      }
    }

    &.qa {
      .agent-role {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
        color: #fff;
      }
    }
  }
}

.novel-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f1f5f9;

  .time-item {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 11px;
    color: #9ca3af;

    .el-icon {
      font-size: 12px;
    }
  }
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 8px;

  .write-btn {
    opacity: 1;
    transform: translateX(0);
    transition: all 0.3s ease;
    border-radius: 6px;
    font-weight: 500;
    padding: 6px 12px;
    font-size: 12px;
    background: linear-gradient(135deg, #6366f1 0%, #7c3aed 100%);
    border: none;
    box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);

    &:hover {
      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
      transform: translateY(-1px);
    }
  }

  .more-btn {
    width: 32px;
    height: 32px;
    background: #f8fafc;
    border: 1px solid #e8eaed;
    color: #6b7280;
    transition: all 0.2s;

    &:hover {
      background: #f1f5f9;
      color: #6366f1;
      border-color: #c7d2fe;
    }
  }
}

// 列表视图
.novel-list {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e8eaed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  :deep(.el-table__header-wrapper) {
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  }

  :deep(.el-table__header th) {
    background: transparent !important;
    color: #4b5563;
    font-weight: 600;
    font-size: 13px;
  }

  :deep(.el-table__row) {
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover > td {
      background-color: #f8fafc !important;
    }
  }
}

.list-cover {
  width: 60px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: #f3f4f6;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .list-cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  }
}

.list-info {
  .list-title {
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 6px;
  }

  .list-desc {
    font-size: 13px;
    color: #6b7280;
    margin: 0 0 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 300px;
  }
}

.list-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #6b7280;
}

.list-dates {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

// 封面上传
.cover-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.cover-uploader {
  :deep(.el-upload) {
    border: 2px dashed #d1d5db;
    border-radius: 12px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      border-color: #6366f1;
    }
  }
}

.cover-preview {
  width: 160px;
  height: 220px;
  object-fit: cover;
  display: block;
}

.cover-upload-placeholder {
  width: 160px;
  height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: #f9fafb;
  color: #6b7280;
  font-size: 13px;

  .el-icon {
    color: #9ca3af;
  }
}

.cover-actions {
  display: flex;
  gap: 8px;
}

.delete-cover-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  border: none;
  color: #fff;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
  transition: all 0.3s ease;
}

.delete-cover-btn:hover {
  background: linear-gradient(135deg, #ee5a5a 0%, #dc4949 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}

.more-btn {
  background: #dbeafe;
  border: none;
  color: #2563eb;
  width: 36px;
  height: 36px;
  opacity: 1;
  transition: all 0.3s ease;
}

.more-btn:hover {
  background: #bfdbfe;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.2);
}

// 时间信息
.time-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;

  .time-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #6b7280;

    .el-icon {
      color: #9ca3af;
    }
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .novel-grid {
    grid-template-columns: 1fr;
  }

  .header-actions {
    flex-direction: column;
    gap: 8px;
  }
}

// Agent 管理样式
.agent-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.agent-management {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.agent-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  margin-top: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.agent-count {
  font-size: 14px;
  color: #6b7280;
}

.agent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 500px;
  overflow-y: auto;
}

.agent-card {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s;

  &:hover {
    border-color: #6366f1;
    box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
  }

  &.disabled {
    opacity: 0.6;
    background: #f3f4f6;
  }
}

.agent-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.agent-info {
  flex: 1;
}

.agent-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.agent-platform {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;

  .el-icon {
    color: #6366f1;
  }
}

.agent-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.agent-edit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: #fff;
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.agent-edit-btn:hover {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(102, 126, 234, 0.4);
}

.agent-delete-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  border: none;
  color: #fff;
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(255, 107, 107, 0.3);
  transition: all 0.3s ease;
}

.agent-delete-btn:hover {
  background: linear-gradient(135deg, #ee5a5a 0%, #dc4949 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(255, 107, 107, 0.4);
}

.agent-description {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
  line-height: 1.5;
}

.agent-meta {
  display: flex;
  gap: 16px;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #9ca3af;

    .el-icon {
      font-size: 13px;
    }
  }
}

// Agent 编辑弹窗样式
.agent-edit-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.platform-option {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .platform-name {
    font-weight: 500;
  }

  .platform-desc {
    font-size: 12px;
    color: #6b7280;
  }
}

.slider-hint {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
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

/* AI 功能配置样式 */
.ai-settings-form {
  .settings-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .settings-section-title {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;
  }

  .setting-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .setting-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .setting-label {
    font-size: 14px;
    color: #374151;
    font-weight: 500;
  }

  .setting-desc {
    font-size: 12px;
    color: #9ca3af;
  }
}

// 列表视图 Agent 配置列样式
.list-agents {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .list-agent-item {
    display: flex;
    align-items: center;
    gap: 6px;

    .list-agent-role {
      display: flex;
      align-items: center;
      gap: 3px;
      font-size: 10px;
      font-weight: 600;
      padding: 2px 6px;
      border-radius: 4px;
      flex-shrink: 0;
      color: #fff;
    }

    .list-agent-name {
      font-size: 12px;
      color: #4b5563;
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &.author .list-agent-role {
      background: linear-gradient(135deg, #6366f1 0%, #7c3aed 100%);
    }

    &.editor .list-agent-role {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    }

    &.qa .list-agent-role {
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    }
  }
}

.list-agents-empty {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #94a3b8;
}
</style>
