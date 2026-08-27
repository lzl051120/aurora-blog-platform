<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { api, apiMessage } from '../api/client'
import { useAuthStore } from '../stores/auth'

interface Message { id: number; content: string; authorName: string; authorAvatar?: string; createdAt: string }
const router = useRouter(); const auth = useAuthStore(); const messages = ref<Message[]>([])
const content = ref(''); const loading = ref(true); const submitting = ref(false); const error = ref('')
function date(value: string) { return new Intl.DateTimeFormat('zh-CN', { month: 'short', day: 'numeric', year: 'numeric' }).format(new Date(value)) }
async function load() { loading.value = true; try { messages.value = (await api.get<Message[]>('/guestbook')).data } catch (e) { error.value = apiMessage(e) } finally { loading.value = false } }
async function submit() {
  if (!auth.authenticated) return router.push({ path: '/auth', query: { redirect: '/guestbook' } })
  submitting.value = true
  try { const { data } = await api.post<{ message: string }>('/guestbook', { content: content.value }); content.value = ''; ElMessage.success(data.message) }
  catch (e) { ElMessage.error(apiMessage(e)) } finally { submitting.value = false }
}
onMounted(load)
</script>
<template>
  <div class="public-page guestbook-page">
    <header class="guestbook-hero"><p class="eyebrow">Leave a trace</p><h1>来过的人，<br><em>都留下一束光。</em></h1><p>分享一个念头、一个问题，或者只是打声招呼。留言审核后会出现在这里。</p></header>
    <section class="guestbook-compose"><form @submit.prevent="submit"><label for="guest-message">写下想说的话</label><textarea id="guest-message" v-model="content" maxlength="1000" required placeholder="此刻，你想留下些什么？"></textarea><footer><span>{{ content.length }} / 1000</span><button class="button primary" :disabled="submitting">{{ submitting ? '正在送出…' : auth.authenticated ? '送出留言' : '登录后留言' }}</button></footer></form></section>
    <p v-if="loading" class="loading-line">正在翻开留言簿…</p><p v-else-if="error" class="error-line">{{ error }}</p>
    <section v-else class="guestbook-grid" aria-label="公开留言">
      <article v-for="(item, index) in messages" :key="item.id" :style="{ '--tilt': `${(index % 3 - 1) * 0.8}deg` }"><span class="quote-mark">“</span><p>{{ item.content }}</p><footer><span class="avatar">{{ item.authorName.slice(0, 1) }}</span><div><strong>{{ item.authorName }}</strong><time>{{ date(item.createdAt) }}</time></div></footer></article>
      <p v-if="!messages.length" class="empty-inline">第一页还是空白，等你写下第一句话。</p>
    </section>
  </div>
</template>
