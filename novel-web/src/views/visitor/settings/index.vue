<template>
  <div class="visitor-settings-page">
    <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200 mb-6">账号设置</h1>
    
    <div class="bg-white dark:bg-gray-800 rounded-lg p-6">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <el-form :model="userInfo" label-width="100px" class="max-w-md">
            <el-form-item label="头像">
              <div class="flex items-center gap-4">
                <el-avatar :size="64" :src="avatarUrl" />
                <el-upload
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :http-request="handleAvatarUpload"
                  accept=".jpg,.jpeg,.png"
                >
                  <el-button size="small">更换头像</el-button>
                </el-upload>
              </div>
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="userInfo.name" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userInfo.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveUserInfo">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="修改密码" name="password">
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" class="max-w-md">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changingPassword" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getVisitorInfo, updateVisitor, updateVisitorPassword, uploadFile } from '@/api'
import { useUserStore } from '@/stores'
import { FileUploadType } from '@/enums'
import { validateImageFile } from '@/utils/file-validator'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'

const userStore = useUserStore()

const activeTab = ref('info')
const saving = ref(false)
const changingPassword = ref(false)

const userInfo = reactive({
  name: '',
  email: '',
  avatar: ''
})

// 响应式头像URL（带预签名）
const avatarUrl = computed(() => userStore.userAvatar)

const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const fetchUserInfo = async () => {
  try {
    const res = await getVisitorInfo(userStore.userId)
    userInfo.name = res.data?.name || ''
    userInfo.email = res.data?.email || ''
    userInfo.avatar = res.data?.avatar || ''
  } catch (error) {
    console.error('Failed to fetch user info:', error)
  }
}

const beforeAvatarUpload = (file: File) => {
  // 头像使用2MB限制
  const error = validateImageFile(file, 2 * 1024 * 1024, '2MB')
  if (error) {
    ElMessage.error(error)
    return false
  }
  return true
}

const handleAvatarUpload = async (options: UploadRequestOptions) => {
  try {
    const res = await uploadFile(options.file as File, FileUploadType.VISITOR_AVATAR)
    userInfo.avatar = res.data
    ElMessage.success('上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const saveUserInfo = async () => {
  saving.value = true
  try {
    await updateVisitor({
      id: userStore.userId,
      name: userInfo.name,
      email: userInfo.email,
      avatar: userInfo.avatar
    })
    ElMessage.success('保存成功')
    // 更新 store 中的用户信息
    userStore.setUserInfo({
      id: userStore.userId,
      name: userInfo.name,
      account: userStore.userInfo?.account || '',
      avatar: userInfo.avatar
    })
  } catch (error) {
    console.error('Failed to save user info:', error)
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return
  
  changingPassword.value = true
  try {
    await updateVisitorPassword(userStore.userId, passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码修改成功')
    passwordFormRef.value?.resetFields()
  } catch (error) {
    console.error('Failed to change password:', error)
  } finally {
    changingPassword.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>