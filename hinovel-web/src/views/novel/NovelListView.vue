<template>
    <div class="novel-list-page">
        <header class="shelf-header">
            <div class="shelf-intro">
                <h1>我的书架</h1>
                <p v-if="!loading && !loadFailed" class="shelf-totals">
                    <span
                        ><b>{{ novels.length }}</b> 部作品</span
                    >
                    <span
                        ><b>{{ totalChapters }}</b> 章</span
                    >
                    <span
                        ><b>{{ formatWordCount(totalWordCount) }}</b> 字</span
                    >
                </p>
                <el-button :icon="Plus" type="primary" @click="openCreateDialog">新建小说</el-button>
            </div>
            <img alt="" class="shelf-art" src="@/assets/writing-room.webp" />
        </header>

        <div class="shelf-toolbar">
            <div aria-label="按作品状态筛选" class="status-filters">
                <button
                    v-for="item in statusFilters"
                    :key="item.value"
                    :aria-pressed="statusFilter === item.value"
                    type="button"
                    @click="statusFilter = item.value"
                >
                    {{ item.label }}
                </button>
            </div>
            <div class="shelf-controls">
                <el-input
                    v-model="query"
                    :prefix-icon="Search"
                    aria-label="搜索书名"
                    clearable
                    placeholder="搜索书名"
                />
                <div aria-label="书架视图" class="view-switch">
                    <button
                        v-for="mode in viewModes"
                        :key="mode.value"
                        :aria-label="mode.label"
                        :aria-pressed="viewMode === mode.value"
                        :title="mode.label"
                        type="button"
                        @click="viewMode = mode.value"
                    >
                        <el-icon>
                            <component :is="mode.icon" />
                        </el-icon>
                    </button>
                </div>
            </div>
        </div>

        <div v-if="loading" aria-label="正在加载书架" class="shelf-state" role="status">
            <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="loadFailed" class="shelf-state" role="status">
            <h2>书架加载失败</h2>
            <el-button @click="fetchNovels">重新加载</el-button>
        </div>
        <div v-else-if="!novels.length" class="shelf-state">
            <el-icon class="state-icon">
                <Reading />
            </el-icon>
            <h2>书架还是空的</h2>
            <el-button type="primary" @click="openCreateDialog">创建第一部小说</el-button>
        </div>
        <div v-else-if="!visibleNovels.length" class="shelf-state" role="status">
            <h2>没有找到匹配的作品</h2>
            <el-button @click="clearFilters">清除筛选</el-button>
        </div>
        <ul v-else :class="{ 'cover-view': viewMode === 'card' }" class="books">
            <li v-for="novel in visibleNovels" :key="novel.id" class="book">
                <router-link :aria-label="'打开《' + novel.title + '》'" :to="'/novel/' + novel.id" class="book-link">
                    <span :class="'cloth-' + coverTone(novel.id)" aria-hidden="true" class="book-cover">
                        <img
                            v-if="novel.coverUrl && failedCovers[novel.id] !== novel.coverUrl"
                            :src="novel.coverUrl"
                            alt=""
                            @error="failedCovers[novel.id] = novel.coverUrl"
                        />
                        <span v-else>{{ novel.title }}</span>
                    </span>
                    <span class="book-info">
                        <span :class="novel.status" class="book-status">{{ statusText(novel.status) }}</span>
                        <strong class="book-title">{{ novel.title }}</strong>
                        <span class="book-desc">{{ novel.description || '暂无简介' }}</span>
                        <span
                            v-if="novel.authorAgentName || novel.editorAgentName || novel.qaAgentName"
                            class="book-agents"
                        >
                            <span v-if="novel.authorAgentName">作者 {{ novel.authorAgentName }}</span>
                            <span v-if="novel.editorAgentName">编辑 {{ novel.editorAgentName }}</span>
                            <span v-if="novel.qaAgentName">问答 {{ novel.qaAgentName }}</span>
                        </span>
                    </span>
                </router-link>
                <div class="book-meta">
                    <span>{{ novel.chapterCount }} 章 <i>/</i> {{ formatWordCount(novel.wordCount) }} 字</span>
                    <time :datetime="novel.updateTime" :title="formatDateTime(novel.updateTime)"
                        >{{ formatDate(novel.updateTime) }} 更新
                    </time>
                </div>
                <div class="book-actions">
                    <el-button :icon="EditPen" @click="goToNovel(novel.id)">写作</el-button>
                    <el-dropdown trigger="click" @command="handleBookCommand($event, novel)">
                        <el-button :aria-label="novel.title + '的更多操作'" :icon="MoreFilled" text title="更多操作" />
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item :icon="Edit" command="edit">编辑小说</el-dropdown-item>
                                <el-dropdown-item :icon="Setting" command="settings">小说设置</el-dropdown-item>
                                <el-dropdown-item :icon="User" command="characters">人物图谱</el-dropdown-item>
                                <el-dropdown-item :icon="Delete" command="delete" divided>删除小说</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </li>
        </ul>

        <!-- 小说设置弹窗（Agent 配置 + AI 功能配置） -->
        <el-dialog v-model="showAgentConfigDialog" title="小说设置" width="600px" destroy-on-close>
            <el-skeleton v-if="agentConfigLoading" :rows="6" animated />
            <div v-else-if="!settingsReady" class="shelf-state">
                <p>设置加载失败</p>
                <el-button v-if="currentNovelForConfig" @click="openAgentConfigDialog(currentNovelForConfig)"
                    >重试
                </el-button>
            </div>
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
                    <NovelAiSettingsForm v-model="novelSettingsForm" :show-inherit-alert="!hasOwnSettings" />
                </el-tab-pane>
            </el-tabs>

            <template #footer>
                <el-button @click="showAgentConfigDialog = false">取消</el-button>
                <el-button
                    :disabled="!settingsReady"
                    :loading="agentConfigLoading"
                    type="primary"
                    @click="handleSaveAllSettings"
                >
                    保存设置
                </el-button>
            </template>
        </el-dialog>

        <!-- 创建/编辑小说对话框 -->
        <el-dialog v-model="showDialog" :title="isEdit ? '编辑小说' : '新建小说'" width="520px" destroy-on-close>
            <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
                <!-- 封面上传 -->
                <el-form-item label="封面">
                    <div class="cover-upload">
                        <el-upload
                            class="cover-uploader"
                            :http-request="uploadCover"
                            action=""
                            :show-file-list="false"
                            :before-upload="beforeCoverUpload"
                            accept="image/*"
                        >
                            <img v-if="form.coverUrl" :src="form.coverUrl" class="cover-preview" />
                            <div v-else class="cover-upload-placeholder">
                                <el-icon :size="28"><Plus /></el-icon>
                                <span>点击上传封面</span>
                            </div>
                        </el-upload>
                        <div v-if="form.coverUrl" class="cover-actions">
                            <el-button class="delete-cover-btn" size="small" type="danger" @click="form.coverUrl = ''">
                                <el-icon><Delete /></el-icon>
                                删除封面
                            </el-button>
                        </div>
                    </div>
                </el-form-item>

                <el-form-item label="小说标题" prop="title">
                    <el-input v-model.trim="form.title" placeholder="请输入小说标题" />
                </el-form-item>

                <el-form-item label="简介" prop="description">
                    <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简要描述您的小说内容" />
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
                <el-button :disabled="coverUploading" :loading="saving" type="primary" @click="handleSave">
                    {{ isEdit ? '保存' : '创建' }}
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { uploadImage } from '@/api/upload'
import type { UploadRequestOptions } from 'element-plus'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useNovelRouteStore } from '@/store/novel-route'
import {
    Calendar,
    Clock,
    Delete,
    Edit,
    EditPen,
    Grid,
    List,
    MoreFilled,
    Plus,
    Reading,
    Search,
    Setting,
    User,
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
    type UpdateNovelRequest,
} from '@/api/novel-platform'
import { type AgentConfig, getAvailableAgentList } from '@/api/agent'
import {
    createDefaultSettings,
    getEffectiveSettings,
    getNovelLevelSettings,
    type NovelSettings,
    saveNovelSettings,
} from '@/api/novel-settings'
import NovelAiSettingsForm from '@/components/novel/NovelAiSettingsForm.vue'

const router = useRouter()
const novelRoute = useNovelRouteStore()
const loading = ref(true)
const loadFailed = ref(false)
const novels = ref<Novel[]>([])

// 视图模式（默认列表，从 localStorage 恢复用户偏好）
const VIEW_MODE_STORAGE_KEY = 'novel_list_view_mode'
const viewMode = ref<'card' | 'list'>(localStorage.getItem(VIEW_MODE_STORAGE_KEY) === 'card' ? 'card' : 'list')
const viewModes = [
    { value: 'list', label: '列表视图', icon: List },
    { value: 'card', label: '封面视图', icon: Grid },
] as const
const statusFilters = [
    { value: '', label: '全部' },
    { value: 'ONGOING', label: '连载中' },
    { value: 'COMPLETED', label: '已完结' },
    { value: 'PAUSED', label: '暂停' },
]
const statusFilter = ref('')
const query = ref('')
const failedCovers = ref<Record<string, string>>({})
const visibleNovels = computed(() => {
    const keyword = query.value.trim().toLocaleLowerCase()
    return novels.value.filter(
        (novel) =>
            (!statusFilter.value || novel.status === statusFilter.value) &&
            novel.title.toLocaleLowerCase().includes(keyword),
    )
})
const clearFilters = () => {
    query.value = ''
    statusFilter.value = ''
}
// 使用字符串末位分配书封颜色，保留长整型 ID 的精度。
const coverTone = (id: string) => String(id).slice(-1).charCodeAt(0) % 4

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
    status: 'ONGOING',
})

const rules: FormRules = {
    title: [
        { required: true, message: '请输入小说标题', trigger: 'blur' },
        { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' },
    ],
}

const agents = ref<AgentConfig[]>([])

const showAgentConfigDialog = ref(false)
const agentConfigLoading = ref(false)
const currentNovelForConfig = ref<Novel | null>(null)
const agentConfig = ref<NovelAgentConfig>({
    novelId: '',
    authorAgentId: undefined,
    editorAgentId: undefined,
    qaAgentId: undefined,
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
        ONGOING: '连载中',
        COMPLETED: '已完结',
        PAUSED: '暂停',
    }
    return map[status] || status
}

const formatWordCount = (count: number | string) => {
    const num = Number(count) || 0
    return num < 10000 ? num.toLocaleString('zh-CN') : `${(num / 10000).toFixed(1)} 万`
}

const formatDate = (date: string) => (date ? new Date(date).toLocaleDateString('zh-CN') : '')

const formatDateTime = (date: string) => {
    if (!date) return ''
    const d = new Date(date)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

const fetchNovels = async () => {
    loading.value = true
    loadFailed.value = false
    try {
        const res = await listMyNovels()
        novels.value = res.data
    } catch {
        loadFailed.value = true
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
        status: 'ONGOING',
    }
    coverUploading.value = false
    showDialog.value = true
}

const openEditDialog = (novel: Novel) => {
    isEdit.value = true
    currentNovel.value = novel
    form.value = {
        title: novel.title,
        description: novel.description || '',
        coverUrl: novel.coverUrl || '',
        status: novel.status,
    }
    coverUploading.value = false
    showDialog.value = true
}

const handleSave = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid || saving.value || coverUploading.value) return

    saving.value = true
    try {
        if (isEdit.value && currentNovel.value) {
            await updateNovel(currentNovel.value.id, form.value as UpdateNovelRequest)
            ElMessage.success('更新成功')
        } else {
            const res = await createNovel(form.value)
            ElMessage.success('创建成功')
            await router.push(`/novel/${res.data.id}`)
        }
        showDialog.value = false
        if (isEdit.value) await fetchNovels()
    } finally {
        saving.value = false
    }
}

// 封面上传
const coverUploading = ref(false)
let coverRequest = 0
const uploadCover = async ({ file }: UploadRequestOptions) => {
    const target = form.value
    const request = ++coverRequest
    coverUploading.value = true
    try {
        const url = await uploadImage(file)
        if (target === form.value && request === coverRequest) target.coverUrl = url
    } finally {
        if (target === form.value && request === coverRequest) coverUploading.value = false
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
        await ElMessageBox.confirm(`确定要删除《${novel.title}》吗？此操作不可恢复。`, '确认删除', {
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            type: 'warning',
        })
        await deleteNovel(novel.id)
        if (novelRoute.lastNovelPath === `/novel/${novel.id}`) novelRoute.clearLastNovelPath()
        ElMessage.success('删除成功')
        fetchNovels()
    } catch {
        // 取消
    }
}

const goToNovel = (id: string) => {
    router.push(`/novel/${id}`)
}

const handleBookCommand = (command: string, novel: Novel) => {
    if (command === 'edit') openEditDialog(novel)
    if (command === 'settings') openAgentConfigDialog(novel)
    if (command === 'characters') router.push(`/novel/${novel.id}/characters`)
    if (command === 'delete') confirmDelete(novel)
}

let settingsRequest = 0
const settingsReady = ref(false)
// 打开小说设置弹窗（同时加载 Agent 配置和 AI 功能配置）
const openAgentConfigDialog = async (novel: Novel) => {
    currentNovelForConfig.value = novel
    settingsActiveTab.value = 'agent'
    const request = ++settingsRequest
    settingsReady.value = false
    showAgentConfigDialog.value = true
    agentConfigLoading.value = true

    try {
        // 并行加载 Agent 配置和小说级别配置
        const [agentRes, settingsRes, agentList] = await Promise.all([
            getNovelAgentConfig(novel.id),
            getNovelLevelSettings(novel.id),
            getAvailableAgentList(),
        ])
        const settings = settingsRes.data ?? (await getEffectiveSettings(novel.id)).data
        if (request !== settingsRequest) return
        agents.value = agentList
        agentConfig.value = agentRes.data ?? { novelId: novel.id }
        novelSettingsForm.value = settings
        hasOwnSettings.value = !!settingsRes.data
        settingsReady.value = true
    } catch (error) {
        ElMessage.error('获取小说设置失败')
    } finally {
        if (request === settingsRequest) agentConfigLoading.value = false
    }
}

// 保存全部设置（Agent 配置 + AI 功能配置）
const handleSaveAllSettings = async () => {
    if (!currentNovelForConfig.value || !settingsReady.value || agentConfigLoading.value) return
    const request = settingsRequest

    agentConfigLoading.value = true
    try {
        const novelId = currentNovelForConfig.value.id

        // 并行保存 Agent 配置和小说配置
        const agentRequest: NovelAgentConfigRequest = {
            authorAgentId: agentConfig.value.authorAgentId,
            editorAgentId: agentConfig.value.editorAgentId,
            qaAgentId: agentConfig.value.qaAgentId,
        }
        const [agentRes] = await Promise.all([
            saveNovelAgentConfig(novelId, agentRequest),
            saveNovelSettings(novelId, novelSettingsForm.value),
        ])

        // 更新本地小说数据中的 Agent 信息
        if (agentRes.data) {
            const index = novels.value.findIndex((n) => n.id === novelId)
            if (index !== -1) {
                novels.value[index] = {
                    ...novels.value[index],
                    authorAgentId: agentRes.data.authorAgentId,
                    authorAgentName: agentRes.data.authorAgentName,
                    editorAgentId: agentRes.data.editorAgentId,
                    editorAgentName: agentRes.data.editorAgentName,
                    qaAgentId: agentRes.data.qaAgentId,
                    qaAgentName: agentRes.data.qaAgentName,
                }
            }
        }
        if (request !== settingsRequest) return
        ElMessage.success('小说设置保存成功')
        showAgentConfigDialog.value = false
    } catch (error) {
        ElMessage.error('保存小说设置失败')
    } finally {
        if (request === settingsRequest) agentConfigLoading.value = false
    }
}

onMounted(fetchNovels)

// 判断是否为内置 Agent
const isBuiltinAgent = (agent: AgentConfig): boolean => {
    return agent.isBuiltin === 1
}

/**
 * 用户自建的 Agent 列表（过滤掉内置模板）
 */
const userAgentList = computed(() => {
    return agents.value.filter((a) => !isBuiltinAgent(a))
})
</script>

<style scoped>
.shelf-header {
    display: grid;
    grid-template-columns: 1fr minmax(240px, 42%);
    min-height: 196px;
    background: #e8ede2;
    border-radius: 4px 14px 14px 4px;
    overflow: hidden;
    border-left: 6px solid var(--pine);
}

.shelf-intro {
    align-self: center;
    padding: 22px clamp(20px, 3.1vw, 48px);
}

.shelf-intro h1 {
    font: 600 clamp(32px, 3vw, 46px)/1.2 var(--display-font);
}

.shelf-totals {
    display: flex;
    flex-wrap: wrap;
    gap: 4px 16px;
    color: var(--muted);
    font-size: 13px;
    margin-top: 12px;
}

.shelf-totals b {
    color: var(--ink);
    font-weight: 600;
}

.shelf-intro .el-button {
    margin-top: 20px;
}

.shelf-art {
    width: 100%;
    height: 100%;
    max-height: 225px;
    object-fit: cover;
    object-position: center 55%;
}

.shelf-toolbar,
.shelf-controls,
.status-filters,
.view-switch,
.book-actions {
    display: flex;
    align-items: center;
}

.shelf-toolbar {
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 14px;
    margin: 28px 0 16px;
}

.shelf-controls {
    gap: 12px;
}

.shelf-controls .el-input {
    width: 180px;
}

.status-filters {
    gap: 3px;
}

.status-filters button,
.view-switch button {
    color: var(--muted);
    border: 0;
    background: transparent;
    cursor: pointer;
}

.status-filters button {
    padding: 7px 11px;
    border-radius: 5px;
    font-size: 14px;
}

.status-filters button[aria-pressed='true'],
.view-switch button[aria-pressed='true'] {
    background: var(--sage);
    color: var(--pine-deep);
    font-weight: 650;
}

.view-switch {
    padding: 2px;
    border: 1px solid var(--rule);
    border-radius: 6px;
    background: var(--surface);
}

.view-switch button {
    width: 31px;
    height: 27px;
    display: grid;
    place-items: center;
    border-radius: 3px;
}

.books {
    padding: 0;
    margin: 0;
    list-style: none;
}

.book {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(130px, 0.32fr) auto;
    align-items: center;
    gap: 18px;
    padding: 19px 16px;
    border-top: 1px solid var(--rule);
}

.book:last-child {
    border-bottom: 1px solid var(--rule);
}

.book-link {
    min-width: 0;
    display: flex;
    gap: 22px;
    align-items: center;
    color: var(--ink);
    text-decoration: none;
}

.book-link:hover .book-title {
    text-decoration: underline;
    text-underline-offset: 4px;
}

.book-cover {
    position: relative;
    flex: none;
    display: grid;
    place-items: center;
    width: 64px;
    height: 88px;
    background: #36584a;
    color: #f0eada;
    border-radius: 2px 5px 5px 2px;
    box-shadow: 2px 3px 4px #26382d1a;
    overflow: hidden;
}

.book-cover::after {
    content: '';
    position: absolute;
    inset: 0 auto 0 6px;
    border-left: 1px solid #fff4;
}

.book-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.book-cover > span {
    writing-mode: vertical-rl;
    max-height: 76%;
    margin-left: 5px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    font: 500 12px/1.5 var(--display-font);
    letter-spacing: 1px;
}

.cloth-1 {
    background: #516675;
}

.cloth-2 {
    background: #865547;
}

.cloth-3 {
    background: #746544;
}

.book-info,
.book-meta {
    display: grid;
    gap: 5px;
    min-width: 0;
}

.book-title,
.book-desc {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.book-title {
    font: 600 21px/1.4 var(--display-font);
}

.book-desc,
.book-meta {
    font-size: 13px;
    color: var(--muted);
}

.book-status {
    font-size: 12px;
    color: var(--pine);
    display: flex;
    gap: 6px;
    align-items: center;
}

.book-status::before {
    content: '';
    height: 6px;
    width: 6px;
    border-radius: 50%;
    background: currentColor;
}

.book-status.PAUSED {
    color: #86671e;
}

.book-status.COMPLETED {
    color: #54697a;
}

.book-agents {
    display: flex;
    flex-wrap: wrap;
    gap: 3px 12px;
    font-size: 12px;
    color: var(--muted);
    overflow-wrap: anywhere;
}

.book-meta i {
    font-style: normal;
    color: #a0aea1;
    margin: 0 4px;
}

.book-meta time {
    font-size: 12px;
}

.book-actions {
    gap: 4px;
}

.cover-view {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(min(100%, 310px), 1fr));
    gap: 16px;
}

.cover-view .book {
    grid-template-columns: 1fr auto;
    gap: 18px 8px;
    align-content: space-between;
    border: 1px solid var(--rule);
    background: var(--surface);
    border-radius: 8px;
    padding: 20px;
}

.cover-view .book-link {
    grid-column: 1 / -1;
    align-items: flex-start;
    gap: 16px;
}

.cover-view .book-cover {
    width: 88px;
    height: 124px;
}

.cover-view .book-cover > span {
    font-size: 15px;
}

.cover-view .book-desc {
    white-space: normal;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

.shelf-state {
    text-align: center;
    padding: clamp(36px, 7vw, 84px) 20px;
    border-block: 1px solid var(--rule);
}

.shelf-state h2 {
    font: 600 22px var(--display-font);
    margin-bottom: 20px;
}

.state-icon {
    color: var(--pine);
    font-size: 38px;
    margin-bottom: 18px;
}

.cover-upload {
    display: flex;
    align-items: center;
    gap: 14px;
}

.cover-preview,
.cover-upload-placeholder {
    width: 80px;
    height: 108px;
    border-radius: 4px;
    object-fit: cover;
}

.cover-upload-placeholder {
    display: grid;
    align-content: center;
    justify-items: center;
    border: 1px dashed var(--rule);
    background: var(--paper);
    color: var(--muted);
    font-size: 12px;
}

.form-hint,
.time-info {
    color: var(--muted);
    font-size: 12px;
}

.time-item {
    display: flex;
    align-items: center;
    gap: 6px;
}

@media (max-width: 700px) {
    .shelf-header {
        grid-template-columns: 1.2fr 1fr;
        min-height: 150px;
    }

    .shelf-art {
        min-height: 100%;
        object-position: 58%;
    }

    .shelf-intro {
        padding: 18px 12px;
    }

    .shelf-totals {
        gap: 3px 8px;
    }

    .shelf-toolbar {
        margin-top: 20px;
    }

    .shelf-controls {
        width: 100%;
    }

    .shelf-controls .el-input {
        flex: 1;
    }

    .book {
        grid-template-columns: minmax(0, 1fr) auto;
        gap: 12px 8px;
        padding: 16px 2px;
    }

    .book-link {
        grid-column: 1 / -1;
        gap: 14px;
    }

    .book-meta {
        gap: 2px;
    }
}
</style>
