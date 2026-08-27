<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { api, apiMessage } from '../api/client'
import PostCard from '../components/PostCard.vue'
import StatePanel from '../components/StatePanel.vue'
import type { Category, PagedPosts } from '../types'

const route = useRoute(); const router = useRouter()
const categories = ref<Category[]>([]); const result = ref<PagedPosts | null>(null)
const search = ref(String(route.query.search || '')); const category = ref(String(route.query.category || ''))
const sort = ref(String(route.query.sort || 'latest')); const page = ref(Number(route.query.page || 1))
const loading = ref(false); const error = ref('')
async function load() {
  loading.value = true; error.value = ''
  try {
    const [posts, cats] = await Promise.all([
      api.get<PagedPosts>('/posts', { params: { search: search.value || undefined, category: category.value || undefined, sort: sort.value, page: page.value, size: 9 } }),
      api.get<Category[]>('/categories'),
    ]); result.value = posts.data; categories.value = cats.data
  } catch (e) { error.value = apiMessage(e, '文章列表加载失败') } finally { loading.value = false }
}
function apply() {
  page.value = 1
  router.replace({ query: { search: search.value || undefined, category: category.value || undefined, sort: sort.value, page: '1' } })
}
watch(() => route.query, () => {
  search.value = String(route.query.search || ''); category.value = String(route.query.category || '')
  sort.value = String(route.query.sort || 'latest'); page.value = Number(route.query.page || 1); load()
})
onMounted(load)
</script>

<template>
  <div class="public-page browse-page">
    <header class="page-intro"><p class="eyebrow">Archive / 索引</p><h1>在文字之间，<br>找到你的线索。</h1><p>浏览全部文章，或者沿着一个主题慢慢阅读。</p></header>
    <section class="filter-bar" aria-label="文章筛选">
      <label class="search-field"><Search /><span class="sr-only">搜索文章</span><input v-model="search" type="search" placeholder="搜索标题或摘要" @keyup.enter="apply"></label>
      <label><span class="sr-only">选择分类</span><select v-model="category" @change="apply"><option value="">全部分类</option><option v-for="item in categories" :key="item.id" :value="item.slug">{{ item.name }}</option></select></label>
      <div class="sort-switch" role="group" aria-label="排序方式"><button :class="{ active: sort === 'latest' }" @click="sort='latest';apply()">最新</button><button :class="{ active: sort === 'hot' }" @click="sort='hot';apply()">热门</button></div>
      <button class="button primary compact" type="button" @click="apply">筛选</button>
    </section>
    <div v-if="loading" class="skeleton-grid" role="status" aria-label="正在加载文章"><div v-for="n in 6" :key="n" class="skeleton-card"></div></div>
    <StatePanel v-else-if="error" title="没有成功取回文章" :description="error" action="重试" @action="load" />
    <StatePanel v-else-if="!result?.items.length" title="暂时没有匹配的文章" description="换一个关键词或分类试试，灵感可能就在下一页。" action="清除筛选" @action="search='';category='';apply()" />
    <template v-else-if="result">
      <p class="result-count">共 {{ result.total }} 篇文章</p>
      <div class="archive-grid"><PostCard v-for="post in result.items" :key="post.id" :post="post" /></div>
      <nav v-if="result.pages > 1" class="pagination" aria-label="分页"><button :disabled="page <= 1" @click="router.replace({ query: { ...route.query, page: String(page - 1) } })">上一页</button><span>{{ page }} / {{ result.pages }}</span><button :disabled="page >= result.pages" @click="router.replace({ query: { ...route.query, page: String(page + 1) } })">下一页</button></nav>
    </template>
  </div>
</template>
