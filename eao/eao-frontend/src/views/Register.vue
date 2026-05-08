<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'
import { unwrapData } from '@/util/result'

const router = useRouter()

const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: ''
})

const loading = ref(false)

async function onSubmit() {
  if (!form.username?.trim() || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const raw = await register({
      username: form.username.trim(),
      // 与后端 SysUser 对齐时若只接收 passwordHash，需后端增加明文密码字段或单独 DTO
      password: form.password,
      realName: form.realName?.trim() || undefined,
      phone: form.phone?.trim() || undefined
    })
    unwrapData(raw)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    if (e?.elMessageNotified) return
    if (e != null && typeof e === 'object' && e.isBusinessError) {
      ElMessage.error(e.message || '注册失败')
    } else {
      const data = e?.response?.data
      const msg =
        data && typeof data === 'object' && data.message != null
          ? String(data.message)
          : '注册失败，请检查接口或表单项'
      ElMessage.error(msg)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <h1 class="title">用户注册</h1>
      <el-form :model="form" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" autocomplete="username" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="密码"
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" placeholder="真实姓名（选填）" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="form.phone" placeholder="手机（选填）" />
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="onSubmit">
          注册
        </el-button>
        <div class="extra">
          <router-link to="/login" class="link">已有账号，去登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #e8f0ff 0%, #f5f7fa 50%, #eef3fb 100%);
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
}
.register-card {
  width: 100%;
  max-width: 420px;
  padding: 32px 28px 36px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 28px rgba(28, 85, 200, 0.12);
  border: 1px solid #e9eef5;
}
.title {
  margin: 0 0 24px;
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}
.submit-btn {
  width: 100%;
  height: 40px;
  margin-top: 8px;
}
.extra {
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
