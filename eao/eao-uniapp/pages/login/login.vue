<template>
  <view class="page">
    <view class="hero">
      <text class="hero-title">设备点检</text>
      <text class="hero-sub">点检仪登录（账号与浏览器端相同）</text>
    </view>

    <view class="card">
      <view class="field">
        <text class="label">用户名</text>
        <input
          class="input"
          v-model="username"
          placeholder="请输入用户名"
          confirm-type="next"
        />
      </view>
      <view class="field">
        <text class="label">密码</text>
        <input
          class="input"
          v-model="password"
          password
          placeholder="请输入密码"
          confirm-type="done"
          @confirm="onSubmit"
        />
      </view>

      <button
        class="submit"
        type="primary"
        :loading="loading"
        :disabled="loading"
        @click="onSubmit"
      >
        登录
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { login } from '../../api/user.js'
import { setAuth, isLoggedIn } from '../../utils/auth.js'

const username = ref('')
const password = ref('')
const loading = ref(false)

onShow(() => {
  if (isLoggedIn()) {
    uni.reLaunch({ url: '/pages/index/index' })
  }
})

async function onSubmit() {
  const u = username.value.trim()
  if (!u || !password.value) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  if (loading.value) return
  loading.value = true
  try {
    const payload = await login({ username: u, password: password.value })
    if (!payload || typeof payload !== 'object' || !payload.token) {
      uni.showToast({ title: '登录成功但未返回 token', icon: 'none' })
      return
    }
    setAuth(payload.token, {
      userId: payload.userId,
      username: payload.username,
      realName: payload.realName
    })
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index/index' })
    }, 400)
  } catch (e) {
    const msg = e?.message || '登录失败'
    uni.showToast({ title: msg, icon: 'none', duration: 2500 })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 48rpx 40rpx 80rpx;
  background: linear-gradient(180deg, #e8eef5 0%, #f4f6f9 40%, #f4f6f9 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.hero {
  width: 100%;
  max-width: 560px;
  margin-bottom: 48rpx;
  text-align: center;
}

.hero-title {
  display: block;
  font-size: 44rpx;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: 2rpx;
}

.hero-sub {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
  color: #666;
}

.card {
  width: 100%;
  max-width: 560px;
  background: #fff;
  border-radius: 24rpx;
  padding: 56rpx 48rpx 48rpx;
  box-shadow: 0 16rpx 48rpx rgba(15, 35, 95, 0.08);
  box-sizing: border-box;
}

.field {
  margin-bottom: 40rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.input {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  padding: 0 28rpx;
  font-size: 32rpx;
  border: 2rpx solid #e2e6ee;
  border-radius: 16rpx;
  background: #fafbfc;
  box-sizing: border-box;
}

.input:focus {
  border-color: #409eff;
  background: #fff;
}

.submit {
  margin-top: 16rpx;
  height: 96rpx;
  line-height: 96rpx;
  font-size: 34rpx;
  border-radius: 16rpx;
}
</style>
