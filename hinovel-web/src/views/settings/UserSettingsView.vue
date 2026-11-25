<template>
    <div class="user-settings-page">
        <header class="page-header">
            <div>
                <h1>默认设置</h1>
                <p>这些设置适用于所有作品。你也可以在小说设置中单独调整。</p>
            </div>
        </header>
        <div v-if="loadFailed" class="load-error" role="status">
            默认设置加载失败
            <el-button @click="loadSettings">重新加载</el-button>
        </div>
        <div v-else v-loading="loading" class="settings-container">
            <el-form label-position="top" class="settings-form">
                <NovelAiSettingsForm v-model="settingsForm" />
            </el-form>

            <!-- 底部操作栏 -->
            <div class="settings-footer">
                <div class="footer-actions">
                    <el-button @click="resetToDefault">恢复默认</el-button>
                    <el-button type="primary" :loading="saving" @click="handleSave"> 保存设置 </el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
    createDefaultSettings,
    getSystemDefaultSettings,
    getUserDefaultSettings,
    type NovelSettings,
    saveUserDefaultSettings,
} from '@/api/novel-settings'
import NovelAiSettingsForm from '@/components/novel/NovelAiSettingsForm.vue'

const loading = ref(true)
const loadFailed = ref(false)
const saving = ref(false)
const settingsForm = ref<NovelSettings>(createDefaultSettings())

/**
 * 加载用户默认配置
 */
const loadSettings = async () => {
    loading.value = true
    loadFailed.value = false
    try {
        const res = await getUserDefaultSettings()
        settingsForm.value = res.data
    } catch (error) {
        loadFailed.value = true
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
 * 恢复为系统默认值（从后端获取，确保默认值权威来源唯一）
 */
const resetToDefault = async () => {
    try {
        const res = await getSystemDefaultSettings()
        settingsForm.value = res.data
        ElMessage.info('已恢复为默认值，请点击保存生效')
    } catch (error) {
        ElMessage.error('获取系统默认配置失败')
    }
}

onMounted(() => {
    loadSettings()
})
</script>

<style scoped>
.user-settings-page {
    max-width: 830px;
    margin-inline: auto;
}

.page-header h1 {
    font: 600 32px/1.3 var(--display-font);
}

.page-header p {
    margin-top: 8px;
    color: var(--muted);
    font-size: 14px;
}

.settings-footer,
.load-error {
    padding: 20px 0;
}

.footer-actions {
    display: flex;
    justify-content: end;
}
</style>
