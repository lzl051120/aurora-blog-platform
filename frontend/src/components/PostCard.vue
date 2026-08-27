<script setup lang="ts">
import { ArrowRight, View } from '@element-plus/icons-vue'
import type { PostSummary } from '../types'
defineProps<{ post: PostSummary; featured?: boolean }>()
function date(value?: string) {
  return value ? new Intl.DateTimeFormat('zh-CN', { month: 'short', day: 'numeric', year: 'numeric' }).format(new Date(value)) : '尚未发布'
}
</script>
<template>
  <article class="post-card" :class="{ featured }">
    <RouterLink class="post-cover" :to="`/posts/${post.slug}`" :aria-label="`阅读《${post.title}》`">
      <img v-if="post.coverUrl" :src="post.coverUrl" :alt="`${post.title}封面`" loading="lazy" />
      <span v-else class="cover-fallback"><i>AU</i><b>{{ post.categoryName || '手记' }}</b></span>
    </RouterLink>
    <div class="post-copy">
      <div class="post-meta"><span>{{ post.categoryName || '未分类' }}</span><time>{{ date(post.publishedAt) }}</time></div>
      <h3><RouterLink :to="`/posts/${post.slug}`">{{ post.title }}</RouterLink></h3>
      <p>{{ post.summary }}</p>
      <div class="post-footer"><span><View /> {{ post.viewCount }}</span><RouterLink :to="`/posts/${post.slug}`">继续阅读 <ArrowRight /></RouterLink></div>
    </div>
  </article>
</template>
