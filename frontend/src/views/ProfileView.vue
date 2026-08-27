<script setup lang="ts">
import { reactive, ref, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api, apiMessage } from '../api/client'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore(); const router = useRouter(); const saving = ref(false)
const profile = reactive({ displayName: '', bio: '', avatarUrl: '' }); const password = reactive({ currentPassword: '', newPassword: '' })
watchEffect(() => { if (auth.user) { profile.displayName = auth.user.displayName; profile.bio = auth.user.bio || ''; profile.avatarUrl = auth.user.avatarUrl || '' } })
async function saveProfile() { saving.value = true; try { await api.put('/profile', profile); await auth.bootstrap(true); ElMessage.success('个人资料已更新') } catch (e) { ElMessage.error(apiMessage(e)) } finally { saving.value = false } }
async function changePassword() { saving.value = true; try { await api.put('/profile/password', password); password.currentPassword='';password.newPassword='';ElMessage.success('密码已更新') } catch (e) { ElMessage.error(apiMessage(e)) } finally { saving.value = false } }
async function logout() { await auth.logout(); router.replace('/') }
</script>
<template>
  <div class="public-page profile-page"><header class="page-intro compact"><p class="eyebrow">Your corner</p><h1>个人中心</h1><p>管理公开资料与账号安全。</p></header>
    <div class="profile-layout"><aside class="profile-card"><div class="large-avatar">{{ auth.user?.displayName?.slice(0, 1) }}</div><h2>{{ auth.user?.displayName }}</h2><p>@{{ auth.user?.username }}</p><span>{{ auth.user?.role === 'ADMIN' ? '编辑管理员' : '社区读者' }}</span><button class="text-button danger" @click="logout">退出登录</button></aside>
      <div class="profile-forms"><section><h2>公开资料</h2><form @submit.prevent="saveProfile"><label>显示昵称<input v-model="profile.displayName" required maxlength="48"></label><label>个人简介<textarea v-model="profile.bio" maxlength="240" placeholder="用一两句话介绍自己"></textarea></label><label>头像地址<input v-model="profile.avatarUrl" type="url" placeholder="https://…"></label><button class="button primary" :disabled="saving">保存资料</button></form></section>
      <section><h2>修改密码</h2><form @submit.prevent="changePassword"><label>当前密码<input v-model="password.currentPassword" type="password" autocomplete="current-password" required></label><label>新密码<input v-model="password.newPassword" type="password" autocomplete="new-password" required minlength="8"></label><button class="button secondary" :disabled="saving">更新密码</button></form></section></div>
    </div>
  </div>
</template>
