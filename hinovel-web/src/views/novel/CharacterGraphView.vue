<template>
  <div class="character-graph-page">
    <!-- 顶部标题栏 -->
    <header class="graph-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" type="primary" plain @click="goBack" class="back-btn">
          返回
        </el-button>
        <el-divider direction="vertical" />
        <div class="header-title">
          <h2>{{ novelTitle }} - 人物图谱</h2>
          <span class="subtitle">管理小说中的角色关系</span>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="openAddCharacter">
          添加角色
        </el-button>
      </div>
    </header>

    <!-- 主体内容 -->
    <div class="graph-body">
      <!-- 左侧角色列表 -->
      <aside class="character-sidebar">
        <div class="sidebar-header">
          <h3>角色列表</h3>
          <span class="count">{{ characters.length }} 人</span>
        </div>
        <div class="character-list" v-loading="loading">
          <div
            v-for="char in characters"
            :key="char.id"
            class="character-item"
            :class="{ active: selectedCharacter?.id === char.id }"
            @click="selectCharacter(char)"
          >
            <el-avatar 
              :size="40" 
              :src="char.avatarUrl" 
              :style="{ background: char.color || '#6366f1' }"
            >
              {{ char.name.charAt(0) }}
            </el-avatar>
            <div class="character-info">
              <span class="character-name">{{ char.name }}</span>
              <span class="character-role">{{ getRoleLabel(char.roleType) }}</span>
            </div>
          </div>
          <el-empty v-if="characters.length === 0 && !loading" description="暂无角色" />
        </div>
      </aside>

      <!-- 中间关系图谱 -->
      <div class="graph-main">
        <div class="graph-toolbar">
          <el-radio-group v-model="graphMode" size="small" @change="updateGraph">
            <el-radio-button label="force">力导向</el-radio-button>
            <el-radio-button label="circular">环形</el-radio-button>
          </el-radio-group>
          <el-button :icon="Refresh" text circle @click="loadCharacterGraph" />
        </div>
        <div ref="graphContainer" class="graph-container" v-loading="loading"></div>
      </div>

      <!-- 右侧角色详情 -->
      <div class="character-detail" v-if="selectedCharacter">
        <div class="detail-header">
          <el-avatar 
            :size="64" 
            :src="selectedCharacter.avatarUrl"
            :style="{ background: selectedCharacter.color || '#6366f1' }"
          >
            {{ selectedCharacter.name.charAt(0) }}
          </el-avatar>
          <div class="detail-title">
            <h3>{{ selectedCharacter.name }}</h3>
            <el-tag size="small" :type="roleTagType(selectedCharacter.roleType)">
              {{ getRoleLabel(selectedCharacter.roleType) }}
            </el-tag>
          </div>
          <el-dropdown trigger="click">
            <el-button circle text>
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="openEditCharacter(selectedCharacter)">
                  <el-icon><Edit /></el-icon>
                  <span>编辑</span>
                </el-dropdown-item>
                <el-dropdown-item @click="confirmDeleteCharacter(selectedCharacter)">
                  <el-icon color="#ef4444"><Delete /></el-icon>
                  <span style="color: #ef4444;">删除</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div class="detail-content">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">性别</span>
                <span class="value">{{ selectedCharacter.gender || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="label">年龄</span>
                <span class="value">{{ selectedCharacter.age || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="label">身份</span>
                <span class="value">{{ selectedCharacter.identity || '未知' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4>人物简介</h4>
            <p class="description">{{ selectedCharacter.background || selectedCharacter.personality || '暂无简介' }}</p>
          </div>

          <div class="detail-section">
            <h4>人物关系</h4>
            <div class="relations-list">
              <div
                v-for="relation in characterRelations"
                :key="relation.id"
                class="relation-item"
              >
                <el-avatar 
                  :size="32" 
                  :src="getTargetCharacter(relation.targetId)?.avatarUrl"
                  :style="{ background: getTargetCharacter(relation.targetId)?.color || '#6366f1' }"
                >
                  {{ getTargetCharacter(relation.targetId)?.name.charAt(0) || '?' }}
                </el-avatar>
                <div class="relation-info">
                  <span class="relation-target">{{ getTargetCharacter(relation.targetId)?.name || '未知' }}</span>
                  <el-tag size="small" effect="plain">{{ relation.relationType }}</el-tag>
                </div>
                <el-button
                  :icon="Delete"
                  text
                  circle
                  size="small"
                  @click="removeRelation(relation.id)"
                />
              </div>
              <el-button
                v-if="characters.length > 1"
                :icon="Plus"
                text
                class="add-relation-btn"
                @click="openAddRelation"
              >
                添加关系
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="character-detail empty" v-else>
        <el-empty description="选择角色查看详情">
          <template #image>
            <el-icon :size="64" color="#d1d5db"><User /></el-icon>
          </template>
        </el-empty>
      </div>
    </div>

    <!-- 添加/编辑角色对话框 -->
    <el-dialog
      v-model="showCharacterDialog"
      :title="isEditCharacter ? '编辑角色' : '添加角色'"
      width="560px"
      destroy-on-close
      class="character-dialog"
    >
      <el-form ref="characterFormRef" :model="characterForm" :rules="characterRules" label-position="top"
               class="character-form">
        <!-- 头像上传 -->
        <el-form-item label="角色头像">
          <div class="avatar-upload-wrapper">
            <div class="avatar-display-box">
              <el-upload
                  class="avatar-uploader"
                  action="/api/oss/upload"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                  :headers="uploadHeaders"
              >
                <div class="avatar-circle" :class="{ 'has-image': characterForm.avatarUrl }">
                  <img
                      v-if="characterForm.avatarUrl"
                      :src="characterForm.avatarUrl"
                      class="avatar-image"
                      alt="角色头像"
                  />
                  <div v-else class="avatar-placeholder" :style="{ backgroundColor: characterForm.color || '#6366f1' }">
                    <el-icon :size="24">
                      <Plus/>
                    </el-icon>
                  </div>
                  <div class="avatar-hover-mask">
                    <el-icon :size="20">
                      <Camera/>
                    </el-icon>
                    <span>更换</span>
                  </div>
                </div>
              </el-upload>
              <div v-if="characterForm.avatarUrl" class="avatar-actions">
                <el-button
                    type="danger"
                    link
                    size="small"
                    @click="characterForm.avatarUrl = ''"
                >
                  <el-icon>
                    <Delete/>
                  </el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="角色名称" prop="name">
          <el-input v-model="characterForm.name" placeholder="请输入角色名称" />
        </el-form-item>

        <el-form-item label="角色类型">
          <el-radio-group v-model="characterForm.roleType">
            <el-radio-button label="PROTAGONIST">主角</el-radio-button>
            <el-radio-button label="SUPPORTING">配角</el-radio-button>
            <el-radio-button label="ANTAGONIST">反派</el-radio-button>
            <el-radio-button label="OTHER">其他</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <div class="form-row">
          <el-form-item label="性别" class="half">
            <el-select v-model="characterForm.gender" placeholder="选择性别" class="full-width">
              <el-option label="男" value="MALE" />
              <el-option label="女" value="FEMALE" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="年龄" class="half">
            <el-input-number v-model="characterForm.age" :min="0" :max="999" placeholder="年龄" class="full-width" />
          </el-form-item>
        </div>

        <el-form-item label="身份">
          <el-input v-model="characterForm.identity" placeholder="例如：学生、医生、将军..." />
        </el-form-item>

        <!-- 默认颜色选择 -->
        <el-form-item label="代表色">
          <div class="color-picker-wrapper">
            <div class="preset-colors">
              <div
                v-for="color in presetColors"
                :key="color"
                class="preset-color"
                :style="{ backgroundColor: color }"
                :class="{ active: characterForm.color === color }"
                @click="characterForm.color = color"
              />
            </div>
            <el-color-picker v-model="characterForm.color" show-alpha />
          </div>
        </el-form-item>

        <el-form-item label="人物简介">
          <el-input
            v-model="characterForm.background"
            type="textarea"
            :rows="3"
            placeholder="描述角色的背景故事、性格特点等信息..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCharacterDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingCharacter" @click="handleSaveCharacter">
          {{ isEditCharacter ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 添加关系对话框 -->
    <el-dialog
      v-model="showRelationDialog"
      title="添加人物关系"
      width="400px"
    >
      <el-form ref="relationFormRef" :model="relationForm" :rules="relationRules" label-position="top">
        <el-form-item label="关联角色" prop="targetId">
          <el-select v-model="relationForm.targetId" placeholder="选择关联角色" class="full-width">
            <el-option
              v-for="char in otherCharacters"
              :key="char.id"
              :label="char.name"
              :value="char.id"
            >
              <div class="character-option">
                <el-avatar 
                  :size="24" 
                  :src="char.avatarUrl"
                  :style="{ background: char.color || '#6366f1' }"
                >
                  {{ char.name.charAt(0) }}
                </el-avatar>
                <span>{{ char.name }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="关系类型" prop="relationType">
          <el-select v-model="relationForm.relationType" placeholder="选择关系类型" class="full-width">
            <el-option label="朋友" value="朋友" />
            <el-option label="恋人" value="恋人" />
            <el-option label="夫妻" value="夫妻" />
            <el-option label="父子" value="父子" />
            <el-option label="母子" value="母子" />
            <el-option label="兄弟姐妹" value="兄弟姐妹" />
            <el-option label="师徒" value="师徒" />
            <el-option label="敌人" value="敌人" />
            <el-option label="同事" value="同事" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="关系描述">
          <el-input
            v-model="relationForm.description"
            type="textarea"
            :rows="2"
            placeholder="描述两人之间的关系..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showRelationDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingRelation" @click="handleSaveRelation">
          添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {ArrowLeft, Camera, Delete, Edit, More, Plus, Refresh, User} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  createCharacter,
  createRelation,
  deleteCharacter,
  deleteRelation,
  getCharacterGraph,
  listCharacterRelations,
  type NovelCharacter,
  type NovelCharacterRelation,
  updateCharacter
} from '@/api/novel-character'

const route = useRoute()
const router = useRouter()
const novelId = computed(() => Number(route.params.id))

const novelTitle = ref('')
const graphMode = ref<'force' | 'circular'>('force')
const selectedCharacter = ref<NovelCharacter | null>(null)
const graphContainer = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null
const loading = ref(false)

// 预设颜色
const presetColors = [
  '#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4', '#ffeaa7',
  '#dfe6e9', '#fd79a8', '#a29bfe', '#00b894', '#e17055',
  '#74b9ff', '#55efc4', '#ff7675', '#fab1a0', '#fdcb6e'
]

// 关系类型颜色映射
const relationColorMap: Record<string, string> = {
  '朋友': '#67C23A',
  '恋人': '#E6A23C',
  '夫妻': '#F56C6C',
  '父子': '#409EFF',
  '母子': '#409EFF',
  '兄弟姐妹': '#409EFF',
  '师徒': '#909399',
  '敌人': '#F56C6C',
  '同事': '#8E44AD',
  '其他': '#606266'
}

// 上传请求头
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('accessToken')
  return {
    'satoken': token || ''
  }
})

// 角色数据
const characters = ref<NovelCharacter[]>([])
const relations = ref<NovelCharacterRelation[]>([])
const characterRelations = ref<NovelCharacterRelation[]>([])

// 角色对话框
const showCharacterDialog = ref(false)
const isEditCharacter = ref(false)
const savingCharacter = ref(false)
const characterFormRef = ref<FormInstance>()
const editingCharacterId = ref<number | null>(null)

const characterForm = ref({
  name: '',
  roleType: 'SUPPORTING',
  gender: '',
  age: undefined as number | undefined,
  identity: '',
  color: '#6366f1',
  background: '',
  avatarUrl: ''
})

const characterRules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 1, max: 20, message: '名称长度在 1 到 20 个字符', trigger: 'blur' }
  ]
}

// 关系对话框
const showRelationDialog = ref(false)
const savingRelation = ref(false)
const relationFormRef = ref<FormInstance>()

const relationForm = ref({
  targetId: null as number | null,
  relationType: '',
  description: ''
})

const relationRules: FormRules = {
  targetId: [{ required: true, message: '请选择关联角色', trigger: 'change' }],
  relationType: [{ required: true, message: '请选择关系类型', trigger: 'change' }]
}

const otherCharacters = computed(() => {
  if (!selectedCharacter.value) return []
  return characters.value.filter(c => c.id !== selectedCharacter.value?.id)
})

// 头像上传相关
const handleAvatarSuccess = (response: any) => {
  if (response.code === 200 && response.data) {
    characterForm.value.avatarUrl = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const beforeAvatarUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    ElMessage.error('只支持 JPG 或 PNG 格式的图片')
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
  }
  return isJpgOrPng && isLt2M
}

// 加载人物图谱数据
const loadCharacterGraph = async () => {
  loading.value = true
  try {
    const res = await getCharacterGraph(novelId.value)
    characters.value = res.data.characters || []
    relations.value = res.data.relations || []
    nextTick(() => updateGraph())
  } catch (error) {
    ElMessage.error('加载人物图谱失败')
  } finally {
    loading.value = false
  }
}

// 加载角色关系
const loadCharacterRelations = async (characterId: number) => {
  try {
    const res = await listCharacterRelations(characterId)
    characterRelations.value = res.data || []
  } catch (error) {
    characterRelations.value = []
  }
}

// 获取目标角色
const getTargetCharacter = (targetId: number) => {
  return characters.value.find(c => c.id === targetId)
}

// 获取角色类型标签
const getRoleLabel = (roleType?: string) => {
  const map: Record<string, string> = {
    'PROTAGONIST': '主角',
    'SUPPORTING': '配角',
    'ANTAGONIST': '反派',
    'OTHER': '其他'
  }
  return map[roleType || ''] || '其他'
}

// 生成图谱数据
const generateGraphData = () => {
  const nodes = characters.value.map(char => ({
    id: String(char.id),
    name: char.name,
    symbolSize: char.roleType === 'PROTAGONIST' ? 60 : 45,
    itemStyle: { color: char.color || '#6366f1' },
    label: {
      show: true,
      fontSize: 14,
      fontWeight: 'bold' as const
    }
  }))

  const links: any[] = []
  const linkSet = new Set<string>()

  relations.value.forEach(rel => {
    const linkKey = [rel.characterId, rel.targetId].sort().join('-')
    if (!linkSet.has(linkKey)) {
      linkSet.add(linkKey)
      const relationColor = relationColorMap[rel.relationType] || '#909399'
      links.push({
        source: String(rel.characterId),
        target: String(rel.targetId),
        value: rel.relationType,
        lineStyle: {
          color: relationColor,
          width: 2
        },
        label: {
          show: true,
          formatter: rel.relationType,
          fontSize: 11,
          color: relationColor
        }
      })
    }
  })

  return { nodes, links }
}

// 初始化图表
const initChart = () => {
  if (!graphContainer.value) return

  chartInstance = echarts.init(graphContainer.value)
  updateGraph()

  // 点击节点选中角色
  chartInstance.on('click', (params: any) => {
    if (params.dataType === 'node') {
      const char = characters.value.find(c => String(c.id) === params.data.id)
      if (char) {
        selectCharacter(char)
      }
    }
  })

  // 响应式
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
}

// 更新图表
const updateGraph = () => {
  if (!chartInstance) return

  const { nodes, links } = generateGraphData()

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        if (params.dataType === 'node') {
          const char = characters.value.find(c => String(c.id) === params.data.id)
          return char ? `${char.name}<br/>${getRoleLabel(char.roleType)}` : params.data.name
        }
        return `${params.data.source} - ${params.data.target}<br/>关系：${params.data.value}`
      }
    },
    animationDuration: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: graphMode.value,
        data: nodes,
        links: links,
        roam: true,
        draggable: true,
        label: {
          position: 'bottom'
        },
        force: {
          repulsion: 300,
          edgeLength: 150,
          gravity: 0.1
        },
        circular: {
          rotateLabel: false
        },
        lineStyle: {
          curveness: 0.2
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 4
          }
        }
      }
    ]
  }

  chartInstance.setOption(option, true)
}

// 方法
const goBack = () => {
  // 返回上一页（从创作页跳转过来时，返回创作页）
  router.back()
}

const selectCharacter = async (char: NovelCharacter) => {
  selectedCharacter.value = char
  await loadCharacterRelations(char.id)
  // 高亮图表中的节点
  if (chartInstance) {
    chartInstance.dispatchAction({
      type: 'highlight',
      seriesIndex: 0,
      name: String(char.id)
    })
  }
}

const roleTagType = (roleType?: string) => {
  const map: Record<string, any> = {
    'PROTAGONIST': 'danger',
    'SUPPORTING': 'primary',
    'ANTAGONIST': 'warning',
    'OTHER': 'info'
  }
  return map[roleType || ''] || ''
}

const openAddCharacter = () => {
  isEditCharacter.value = false
  editingCharacterId.value = null
  characterForm.value = {
    name: '',
    roleType: 'SUPPORTING',
    gender: '',
    age: undefined,
    identity: '',
    color: '#6366f1',
    background: '',
    avatarUrl: ''
  }
  showCharacterDialog.value = true
}

const openEditCharacter = (char: NovelCharacter) => {
  isEditCharacter.value = true
  editingCharacterId.value = char.id
  characterForm.value = {
    name: char.name,
    roleType: char.roleType || 'SUPPORTING',
    gender: char.gender || '',
    age: char.age,
    identity: char.identity || '',
    color: char.color || '#6366f1',
    background: char.background || '',
    avatarUrl: char.avatarUrl || ''
  }
  showCharacterDialog.value = true
}

const handleSaveCharacter = async () => {
  const valid = await characterFormRef.value?.validate().catch(() => false)
  if (!valid) return

  savingCharacter.value = true
  try {
    if (isEditCharacter.value && editingCharacterId.value) {
      await updateCharacter(editingCharacterId.value, {
        name: characterForm.value.name,
        roleType: characterForm.value.roleType,
        gender: characterForm.value.gender,
        age: characterForm.value.age,
        identity: characterForm.value.identity,
        color: characterForm.value.color,
        background: characterForm.value.background,
        avatarUrl: characterForm.value.avatarUrl
      })
      ElMessage.success('更新成功')
    } else {
      await createCharacter({
        novelId: novelId.value,
        name: characterForm.value.name,
        roleType: characterForm.value.roleType,
        gender: characterForm.value.gender,
        age: characterForm.value.age,
        identity: characterForm.value.identity,
        color: characterForm.value.color,
        background: characterForm.value.background,
        avatarUrl: characterForm.value.avatarUrl
      })
      ElMessage.success('添加成功')
    }
    showCharacterDialog.value = false
    await loadCharacterGraph()
  } catch (error) {
    ElMessage.error(isEditCharacter.value ? '更新失败' : '添加失败')
  } finally {
    savingCharacter.value = false
  }
}

const confirmDeleteCharacter = async (char: NovelCharacter) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色「${char.name}」吗？`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteCharacter(char.id)
    if (selectedCharacter.value?.id === char.id) {
      selectedCharacter.value = null
    }
    ElMessage.success('删除成功')
    await loadCharacterGraph()
  } catch {
    // 取消
  }
}

const openAddRelation = () => {
  relationForm.value = {
    targetId: null,
    relationType: '',
    description: ''
  }
  showRelationDialog.value = true
}

const handleSaveRelation = async () => {
  const valid = await relationFormRef.value?.validate().catch(() => false)
  if (!valid || !selectedCharacter.value) return

  savingRelation.value = true
  try {
    await createRelation({
      novelId: novelId.value,
      characterId: selectedCharacter.value.id,
      targetId: relationForm.value.targetId!,
      relationType: relationForm.value.relationType,
      description: relationForm.value.description
    })
    savingRelation.value = false
    showRelationDialog.value = false
    ElMessage.success('关系添加成功')
    await loadCharacterGraph()
    await loadCharacterRelations(selectedCharacter.value.id)
  } catch (error) {
    savingRelation.value = false
    ElMessage.error('添加关系失败')
  }
}

const removeRelation = async (relationId: number) => {
  try {
    await deleteRelation(relationId)
    ElMessage.success('关系已删除')
    await loadCharacterGraph()
    if (selectedCharacter.value) {
      await loadCharacterRelations(selectedCharacter.value.id)
    }
  } catch (error) {
    ElMessage.error('删除关系失败')
  }
}

// 监听角色数据变化
watch(characters, () => {
  nextTick(() => updateGraph())
}, { deep: true })

onMounted(() => {
  loadCharacterGraph()
  nextTick(() => {
    initChart()
  })
})
</script>

<style scoped>
.character-graph-page {
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
}

/* 顶部标题栏 */
.graph-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid #e8eaed;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  font-size: 16px;
  color: #1a73e8;
  font-weight: 600;
  background: transparent !important;
  border: none !important;
  padding: 8px 12px;
}

.back-btn:hover {
  color: #1557b0;
  background: transparent !important;
}

.header-title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #202124;
}

.subtitle {
  font-size: 13px;
  color: #9aa0a6;
}

/* 主体内容 */
.graph-body {
  flex: 1;
  display: flex;
  overflow: hidden;
  padding: 16px 20px 20px;
  gap: 16px;
}

/* 左侧角色列表 */
.character-sidebar {
  width: 260px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e8eaed;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #202124;
}

.count {
  font-size: 13px;
  color: #9aa0a6;
}

.character-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.character-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.character-item:hover {
  background: #f8f9fa;
}

.character-item.active {
  background: #e8f0fe;
}

.character-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.character-name {
  font-size: 14px;
  font-weight: 500;
  color: #202124;
}

.character-role {
  font-size: 12px;
  color: #9aa0a6;
}

/* 中间关系图谱 */
.graph-main {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e8eaed;
}

.graph-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafbfc;
}

.graph-container {
  flex: 1;
  position: relative;
  min-height: 0;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
}

/* 右侧角色详情 */
.character-detail {
  width: 340px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e8eaed;
}

.character-detail.empty {
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
}

.detail-title {
  flex: 1;
}

.detail-title h3 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #202124;
}

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 12px;
  font-size: 13px;
  font-weight: 600;
  color: #5f6368;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: #9aa0a6;
}

.info-item .value {
  font-size: 14px;
  font-weight: 500;
  color: #202124;
}

.description {
  font-size: 14px;
  color: #5f6368;
  line-height: 1.7;
  margin: 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.relations-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.relation-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.relation-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.relation-target {
  font-size: 14px;
  font-weight: 500;
  color: #202124;
}

.add-relation-btn {
  margin-top: 8px;
}

/* 表单样式 */
.form-row {
  display: flex;
  gap: 16px;
}

.form-row .half {
  flex: 1;
}

.full-width {
  width: 100%;
}

.character-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 头像上传 */
.avatar-upload-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.avatar-display-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar-uploader {
  cursor: pointer;
}

.avatar-circle {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #e4e7ed;
  transition: all 0.3s ease;
  background: #f5f7fa;
}

.avatar-circle:hover {
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.avatar-placeholder .el-icon {
  color: #fff;
  opacity: 0.9;
}

.avatar-hover-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: #fff;
  font-size: 12px;
}

.avatar-hover-mask .el-icon {
  color: #fff;
}

.avatar-circle:hover .avatar-hover-mask {
  opacity: 1;
}

.avatar-actions {
  display: flex;
  justify-content: center;
}

/* 弹窗样式优化 */
.character-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #e4e7ed;
  margin-right: 0;
}

.character-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.character-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.character-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #e4e7ed;
}

/* 表单样式优化 */
.character-form .el-form-item {
  margin-bottom: 20px;
}

.character-form .el-form-item__label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  padding-bottom: 8px;
}

/* 头像区域居中 */
.character-form .el-form-item:first-child .el-form-item__content {
  display: flex;
  justify-content: center;
}

/* 输入框样式 */
.character-form .el-input__inner,
.character-form .el-textarea__inner {
  border-radius: 8px;
}

.character-form .el-input__inner:hover,
.character-form .el-textarea__inner:hover {
  border-color: #409eff;
}

/* 单选按钮组样式 */
.character-form .el-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.character-form .el-radio-button__inner {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  padding: 8px 16px;
}

.character-form .el-radio-button:first-child .el-radio-button__inner {
  border-radius: 6px;
}

.character-form .el-radio-button:last-child .el-radio-button__inner {
  border-radius: 6px;
}

.character-form .el-radio-button__original-radio:checked + .el-radio-button__inner {
  background-color: #409eff;
  border-color: #409eff;
  box-shadow: none;
}

/* 选择器样式 */
.character-form .el-select .el-input__inner {
  border-radius: 8px;
}

/* 数字输入框样式 */
.character-form .el-input-number .el-input__inner {
  border-radius: 8px;
}

/* 颜色选择器 */
.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.preset-colors {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  max-width: 320px;
}

.preset-color {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.preset-color:hover {
  transform: scale(1.15);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.preset-color.active {
  border-color: #fff;
  box-shadow: 0 0 0 2px #409eff, 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 文本域样式 */
.character-form .el-textarea__inner {
  min-height: 100px;
  resize: vertical;
}

/* 表单行布局 */
.form-row {
  display: flex;
  gap: 20px;
}

.form-row .half {
  flex: 1;
}

.full-width {
  width: 100%;
}

/* 底部按钮样式 */
.character-dialog :deep(.el-dialog__footer .el-button) {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 500;
}

.character-dialog :deep(.el-dialog__footer .el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.character-dialog :deep(.el-dialog__footer .el-button--primary:hover) {
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}
</style>
