<template>
  <div class="manager-login-page min-h-screen flex items-center justify-center bg-gray-900">
    <div class="w-full max-w-md px-6">
      <div class="bg-gray-800 rounded-lg shadow-md p-8">
        <div class="text-center mb-8">
          <el-icon :size="48" color="#409eff"><Setting /></el-icon>
          <h1 class="text-2xl font-bold text-white mt-4">管理后台</h1>
        </div>
        
        <el-form ref="formRef" :model="formData" :rules="rules" label-position="top">
          <el-form-item label="账号" prop="account">
            <el-input v-model="formData.account" placeholder="请输入账号" size="large" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="formData.password" 
              type="password" 
              placeholder="请输入密码" 
              size="large"
              show-password 
              @keyup.enter="handleLogin" 
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" class="w-full" size="large" :loading="loading" @click="handleLogin">
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="text-center mt-4">
          <router-link to="/login" class="text-gray-400 hover:text-white text-sm">返回登录选择</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { managerLogin } from '@/api'
import { useUserStore } from '@/stores'
import { parseJwtToken } from '@/utils/jwt'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const formData = reactive({
  account: '',
  password: ''
})

const rules: FormRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const token = (await managerLogin(formData.account, formData.password)).data
    
    // 检查 token 是否有效
    if (!token) {
      ElMessage.error('登录失败：未获取到有效token')
      return
    }
    
    // 从 token 中解析用户信息
    const userInfo = parseJwtToken(token)
    if (!userInfo) {
      ElMessage.error('登录失败：无法解析用户信息')
      return
    }
    
    userStore.login({
      token: token,
      userInfo: userInfo,
      role: 'MANAGER'
    })
    
    ElMessage.success('登录成功')
    router.push('/manager/dashboard')
  } catch (error) {
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}
</script>