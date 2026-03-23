<template>
  <div class="author-register-page min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12">
    <div class="w-full max-w-md px-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-8">
        <div class="text-center mb-8">
          <el-icon :size="48" color="#409eff"><Edit /></el-icon>
          <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200 mt-4">作者注册</h1>
          <p class="text-gray-500 dark:text-gray-400 mt-2">成为作者，创作属于你的故事</p>
        </div>
        
        <el-form ref="formRef" :model="formData" :rules="rules" label-position="top">
          <el-form-item label="昵称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入昵称" />
          </el-form-item>
          
          <el-form-item label="账号（手机号）" prop="account">
            <el-input v-model="formData.account" placeholder="请输入手机号" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" class="w-full" :loading="loading" @click="handleSubmit">
              注册
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="text-center mt-4 text-sm text-gray-500 dark:text-gray-400">
          已有账号？<router-link to="/author/login" class="text-primary hover:underline">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { authorRegister } from '@/api'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)

const formData = reactive({
  name: '',
  account: '',
  password: '',
  email: '',
  code: '',
  token: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await authorRegister({
      name: formData.name,
      account: formData.account,
      password: formData.password,
      email: formData.email,
      code: formData.code,
      token: formData.token
    })
    ElMessage.success('注册成功')
    router.push('/author/login')
  } catch (error) {
    console.error('Register failed:', error)
  } finally {
    loading.value = false
  }
}
</script>