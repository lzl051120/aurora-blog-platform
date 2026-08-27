<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatLineRound, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api, apiMessage } from '../api/client'
import { useAuthStore } from '../stores/auth'
import StatePanel from '../components/StatePanel.vue'
import type { PostDetail } from '../types'

const route = useRoute(); const router = useRouter(); const auth = useAuthStore()
const post = ref<PostDetail | null>(null); const loading = ref(true); const error = ref('')
const comment = ref(''); const submitting = ref(false)
const readingMinutes = computed(() => Math.max(1, Math.ceil((post.value?.contentHtml.replace(/<[^>]+>/g, '').length || 0) / 450)))
function date(value?: string) { return value ? new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }).format(new Date(value)) : '' }
async function load() {
  loading.value = true; error.value = ''
  try { post.value = (await api.get<PostDetail>(`/posts/${route.params.slug}`)).data; document.title = `${post.value.title} · Aurora` }
  catch (e) { error.value = apiMessage(e, '文章暂时无法打开') } finally { loading.value = false }
}
async function submitComment() {
  if (!auth.authenticated) return router.push({ path: '/auth', query: { redirect: route.fullPath } })
  submitting.value = true
  try { const { data } = await api.post<{ message: string }>(`/posts/${post.value?.id}/comments`, { content: comment.value }); comment.value = ''; ElMessage.success(data.message) }
  catch (e) { ElMessage.error(apiMessage(e)) } finally { submitting.value = false }
}
watch(() => route.params.slug, load); onMounted(load)
</script>

<template>
  <div class="public-page article-page">
    <div v-if="loading" class="article-skeleton"><div></div><div></div><div></div></div>
    <StatePanel v-else-if="error" title="这篇文章没有抵达" :description="error" action="返回文章列表" @action="router.push('/posts')" />
    <article v-else-if="post" class="article-shell">
      <header class="article-header">
        <RouterLink class="category-label" :to="`/posts?category=${post.categorySlug || ''}`">{{ post.categoryName || '手记' }}</RouterLink>
        <h1>{{ post.title }}</h1><p class="article-summary">{{ post.summary }}</p>
        <div class="article-byline"><span>撰文 · {{ post.authorName }}</span><time>{{ date(post.publishedAt) }}</time><span>{{ readingMinutes }} 分钟阅读</span><span><View /> {{ post.viewCount }}</span></div>
      </header>
      <div class="article-cover"><img v-if="post.coverUrl" :src="post.coverUrl" :alt="`${post.title}封面`"><div v-else class="cover-fallback"><i>AURORA</i><b>{{ post.categoryName }}</b></div></div>
      <div class="article-content" v-html="post.contentHtml"></div>
      <footer class="article-end"><span>FIN.</span><p>感谢你把时间留在这里。</p></footer>
    </article>
    <section v-if="post" class="comment-section" aria-labelledby="comments-title">
      <header><div><p class="eyebrow">Conversation</p><h2 id="comments-title">评论 · {{ post.comments.length }}</h2></div><ChatLineRound /></header>
      <form class="comment-form" @submit.prevent="submitComment"><label for="comment">留下你的想法</label><textarea id="comment" v-model="comment" maxlength="1000" required placeholder="认真交流，彼此照亮。"></textarea><div><small>{{ comment.length }} / 1000</small><button class="button primary compact" :disabled="submitting" type="submit">{{ submitting ? '提交中…' : auth.authenticated ? '提交审核' : '登录后评论' }}</button></div></form>
      <div v-if="post.comments.length" class="comment-list"><article v-for="item in post.comments" :key="item.id"><div class="avatar">{{ item.authorName.slice(0, 1) }}</div><div><header><strong>{{ item.authorName }}</strong><time>{{ date(item.createdAt) }}</time></header><p>{{ item.content }}</p></div></article></div>
      <p v-else class="empty-inline">还没有公开评论，成为第一个认真回应的人。</p>
    </section>
  </div>
</template>
