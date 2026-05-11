<template>
  <view class="page">
    <view class="panel">
      <text class="welcome">你好，{{ displayName }}</text>
      <text class="hint">点检仪已就绪，后续可在此进入点检流程。</text>
      <button class="logout" type="warn" plain @click="onLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getUserProfile, clearAuth, isLoggedIn } from '../../utils/auth.js'
import { logout as logoutApi } from '../../api/user.js'

const profile = ref(null)

const displayName = computed(() => {
  const p = profile.value
  if (!p) return '用户'
  if (p.realName) return p.realName
  if (p.username) return p.username
  return '用户'
})

function refreshProfile() {
  profile.value = getUserProfile()
}

onShow(() => {
  if (!isLoggedIn()) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  refreshProfile()
})

async function onLogout() {
  try {
    await logoutApi()
  } catch {
    // 后端无 token 吊销时仍清理本地
  }
  clearAuth()
  uni.reLaunch({ url: '/pages/login/login' })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 48rpx 40rpx;
  background: #f4f6f9;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.panel {
  width: 100%;
  max-width: 560px;
  margin: 0 auto;
  background: #fff;
  border-radius: 24rpx;
  padding: 56rpx 48rpx;
  box-shadow: 0 16rpx 48rpx rgba(15, 35, 95, 0.06);
  box-sizing: border-box;
}

.welcome {
  display: block;
  font-size: 40rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.hint {
  display: block;
  margin-top: 24rpx;
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.logout {
  margin-top: 56rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
  border-radius: 16rpx;
}
</style>
