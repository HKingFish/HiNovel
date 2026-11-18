<template>
    <AuthLayout title="登录创作室">
        <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            novalidate
            @submit.prevent="handleLogin"
        >
            <el-form-item label="用户名或邮箱" prop="account">
                <el-input v-model.trim="form.account" autocomplete="username" size="large" type="text" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
                <el-input
                    v-model="form.password"
                    autocomplete="current-password"
                    show-password
                    size="large"
                    type="password"
                />
            </el-form-item>
            <el-button :loading="loading" class="submit-btn" native-type="submit" size="large" type="primary"
                >登录</el-button
            >
        </el-form>
        <p class="form-footer">
            还没有账号？
            <router-link to="/register">注册账号</router-link>
        </p>
    </AuthLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import AuthLayout from './AuthLayout.vue'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
    account: '',
    password: '',
})

const rules: FormRules = {
    account: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid || loading.value) return

    loading.value = true
    try {
        await authStore.login(form)
        ElMessage.success('登录成功')
        await router.replace('/novel')
    } catch {
        // 请求错误由 HTTP 拦截器提示，保留表单以便重试。
    } finally {
        loading.value = false
    }
}
</script>
