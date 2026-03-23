<template>
  <div class="register-page min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12">
    <div class="w-full max-w-md px-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-8">
        <!-- 标题 -->
        <div class="text-center mb-8">
          <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">访客注册</h1>
          <p class="text-gray-500 dark:text-gray-400 mt-2">创建账号，开始阅读之旅</p>
        </div>
        
        <!-- 注册表单 -->
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
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="formData.confirmPassword" type="password" placeholder="请确认密码" show-password />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>
          
          <el-form-item label="邮箱验证码" prop="code">
            <div class="flex gap-2">
              <el-input v-model="formData.code" placeholder="请输入验证码" class="flex-1" />
              <el-button :disabled="countdown > 0" :loading="sendingCode" @click="sendCode">
                {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
          
          <el-form-item label="图形验证码" prop="captchaCode">
            <div class="flex gap-2">
              <el-input v-model="formData.captchaCode" placeholder="请输入验证码" class="flex-1" />
              <div 
                class="h-8 w-24 bg-gray-100 dark:bg-gray-700 rounded cursor-pointer flex items-center justify-center overflow-hidden"
                @click="refreshCaptcha"
              >
                <img v-if="captchaImage" :src="captchaImage" class="h-full w-full object-cover" />
                <span v-else class="text-xs text-gray-400">点击刷新</span>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" class="w-full" :loading="loading" @click="handleSubmit">
              注册
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 登录链接 -->
        <div class="text-center mt-4 text-sm text-gray-500 dark:text-gray-400">
          已有账号？<router-link to="/login" class="text-primary hover:underline">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { generateCaptcha, sendEmailCode, visitorRegister } from '@/api'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const captchaImage = ref('')
const captchaToken = ref('')

const formData = reactive({
  name: '',
  account: '',
  password: '',
  confirmPassword: '',
  email: '',
  code: '',
  captchaCode: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== formData.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

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
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  try {
    const res = await generateCaptcha()
    captchaImage.value = res.data?.image || ''
    captchaToken.value = res.data?.token || ''
  } catch (error) {
    console.error('Failed to refresh captcha:', error)
  }
}

const sendCode = async () => {
  if (!formData.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  sendingCode.value = true
  try {
    await sendEmailCode(formData.email)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('Failed to send code:', error)
  } finally {
    sendingCode.value = false
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await visitorRegister({
      name: formData.name,
      account: formData.account,
      password: formData.password,
      email: formData.email,
      code: formData.code,
      token: captchaToken.value
    })
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>