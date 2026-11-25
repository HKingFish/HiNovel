<template>
    <AuthLayout title="创建账号">
        <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            novalidate
            @submit.prevent="handleRegister"
        >
            <el-form-item label="用户名" prop="username">
                <el-input v-model.trim="form.username" autocomplete="username" size="large" type="text" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
                <el-input v-model.trim="form.email" autocomplete="email" size="large" type="email" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
                <el-input
                    v-model="form.password"
                    autocomplete="new-password"
                    show-password
                    size="large"
                    type="password"
                />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                    v-model="form.confirmPassword"
                    autocomplete="new-password"
                    show-password
                    size="large"
                    type="password"
                />
            </el-form-item>
            <el-button :loading="loading" class="submit-btn" native-type="submit" size="large" type="primary"
                >注册</el-button
            >
        </el-form>
        <p class="form-footer">
            已有账号？
            <router-link to="/login">返回登录</router-link>
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
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
})

const validatePass2 = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
    if (value === '') {
        callback(new Error('请再次输入密码'))
    } else if (value !== form.password) {
        callback(new Error('两次输入密码不一致'))
    } else {
        callback()
    }
}

const rules: FormRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为 6 位', trigger: 'blur' },
    ],
    confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }],
}

const handleRegister = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid || loading.value) return

    loading.value = true
    try {
        await authStore.register({
            username: form.username,
            email: form.email,
            password: form.password,
        })
        ElMessage.success('注册成功，请登录')
        await router.replace('/login')
    } catch {
        // 请求错误由 HTTP 拦截器提示，保留表单以便重试。
    } finally {
        loading.value = false
    }
}
</script>
