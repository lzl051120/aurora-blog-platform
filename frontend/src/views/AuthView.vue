<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apiMessage } from '../api/client'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore(); const router = useRouter(); const route = useRoute()
const mode = ref<'login' | 'register'>('login'); const busy = ref(false); const error = ref('')
const login = reactive({ login: '', password: '' })
const register = reactive({ username: '', email: '', displayName: '', password: '' })
async function submitLogin() {
  busy.value = true; error.value = ''
  try { await auth.login(login); ElMessage.success('欢迎回来'); router.replace(String(route.query.redirect || (auth.isAdmin ? '/admin' : '/profile'))) }
  catch (e) { error.value = apiMessage(e, '登录失败') } finally { busy.value = false }
}
async function submitRegister() {
  busy.value = true; error.value = ''
  try { await auth.register(register); ElMessage.success('账号创建成功'); router.replace(String(route.query.redirect || '/profile')) }
  catch (e) { error.value = apiMessage(e, '注册失败') } finally { busy.value = false }
}
</script>
<template>
  <div class="auth-page">
    <aside class="auth-story"><RouterLink class="brand light" to="/"><span class="brand-mark">A</span><span><strong>AURORA</strong><small>微光编辑部</small></span></RouterLink><blockquote>“愿每一次登录，<br>都是回到自己的坐标。”</blockquote><div class="auth-constellation" aria-hidden="true"><i></i><i></i><i></i><i></i></div><p>阅读 · 交流 · 继续生长</p></aside>
    <main class="auth-panel">
      <div class="auth-box"><p class="eyebrow">Member access</p><h1>{{ mode === 'login' ? '欢迎回来' : '成为同行者' }}</h1><p class="auth-caption">{{ mode === 'login' ? '登录后参与评论、留言与个人资料管理。' : '用一个简单账号，加入这场安静的交流。' }}</p>
        <div class="auth-tabs" role="tablist"><button :class="{ active: mode === 'login' }" role="tab" @click="mode='login';error=''">登录</button><button :class="{ active: mode === 'register' }" role="tab" @click="mode='register';error=''">注册</button></div>
        <form v-if="mode === 'login'" @submit.prevent="submitLogin"><label>用户名或邮箱<input v-model="login.login" autocomplete="username" required placeholder="例如 aurora_reader"></label><label>密码<input v-model="login.password" type="password" autocomplete="current-password" required minlength="8" placeholder="至少 8 位"></label><p v-if="error" class="form-error" role="alert">{{ error }}</p><button class="button primary wide" :disabled="busy">{{ busy ? '正在验证…' : '进入 Aurora' }}</button></form>
        <form v-else @submit.prevent="submitRegister"><div class="two-fields"><label>用户名<input v-model="register.username" autocomplete="username" required minlength="3" maxlength="32" pattern="[a-zA-Z0-9_]+" placeholder="字母、数字或下划线"></label><label>显示昵称<input v-model="register.displayName" required maxlength="48" placeholder="大家如何称呼你"></label></div><label>邮箱<input v-model="register.email" type="email" autocomplete="email" required placeholder="name@example.com"></label><label>密码<input v-model="register.password" type="password" autocomplete="new-password" required minlength="8" maxlength="72" placeholder="8-72 位"></label><p v-if="error" class="form-error" role="alert">{{ error }}</p><button class="button primary wide" :disabled="busy">{{ busy ? '正在创建…' : '创建账号' }}</button></form>
        <RouterLink class="back-home" to="/">← 返回首页</RouterLink>
      </div>
    </main>
  </div>
</template>
