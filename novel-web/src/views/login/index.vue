<template>
  <div class="login-page min-h-screen flex items-center justify-center bg-gradient-primary py-12">
    <div class="w-full max-w-md px-6">
      <!-- Logo 和 标题 -->
      <div class="text-center mb-8">
        <div class="flex justify-center mb-4">
          <img src="/logo.png" alt="NMS Logo" class="w-16 h-16 object-contain rounded-lg" />
        </div>
        <h1 class="text-2xl font-bold text-white">NMS</h1>
        <p class="text-white/80 mt-2">选择登录角色</p>
      </div>
      
      <div class="bg-white/10 backdrop-blur-md rounded-2xl p-8 shadow-lg">
        <!-- 角色选择 -->
        <div class="grid grid-cols-3 gap-4 mb-6">
          <div 
            v-for="role in roles" 
            :key="role.value"
            class="p-4 text-center rounded-xl border-2 cursor-pointer transition-all duration-200"
            :class="selectedRole === role.value 
              ? 'border-white/30 bg-white/20 backdrop-blur-sm text-white shadow-lg' 
              : 'border-white/20 bg-white/10 text-white/80 hover:bg-white/20'"
            @click="selectedRole = role.value"
          >
            <el-icon :size="32">
              <component :is="role.icon" />
            </el-icon>
            <p class="mt-2 text-sm font-medium">{{ role.label }}</p>
          </div>
        </div>
        
        <!-- 登录表单 -->
        <el-form ref="formRef" :model="formData" :rules="rules" label-position="top">
          <el-form-item label="账号" prop="account">
            <el-input 
              v-model="formData.account" 
              placeholder="请输入账号" 
              class="bg-white/20 backdrop-blur-sm !text-white"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="formData.password" 
              type="password" 
              placeholder="请输入密码" 
              show-password 
              class="bg-white/20 backdrop-blur-sm !text-white"
              @keyup.enter="handleLogin" 
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="w-full rounded-xl bg-white text-gray-800 hover:bg-gray-100 font-bold py-3" 
              :loading="loading" 
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
          
          <!-- 忘记密码链接 -->
          <div class="text-center">
            <router-link to="/forgot-password" class="text-white/70 hover:text-white text-sm">
              忘记密码？
            </router-link>
          </div>
        </el-form>
        
         <!-- 注册链接 -->
        <div class="text-center mt-4 text-sm text-white/80">
          没有账号？
          <router-link v-if="selectedRole === 'VISITOR'" to="/visitor/register" class="text-white hover:text-white/80 underline">立即注册</router-link>
          <router-link v-else-if="selectedRole === 'AUTHOR'" to="/author/register" class="text-white hover:text-white/80 underline">立即注册</router-link>
          <span v-else class="line-through text-white/60">联系管理员注册</span> <!-- 对于管理员，直接登录而不是注册 -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Edit, Setting, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { visitorLogin, authorLogin, managerLogin } from '@/api'
import { useUserStore } from '@/stores'
import { parseJwtToken } from '@/utils/jwt'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const selectedRole = ref<'VISITOR' | 'AUTHOR' | 'MANAGER'>('VISITOR')

const formData = reactive({
  account: '',
  password: ''
})

const roles = [
  { value: 'VISITOR' as const, label: '访客', icon: User },
  { value: 'AUTHOR' as const, label: '作者', icon: Edit },
  { value: 'MANAGER' as const, label: '管理员', icon: Setting }
]

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    let token: string | undefined
    let redirectPath = '/home'
    
    switch (selectedRole.value) {
      case 'VISITOR':
        token = (await visitorLogin(formData.account, formData.password)).data
        redirectPath = '/home'
        break
      case 'AUTHOR':
        token = (await authorLogin(formData.account, formData.password)).data
        redirectPath = '/author/dashboard'
        break
      case 'MANAGER':
        token = (await managerLogin(formData.account, formData.password)).data
        redirectPath = '/manager/dashboard'
        break
    }
    
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
    
    // 保存登录信息
    userStore.login({
      token: token,
      userInfo: userInfo,
      role: selectedRole.value
    })

    // 等待用户头像被处理 - 我们等待userAvatar不再是默认值
    await new Promise<void>((resolve) => {
      // 立即检查状态（第一次调用）
      const currentAvatar = userStore.userAvatar
      if (currentAvatar && 
          currentAvatar !== '/default-avatar.png' && 
          (currentAvatar.startsWith('http') || currentAvatar.includes('minio'))) {
        resolve()
        return
      }

      const unsubscribe = userStore.$subscribe((_mutation, state) => {
        // 在每次状态更新时检查userAvatar
        const avatar = (state as { userAvatar?: string }).userAvatar
        if (avatar && 
            avatar !== '/default-avatar.png' && 
            (avatar.startsWith('http') || avatar.includes('minio'))) {
          setTimeout(() => {
            unsubscribe()
            resolve()
          }, 100)  // 确保DOM渲染更新
        }
      })
      
      // 超时保护，最多等待2秒
      setTimeout(() => {
        unsubscribe()
        resolve()
      }, 2000)
    })

    ElMessage.success('登录成功')
    
    // 跳转
    const redirect = route.query.redirect as string
    router.push(redirect || redirectPath)
  } catch (error) {
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}
</script>