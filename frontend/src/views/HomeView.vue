<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ArrowRight, Compass, EditPen } from '@element-plus/icons-vue'
import { api, apiMessage } from '../api/client'
import PostCard from '../components/PostCard.vue'
import StatePanel from '../components/StatePanel.vue'
import type { Category, PostSummary } from '../types'

interface HomeData { featured: PostSummary[]; latest: PostSummary[]; categories: Category[]; stats: { posts: number; users: number } }
const data = ref<HomeData | null>(null)
const loading = ref(true)
const error = ref('')
async function load() {
  loading.value = true; error.value = ''
  try { data.value = (await api.get<HomeData>('/home')).data } catch (e) { error.value = apiMessage(e, '首页内容暂时无法抵达') }
  finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <div class="public-page home-page">
    <section class="hero-section">
      <div class="hero-orbit" aria-hidden="true"><span></span><span></span></div>
      <div class="hero-copy">
        <p class="eyebrow">A quiet place for vivid ideas · 2026</p>
        <h1>把复杂的世界，<br><em>写成清楚的光。</em></h1>
        <p class="hero-lead">关于工程、设计与成长的个人刊物。慢一点观察，认真地记录，也把每次实践变成下一次出发的坐标。</p>
        <div class="hero-actions">
          <RouterLink class="button primary" to="/posts">开始阅读 <ArrowRight /></RouterLink>
          <RouterLink class="text-link" to="/guestbook">去留言簿聊聊</RouterLink>
        </div>
      </div>
      <aside class="hero-note">
        <span>本期主题</span><strong>BUILD<br>WITH<br>CLARITY</strong><p>清楚，是一种温柔的工程能力。</p>
      </aside>
    </section>

    <div v-if="loading" class="skeleton-grid" role="status" aria-label="正在加载文章"><div v-for="n in 3" :key="n" class="skeleton-card"></div></div>
    <StatePanel v-else-if="error" title="微光暂时熄灭" :description="error" action="重新加载" @action="load" />
    <template v-else-if="data">
      <section class="section-block featured-section">
        <header class="section-heading"><div><p class="eyebrow">Editor's selection</p><h2>值得停留的篇章</h2></div><RouterLink to="/posts?sort=hot">查看热门 <ArrowRight /></RouterLink></header>
        <div class="featured-grid"><PostCard v-for="post in data.featured" :key="post.id" :post="post" featured /></div>
      </section>

      <section class="manifesto-band">
        <div><Compass /><span>探索</span><p>从真实问题出发，不追逐空洞的复杂。</p></div>
        <blockquote>“作品不是答案，<br>而是思考留下的路径。”</blockquote>
        <div><EditPen /><span>记录</span><p>让经验可以被看见、复用与继续。</p></div>
      </section>

      <section class="section-block latest-section">
        <header class="section-heading"><div><p class="eyebrow">Recently published</p><h2>最近更新</h2></div><span class="issue-number">NO. {{ String(data.stats.posts).padStart(2, '0') }}</span></header>
        <div class="latest-list"><PostCard v-for="post in data.latest" :key="post.id" :post="post" /></div>
      </section>

      <section class="category-ribbon" aria-labelledby="category-title">
        <div><p class="eyebrow">Find your thread</p><h2 id="category-title">沿着兴趣继续</h2></div>
        <nav aria-label="文章分类"><RouterLink v-for="category in data.categories" :key="category.id" :to="`/posts?category=${category.slug}`"><span>{{ category.name }}</span><small>{{ category.postCount }} 篇</small></RouterLink></nav>
      </section>
    </template>
  </div>
</template>
