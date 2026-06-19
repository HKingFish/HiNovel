<template>
  <div class="call-log-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>调用记录</h1>
        <p class="subtitle">查看所有 LLM 调用的请求、响应、Token 消耗和耗时信息</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="refresh-btn" @click="loadData">
          <el-icon>
            <Refresh/>
          </el-icon>
          <span>刷新</span>
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon success">
          <el-icon>
            <Check/>
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ successCount }}</span>
          <span class="stat-label">成功调用</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon error">
          <el-icon>
            <WarningFilled/>
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ failCount }}</span>
          <span class="stat-label">失败调用</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon token">
          <el-icon>
            <Coin/>
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ totalTokensUsed.toLocaleString() }}</span>
          <span class="stat-label">总 Token 消耗</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon time">
          <el-icon>
            <Timer/>
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ formatTotalDuration(totalDuration) }}</span>
          <span class="stat-label">总耗时</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-card">
      <div class="filter-bar">
        <div class="filter-group">
          <span class="filter-label">时间范围</span>
          <el-date-picker
              v-model="filterDateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="filter-date-picker"
              :shortcuts="dateShortcuts"
              @change="loadData"
          />
        </div>
        <div class="filter-group">
          <span class="filter-label">调用场景</span>
          <el-select
              v-model="filterScene"
              placeholder="全部场景"
              clearable
              class="filter-select"
              @change="loadData"
          >
            <el-option label="改写" value="REWRITE"/>
            <el-option label="审核" value="AUDIT"/>
            <el-option label="重新生成" value="REGENERATE"/>
          </el-select>
        </div>
        <div class="filter-group">
          <span class="filter-label">调用状态</span>
          <el-select
              v-model="filterStatus"
              placeholder="全部状态"
              clearable
              class="filter-select"
              @change="loadData"
          >
            <el-option label="成功" value="SUCCESS"/>
            <el-option label="失败" value="FAILED"/>
          </el-select>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table
          v-loading="loading"
          :data="logList"
          class="log-table"
          @row-click="openDetail"
      >
        <el-table-column label="时间" prop="createTime" width="170">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon class="time-icon">
                <Clock/>
              </el-icon>
              <span>{{ formatTime(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="场景" prop="callScene" width="100">
          <template #default="{ row }">
            <el-tag :type="getSceneTagType(row.callScene)" size="small" effect="plain">
              {{ getSceneLabel(row.callScene) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Agent" prop="agentName" width="130">
          <template #default="{ row }">
            <span class="agent-name">{{ row.agentName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="模型" prop="modelName" width="160">
          <template #default="{ row }">
            <span class="model-name">{{ row.modelName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="90" align="center">
          <template #default="{ row }">
            <div class="status-cell" :class="row.status === 'SUCCESS' ? 'success' : 'error'">
              <el-icon v-if="row.status === 'SUCCESS'">
                <CircleCheck/>
              </el-icon>
              <el-icon v-else>
                <CircleClose/>
              </el-icon>
              <span>{{ row.status === 'SUCCESS' ? '成功' : '失败' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="流式" prop="isStreaming" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isStreaming === 1" type="success" size="small" effect="light">是</el-tag>
            <span v-else class="streaming-no">否</span>
          </template>
        </el-table-column>
        <el-table-column label="输入 Token" prop="promptTokens" width="110" align="right">
          <template #default="{ row }">
            <span class="token-value input">{{ row.promptTokens > 0 ? row.promptTokens.toLocaleString() : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="输出 Token" prop="completionTokens" width="110" align="right">
          <template #default="{ row }">
            <span class="token-value output">{{
                row.completionTokens > 0 ? row.completionTokens.toLocaleString() : '-'
              }}</span>
          </template>
        </el-table-column>
        <el-table-column label="总 Token" prop="totalTokens" width="110" align="right">
          <template #default="{ row }">
            <span class="token-value total">{{ row.totalTokens > 0 ? row.totalTokens.toLocaleString() : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="耗时" prop="processingTimeMs" width="100" align="right">
          <template #default="{ row }">
            <span class="duration-value">{{ formatDuration(row.processingTimeMs) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
        v-model="showDetail"
        title="调用记录详情"
        width="800px"
        class="detail-dialog"
        align-center
        destroy-on-close
    >
      <div v-if="detailLog" class="detail-content">
        <!-- 基本信息卡片 -->
        <div class="info-card">
          <div class="info-card-header">
            <el-icon class="card-icon">
              <Document/>
            </el-icon>
            <span>基本信息</span>
          </div>
          <div class="info-card-body">
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">调用场景</span>
                <el-tag :type="getSceneTagType(detailLog.callScene)" size="default">
                  {{ getSceneLabel(detailLog.callScene) }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="info-label">调用状态</span>
                <el-tag :type="detailLog.status === 'SUCCESS' ? 'success' : 'danger'" size="default">
                  {{ detailLog.status === 'SUCCESS' ? '成功' : '失败' }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="info-label">Agent</span>
                <span class="info-value">{{ detailLog.agentName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">模型</span>
                <span class="info-value model-tag">{{ detailLog.modelName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">提供方</span>
                <span class="info-value">{{ detailLog.llmProviderName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">流式调用</span>
                <el-tag :type="detailLog.isStreaming === 1 ? 'success' : 'info'" size="default" effect="plain">
                  {{ detailLog.isStreaming === 1 ? '是' : '否' }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="info-label">耗时</span>
                <span class="info-value duration">{{ formatDuration(detailLog.processingTimeMs) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">调用时间</span>
                <span class="info-value">{{ formatTime(detailLog.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Token 统计卡片 -->
        <div class="token-card">
          <div class="token-card-header">
            <el-icon class="card-icon">
              <Coin/>
            </el-icon>
            <span>Token 消耗</span>
          </div>
          <div class="token-card-body">
            <div class="token-flow">
              <div class="token-block">
                <div class="token-icon input">
                  <el-icon>
                    <Download/>
                  </el-icon>
                </div>
                <div class="token-info">
                  <span class="token-number">{{ detailLog.promptTokens?.toLocaleString() || 0 }}</span>
                  <span class="token-text">输入 Token</span>
                </div>
              </div>
              <div class="token-arrow">
                <el-icon>
                  <Plus/>
                </el-icon>
              </div>
              <div class="token-block">
                <div class="token-icon output">
                  <el-icon>
                    <Upload/>
                  </el-icon>
                </div>
                <div class="token-info">
                  <span class="token-number">{{ detailLog.completionTokens?.toLocaleString() || 0 }}</span>
                  <span class="token-text">输出 Token</span>
                </div>
              </div>
              <div class="token-arrow">
                <el-icon>
                  <Right/>
                </el-icon>
              </div>
              <div class="token-block total">
                <div class="token-icon total-icon">
                  <el-icon>
                    <Coin/>
                  </el-icon>
                </div>
                <div class="token-info">
                  <span class="token-number">{{ detailLog.totalTokens?.toLocaleString() || 0 }}</span>
                  <span class="token-text">总计</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 请求内容 -->
        <div v-if="detailLog.requestContent" class="content-card">
          <div class="content-card-header">
            <div class="header-left">
              <el-icon class="card-icon">
                <EditPen/>
              </el-icon>
              <span>请求内容</span>
            </div>
            <el-button size="small" text @click="copyContent(detailLog.requestContent)">
              <el-icon>
                <CopyDocument/>
              </el-icon>
              复制
            </el-button>
          </div>
          <div class="content-card-body">
            <pre class="content-pre">{{ detailLog.requestContent }}</pre>
          </div>
        </div>

        <!-- 响应内容 -->
        <div v-if="detailLog.responseContent" class="content-card">
          <div class="content-card-header">
            <div class="header-left">
              <el-icon class="card-icon success">
                <Document/>
              </el-icon>
              <span>响应内容</span>
            </div>
            <el-button size="small" text @click="copyContent(detailLog.responseContent)">
              <el-icon>
                <CopyDocument/>
              </el-icon>
              复制
            </el-button>
          </div>
          <div class="content-card-body">
            <pre class="content-pre">{{ detailLog.responseContent }}</pre>
          </div>
        </div>

        <!-- 错误信息 -->
        <div v-if="detailLog.errorMessage" class="content-card error-card">
          <div class="content-card-header">
            <div class="header-left">
              <el-icon class="card-icon error">
                <WarningFilled/>
              </el-icon>
              <span>错误信息</span>
            </div>
            <el-button size="small" text @click="copyContent(detailLog.errorMessage)">
              <el-icon>
                <CopyDocument/>
              </el-icon>
              复制
            </el-button>
          </div>
          <div class="content-card-body">
            <pre class="content-pre error-text">{{ detailLog.errorMessage }}</pre>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {
  Check,
  CircleCheck,
  CircleClose,
  Clock,
  Coin,
  CopyDocument,
  Document,
  Download,
  EditPen,
  Plus,
  Refresh,
  Right,
  Timer,
  Upload,
  WarningFilled
} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {type CallStatistics, getCallStatistics, getLlmCallLogList, type LlmCallLog} from '@/api/llm-call-log'

/**
 * 格式化日期为 yyyy-MM-dd HH:mm:ss
 */
const formatDateTime = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 获取默认时间范围（当前月第一天 00:00:00 到当前时间）
 */
const getDefaultDateRange = (): [string, string] => {
  const now = new Date()
  const monthStart = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0)
  return [formatDateTime(monthStart), formatDateTime(now)]
}

// 列表数据
const logList = ref<LlmCallLog[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

// 筛选条件
const filterScene = ref('')
const filterStatus = ref('')
const filterDateRange = ref<[string, string]>(getDefaultDateRange())

// 时间快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      start.setHours(0, 0, 0, 0)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      start.setHours(0, 0, 0, 0)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 3)
      start.setHours(0, 0, 0, 0)
      return [start, end]
    }
  },
  {
    text: '本月',
    value: () => {
      const end = new Date()
      const start = new Date(end.getFullYear(), end.getMonth(), 1, 0, 0, 0)
      return [start, end]
    }
  },
  {
    text: '上月',
    value: () => {
      const now = new Date()
      const start = new Date(now.getFullYear(), now.getMonth() - 1, 1, 0, 0, 0)
      const end = new Date(now.getFullYear(), now.getMonth(), 0, 23, 59, 59)
      return [start, end]
    }
  }
]

// 详情弹窗
const showDetail = ref(false)
const detailLog = ref<LlmCallLog | null>(null)

// 统计数据（从后端获取）
const statistics = ref<CallStatistics>({
  successCount: 0,
  failCount: 0,
  totalTokens: 0,
  totalDuration: 0
})

const successCount = computed(() => statistics.value.successCount)
const failCount = computed(() => statistics.value.failCount)
const totalTokensUsed = computed(() => statistics.value.totalTokens)
const totalDuration = computed(() => statistics.value.totalDuration)

/**
 * 加载调用记录列表和统计数据
 */
const loadData = async () => {
  loading.value = true
  const startTime = filterDateRange.value?.[0] || undefined
  const endTime = filterDateRange.value?.[1] || undefined
  const scene = filterScene.value || undefined
  const status = filterStatus.value || undefined

  try {
    // 并行加载列表和统计数据
    const [listRes, statsRes] = await Promise.all([
      getLlmCallLogList(
          currentPage.value,
          pageSize.value,
          scene,
          status,
          startTime,
          endTime
      ),
      getCallStatistics(scene, status, startTime, endTime)
    ])
    const pageData = listRes.data
    logList.value = pageData.records || []
    total.value = pageData.total || 0
    if (statsRes.data) {
      statistics.value = statsRes.data
    }
  } catch (error) {
    ElMessage.error('加载调用记录失败')
  } finally {
    loading.value = false
  }
}

/**
 * 打开详情弹窗
 */
const openDetail = (row: LlmCallLog) => {
  detailLog.value = row
  showDetail.value = true
}

/**
 * 格式化时间
 */
const formatTime = (time: string): string => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
}

/**
 * 格式化耗时
 */
const formatDuration = (ms: number): string => {
  if (!ms || ms <= 0) return '-'
  if (ms < 1000) return ms + 'ms'
  return (ms / 1000).toFixed(1) + 's'
}

/**
 * 格式化总耗时
 */
const formatTotalDuration = (ms: number): string => {
  if (!ms || ms <= 0) return '0s'
  if (ms < 60000) return (ms / 1000).toFixed(1) + 's'
  const minutes = Math.floor(ms / 60000)
  const seconds = Math.round((ms % 60000) / 1000)
  return `${minutes}m ${seconds}s`
}

/**
 * 获取场景标签类型
 */
const getSceneTagType = (scene: string): string => {
  const sceneTypeMap: Record<string, string> = {
    REWRITE: '',
    AUDIT: 'warning',
    REGENERATE: 'success'
  }
  return sceneTypeMap[scene] || 'info'
}

/**
 * 获取场景中文标签
 */
const getSceneLabel = (scene: string): string => {
  const sceneLabelMap: Record<string, string> = {
    REWRITE: '改写',
    AUDIT: '审核',
    REGENERATE: '重新生成'
  }
  return sceneLabelMap[scene] || scene
}

/**
 * 复制内容到剪贴板
 */
const copyContent = async (content: string) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.call-log-page {
  padding: 24px;
  padding-bottom: 40px;
  min-width: 1000px;
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

.refresh-btn {
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
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8eaed;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.stat-icon .el-icon {
  font-size: 24px;
}

.stat-icon.success {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #059669;
}

.stat-icon.error {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
}

.stat-icon.token {
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  color: #6366f1;
}

.stat-icon.time {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

/* 筛选卡片 */
.filter-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8eaed;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 20px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  color: #6b7280;
  white-space: nowrap;
}

.filter-select {
  width: 150px;
}

.filter-date-picker {
  width: 380px;
}

/* 表格卡片 */
.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8eaed;
  overflow: hidden;
}

.log-table {
  width: 100%;
}

.log-table :deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.log-table :deep(.el-table__header th) {
  background: transparent !important;
  color: #4b5563;
  font-weight: 600;
  font-size: 13px;
}

.log-table :deep(.el-table__row) {
  cursor: pointer;
  transition: background-color 0.2s;
}

.log-table :deep(.el-table__row:hover > td) {
  background-color: #f8fafc !important;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6b7280;
  font-size: 13px;
}

.time-icon {
  color: #9ca3af;
}

.agent-name {
  color: #1f2937;
  font-weight: 500;
}

.model-name {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 12px;
  color: #6366f1;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  padding: 4px 8px;
  border-radius: 6px;
}

.status-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.status-cell.success {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #059669;
}

.status-cell.error {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
}

.streaming-no {
  color: #9ca3af;
}

.token-value {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 13px;
  font-weight: 500;
}

.token-value.input {
  color: #3b82f6;
}

.token-value.output {
  color: #10b981;
}

.token-value.total {
  color: #6366f1;
  font-weight: 600;
}

.duration-value {
  color: #f59e0b;
  font-weight: 600;
  font-size: 13px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 详情弹窗样式 */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 信息卡片 */
.info-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8eaed;
  overflow: hidden;
}

.info-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 18px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e8eaed;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.info-card-header .card-icon {
  color: #6366f1;
  font-size: 18px;
}

.info-card-body {
  padding: 18px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px 32px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-label {
  color: #6b7280;
  font-size: 14px;
  min-width: 70px;
  flex-shrink: 0;
}

.info-value {
  color: #1f2937;
  font-size: 14px;
  font-weight: 500;
}

.info-value.model-tag {
  font-family: 'SF Mono', 'Fira Code', monospace;
  color: #6366f1;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
}

.info-value.duration {
  color: #f59e0b;
  font-weight: 600;
}

/* Token 卡片 */
.token-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.25);
}

.token-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.token-card-header .card-icon {
  font-size: 18px;
}

.token-card-body {
  padding: 24px 18px;
}

.token-flow {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.token-block {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.token-block.total {
  background: rgba(255, 255, 255, 0.95);
  border: none;
}

.token-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.token-icon.input {
  background: rgba(59, 130, 246, 0.2);
  color: #93c5fd;
}

.token-icon.output {
  background: rgba(16, 185, 129, 0.2);
  color: #6ee7b7;
}

.token-icon.total-icon {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff;
}

.token-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.token-number {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

.token-block.total .token-number {
  color: #6366f1;
}

.token-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.token-block.total .token-text {
  color: #6b7280;
}

.token-arrow {
  color: rgba(255, 255, 255, 0.6);
  font-size: 18px;
  font-weight: 600;
}

/* 内容卡片 */
.content-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8eaed;
  overflow: hidden;
}

.content-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 18px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e8eaed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.content-card-header .card-icon {
  color: #6366f1;
  font-size: 16px;
}

.content-card-header .card-icon.success {
  color: #10b981;
}

.content-card-header .card-icon.error {
  color: #ef4444;
}

.content-card-body {
  padding: 16px 18px;
  max-height: 300px;
  overflow-y: auto;
}

.content-pre {
  margin: 0;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.7;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 错误卡片 */
.error-card {
  border-color: #fecaca;
}

.error-card .content-card-header {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
}

.error-card .content-pre {
  color: #dc2626;
}

/* 弹窗样式覆盖 */
:deep(.detail-dialog) {
  border-radius: 16px;
}

:deep(.detail-dialog .el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #e8eaed;
  margin: 0;
}

:deep(.detail-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

:deep(.detail-dialog .el-dialog__body) {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
