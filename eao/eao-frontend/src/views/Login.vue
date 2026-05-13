<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'
import { unwrapData } from '@/util/result'

const userStore = useUserStore()

const route = useRoute()
const router = useRouter()

const form = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

async function onSubmit() {
  if (!form.username?.trim() || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  const tokenKey = import.meta.env.VITE_USER_TOKEN_KEY
  if (!tokenKey) {
    ElMessage.error('缺少环境变量 VITE_USER_TOKEN_KEY')
    return
  }
  loading.value = true
  try {
    const raw = await login({
      username: form.username.trim(),
      password: form.password
    })
    const payload = unwrapData(raw)
    if (!payload || typeof payload !== 'object' || !payload.token) {
      ElMessage.error('登录成功但未获取到 token，请检查 Result.data（LoginResponse）')
      return
    }
    localStorage.setItem(tokenKey, payload.token)
    userStore.setFromLoginResponse({
      userId: payload.userId,
      username: payload.username,
      realName: payload.realName
    })
    ElMessage.success('登录成功')
    const rawRedirect = route.query.redirect
    const nextPath =
      typeof rawRedirect === 'string' && rawRedirect.startsWith('/') ? rawRedirect : '/'
    router.replace(nextPath)
  } catch (e) {
    if (e?.elMessageNotified) return
    if (e != null && typeof e === 'object' && e.isBusinessError) {
      ElMessage.error(e.message || '登录失败')
    } else {
      const status = e?.response?.status
      const data = e?.response?.data
      const fromServer =
        data && typeof data === 'object' && data.message != null
          ? String(data.message)
          : typeof data === 'string' && data
            ? data
            : ''
      if (status === 401 || status === 403) {
        ElMessage.error(fromServer || '用户名或密码错误')
      } else if (status == null) {
        ElMessage.error('网络异常，请稍后重试')
      } else {
        ElMessage.error(fromServer || '登录失败，请重试')
      }
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-badge">高仁</div>
        <div>
          <div class="brand-line1">智能点检系统</div>
          <div class="brand-line2">天津高仁科技有限公司</div>
        </div>
      </div>
      <h1 class="login-title">用户登录</h1>
      <el-form :model="form" label-position="top" @submit.prevent="onSubmit" class="login-form">
        <el-form-item label="用户名" required>
          <el-input
            v-model="form.username"
            autocomplete="username"
            placeholder="请输入用户名"
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="current-password"
            placeholder="请输入密码"
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="onSubmit">
          登录
        </el-button>
        <div class="login-extra">
          <span class="login-tip">账号由系统统一导入，请联系管理员分配登录信息</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #e8f0ff 0%, #f5f7fa 50%, #eef3fb 100%);
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
}
.login-card {
  width: 100%;
  max-width: 400px;
  padding: 36px 32px 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 28px rgba(28, 85, 200, 0.12);
  border: 1px solid #e9eef5;
}
.login-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}
.brand-badge {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 6px;
  background: linear-gradient(135deg, #1c55c8, #2b61ca);
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}
.brand-line1 {
  font-size: 12px;
  color: #606266;
}
.brand-line2 {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-top: 2px;
}
.login-title {
  margin: 0 0 24px;
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}
.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}
.login-btn {
  width: 100%;
  height: 40px;
  margin-top: 8px;
}
.login-extra {
  margin-top: 16px;
  text-align: center;
}
.link {
  font-size: 14px;
  color: #409eff;
  text-decoration: none;
}
.link:hover {
  text-decoration: underline;
}
</style>
