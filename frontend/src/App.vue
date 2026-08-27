<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { Moon, Sunny, User } from '@element-plus/icons-vue'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const auth = useAuthStore()
const theme = ref<'light' | 'dark'>((localStorage.getItem('aurora-theme') as 'light' | 'dark') || 'light')
const isAdmin = computed(() => route.path.startsWith('/admin'))
const year = new Date().getFullYear()

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
  document.documentElement.dataset.theme = theme.value
  localStorage.setItem('aurora-theme', theme.value)
}

onMounted(() => {
  document.documentElement.dataset.theme = theme.value
  auth.bootstrap()
})
</script>

<template>
  <a class="skip-link" href="#main-content">跳到主要内容</a>
  <div class="app-shell" :class="{ 'admin-shell': isAdmin }">
    <header v-if="!isAdmin" class="site-header">
      <RouterLink class="brand" to="/" aria-label="Aurora 微光编辑部首页">
        <span class="brand-mark">A</span>
        <span><strong>AURORA</strong><small>微光编辑部</small></span>
      </RouterLink>
      <nav class="primary-nav" aria-label="主导航">
        <RouterLink to="/posts">文章</RouterLink>
        <RouterLink to="/guestbook">留言</RouterLink>
        <RouterLink v-if="auth.isAdmin" to="/admin">后台</RouterLink>
      </nav>
      <div class="header-actions">
        <button class="icon-button" type="button" :aria-label="theme === 'light' ? '切换深色主题' : '切换浅色主题'" @click="toggleTheme">
          <Moon v-if="theme === 'light'" />
          <Sunny v-else />
        </button>
        <RouterLink class="account-link" :to="auth.authenticated ? '/profile' : '/auth'" :aria-label="auth.authenticated ? `进入${auth.user?.displayName || '我的'}个人中心` : '登录或注册'">
          <User /><span>{{ auth.authenticated ? auth.user?.displayName : '登录' }}</span>
        </RouterLink>
      </div>
    </header>
    <main id="main-content" tabindex="-1"><RouterView /></main>
    <footer v-if="!isAdmin" class="site-footer">
      <div><span class="footer-wordmark">AURORA</span><p>记录工程、设计与缓慢生长的时刻。</p></div>
      <p>© {{ year }} 微光编辑部 · 用 Java 与 Vue 构建</p>
    </footer>
  </div>
</template>
