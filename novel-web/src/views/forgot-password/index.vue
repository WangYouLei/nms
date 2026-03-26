<template>
  <div class="forgot-password-page min-h-screen flex items-center justify-center bg-gradient-primary py-12">
    <div class="w-full max-w-md px-6">
      <!-- Logo 和 标题 -->
      <div class="text-center mb-8">
        <div class="flex justify-center mb-4">
          <img src="/logo.png" alt="NMS Logo" class="w-16 h-16 object-contain rounded-lg" />
        </div>
        <h1 class="text-2xl font-bold text-white">找回密码</h1>
        <p class="text-white/80 mt-2">通过邮箱验证重置您的密码</p>
      </div>
      
      <div class="bg-white/10 backdrop-blur-md rounded-2xl p-8 shadow-lg">
        <!-- 步骤指示器 -->
        <div class="flex items-center justify-center mb-6">
          <div class="flex items-center">
            <div 
              class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold"
              :class="step >= 1 ? 'bg-white text-gray-800' : 'bg-white/30 text-white'"
            >
              1
            </div>
            <div class="w-12 h-0.5 mx-1" :class="step >= 2 ? 'bg-white' : 'bg-white/30'"></div>
            <div 
              class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold"
              :class="step >= 2 ? 'bg-white text-gray-800' : 'bg-white/30 text-white'"
            >
              2
            </div>
            <div class="w-12 h-0.5 mx-1" :class="step >= 3 ? 'bg-white' : 'bg-white/30'"></div>
            <div 
              class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold"
              :class="step >= 3 ? 'bg-white text-gray-800' : 'bg-white/30 text-white'"
            >
              3
            </div>
          </div>
        </div>

        <!-- 步骤1: 选择角色和输入账号 -->
        <div v-if="step === 1">
          <el-form ref="step1FormRef" :model="formData" :rules="step1Rules" label-position="top">
            <!-- 角色选择 -->
            <el-form-item label="选择角色">
              <div class="grid grid-cols-2 gap-3 w-full">
                <div 
                  class="p-3 text-center rounded-xl border-2 cursor-pointer transition-all duration-200"
                  :class="formData.role === 'VISITOR' 
                    ? 'border-white/50 bg-white/20 text-white' 
                    : 'border-white/20 bg-white/10 text-white/70 hover:bg-white/20'"
                  @click="formData.role = 'VISITOR'"
                >
                  <el-icon :size="24"><User /></el-icon>
                  <p class="mt-1 text-sm">访客</p>
                </div>
                <div 
                  class="p-3 text-center rounded-xl border-2 cursor-pointer transition-all duration-200"
                  :class="formData.role === 'AUTHOR' 
                    ? 'border-white/50 bg-white/20 text-white' 
                    : 'border-white/20 bg-white/10 text-white/70 hover:bg-white/20'"
                  @click="formData.role = 'AUTHOR'"
                >
                  <el-icon :size="24"><Edit /></el-icon>
                  <p class="mt-1 text-sm">作者</p>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="账号" prop="account">
              <el-input 
                v-model="formData.account" 
                placeholder="请输入您的账号"
                class="bg-white/20 backdrop-blur-sm"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input 
                v-model="formData.email" 
                placeholder="请输入注册时使用的邮箱"
                class="bg-white/20 backdrop-blur-sm"
              >
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button 
                type="primary" 
                class="w-full rounded-xl bg-white text-gray-800 hover:bg-gray-100 font-bold py-3" 
                :loading="loading"
                @click="goToStep2"
              >
                下一步
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤2: 验证邮箱 -->
        <div v-if="step === 2">
          <el-form ref="step2FormRef" :model="formData" :rules="step2Rules" label-position="top">
            <div class="bg-white/10 rounded-xl p-4 mb-4">
              <p class="text-white/80 text-sm">验证码将发送至邮箱：</p>
              <p class="text-white font-medium">{{ maskEmail(formData.email) }}</p>
            </div>

            <el-form-item label="验证码" prop="code">
              <div class="flex gap-2 w-full">
                <el-input 
                  v-model="formData.code" 
                  placeholder="请输入验证码"
                  class="flex-1 bg-white/20 backdrop-blur-sm"
                  :maxlength="6"
                >
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
                <el-button 
                  :disabled="countdown > 0"
                  :loading="sendingCode"
                  class="bg-white/20 text-white hover:bg-white/30"
                  @click="sendCode"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button 
                type="primary" 
                class="w-full rounded-xl bg-white text-gray-800 hover:bg-gray-100 font-bold py-3" 
                :loading="loading"
                @click="goToStep3"
              >
                下一步
              </el-button>
            </el-form-item>

            <el-button 
              text 
              class="w-full text-white/80 hover:text-white"
              @click="step = 1"
            >
              返回上一步
            </el-button>
          </el-form>
        </div>

        <!-- 步骤3: 设置新密码 -->
        <div v-if="step === 3">
          <el-form ref="step3FormRef" :model="formData" :rules="step3Rules" label-position="top">
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="formData.newPassword" 
                type="password"
                placeholder="请输入新密码（6-20位）"
                show-password
                class="bg-white/20 backdrop-blur-sm"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="formData.confirmPassword" 
                type="password"
                placeholder="请再次输入新密码"
                show-password
                class="bg-white/20 backdrop-blur-sm"
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
                @click="handleSubmit"
              >
                重置密码
              </el-button>
            </el-form-item>

            <el-button 
              text 
              class="w-full text-white/80 hover:text-white"
              @click="step = 2"
            >
              返回上一步
            </el-button>
          </el-form>
        </div>
      </div>
      
      <!-- 返回登录链接 -->
      <div class="text-center mt-6">
        <router-link to="/login" class="text-white/80 hover:text-white text-sm">
          <el-icon class="mr-1"><ArrowLeft /></el-icon>
          返回登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Edit, Message, Key, Lock, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { sendEmailCode, verifyEmailCode } from '@/api/common'
import { updateVisitorPasswordByEmail, updateAuthorPasswordByEmail } from '@/api'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()

const step = ref(1)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const step1FormRef = ref<FormInstance>()
const step2FormRef = ref<FormInstance>()
const step3FormRef = ref<FormInstance>()

const formData = reactive({
  role: 'VISITOR' as 'VISITOR' | 'AUTHOR',
  account: '',
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const step1Rules: FormRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const step2Rules: FormRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== formData.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const step3Rules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 邮箱脱敏显示
const maskEmail = (email: string) => {
  if (!email) return ''
  const [name, domain] = email.split('@')
  const maskedName = name.length > 2 
    ? name[0] + '***' + name[name.length - 1] 
    : name[0] + '***'
  return `${maskedName}@${domain}`
}

// 发送验证码
const sendCode = async () => {
  if (!formData.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  sendingCode.value = true
  try {
    await sendEmailCode(formData.email)
    ElMessage.success('验证码已发送')
    // 开始倒计时
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer!)
        countdownTimer = null
      }
    }, 1000)
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

// 步骤1 -> 步骤2
const goToStep2 = async () => {
  const valid = await step1FormRef.value?.validate().catch(() => false)
  if (!valid) return
  
  step.value = 2
}

// 步骤2 -> 步骤3
const goToStep3 = async () => {
  const valid = await step2FormRef.value?.validate().catch(() => false)
  if (!valid) return
  
  // 验证验证码
  loading.value = true
  try {
    await verifyEmailCode(formData.email, formData.code)
    step.value = 3
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || '验证码错误')
  } finally {
    loading.value = false
  }
}

// 提交重置密码
const handleSubmit = async () => {
  const valid = await step3FormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = {
      account: formData.account,
      email: formData.email,
      code: formData.code,
      newPassword: formData.newPassword
    }

    if (formData.role === 'VISITOR') {
      await updateVisitorPasswordByEmail(data)
    } else {
      await updateAuthorPasswordByEmail(data)
    }

    ElMessage.success('密码重置成功，请重新登录')
    router.push('/login')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || '密码重置失败')
  } finally {
    loading.value = false
  }
}

// 清理定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>