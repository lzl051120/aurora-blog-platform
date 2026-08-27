<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type UploadRequestOptions } from 'element-plus'
import { Collection, ChatDotRound, Document, FolderOpened, House, Picture, SwitchButton, User } from '@element-plus/icons-vue'
import { api, apiMessage } from '../api/client'
import { useAuthStore } from '../stores/auth'
import type { Category, CommentItem, PostSummary, UserProfile } from '../types'

interface Dashboard { posts: number; published: number; users: number; pendingComments: number }
interface GuestMessage extends CommentItem { status: 'PENDING' | 'APPROVED' | 'REJECTED' }
interface MediaItem { id: number; originalName: string; publicUrl: string; sizeBytes: number; createdAt: string }
const auth = useAuthStore(); const router = useRouter(); const active = ref('overview'); const loading = ref(true)
const dashboard = ref<Dashboard>({ posts: 0, published: 0, users: 0, pendingComments: 0 })
const posts = ref<PostSummary[]>([]); const categories = ref<Category[]>([]); const comments = ref<CommentItem[]>([])
const guestbook = ref<GuestMessage[]>([]); const users = ref<UserProfile[]>([]); const media = ref<MediaItem[]>([])
const categoryForm = ref({ id: 0, name: '', slug: '', description: '', sortOrder: 0 })
const title = computed(() => ({ overview: '工作台', posts: '文章管理', categories: '分类管理', comments: '评论审核', guestbook: '留言审核', users: '用户管理', media: '媒体库' }[active.value]))
async function loadAll() {
  loading.value = true
  try {
    const [d, p, c, cm, g, u, m] = await Promise.all([
      api.get<Dashboard>('/admin/dashboard'), api.get<PostSummary[]>('/admin/posts'), api.get<Category[]>('/categories'),
      api.get<CommentItem[]>('/admin/comments'), api.get<GuestMessage[]>('/admin/guestbook'), api.get<UserProfile[]>('/admin/users'), api.get<MediaItem[]>('/admin/media'),
    ])
    dashboard.value=d.data;posts.value=p.data;categories.value=c.data;comments.value=cm.data;guestbook.value=g.data;users.value=u.data;media.value=m.data
  } catch (e) { ElMessage.error(apiMessage(e, '后台数据加载失败')) } finally { loading.value = false }
}
async function removePost(post: PostSummary) {
  await ElMessageBox.confirm(`确定删除《${post.title}》？评论也会一并删除。`, '删除文章', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  try { await api.delete(`/admin/posts/${post.id}`); ElMessage.success('文章已删除'); await loadAll() } catch (e) { ElMessage.error(apiMessage(e)) }
}
function editCategory(item?: Category) { categoryForm.value = item ? { ...item } : { id: 0, name: '', slug: '', description: '', sortOrder: 0 } }
async function saveCategory() {
  try { const f=categoryForm.value; if (f.id) await api.put(`/admin/categories/${f.id}`, f); else await api.post('/admin/categories', f); ElMessage.success('分类已保存'); editCategory(); await loadAll() } catch (e) { ElMessage.error(apiMessage(e)) }
}
async function removeCategory(item: Category) {
  await ElMessageBox.confirm(`删除分类“${item.name}”？文章将变为未分类。`, '删除分类', { type:'warning' })
  try { await api.delete(`/admin/categories/${item.id}`); await loadAll(); ElMessage.success('分类已删除') } catch (e) { ElMessage.error(apiMessage(e)) }
}
async function moderate(kind: 'comments' | 'guestbook', id: number, status: string) {
  try { await api.patch(`/admin/${kind}/${id}`, { status }); ElMessage.success('审核状态已更新'); await loadAll() } catch (e) { ElMessage.error(apiMessage(e)) }
}
async function removeItem(kind: 'comments' | 'guestbook', id: number) {
  await ElMessageBox.confirm('删除后不可恢复，确定继续？', '删除内容', { type:'warning' })
  try { await api.delete(`/admin/${kind}/${id}`); await loadAll(); ElMessage.success('内容已删除') } catch (e) { ElMessage.error(apiMessage(e)) }
}
async function toggleUser(user: UserProfile) {
  try { await api.patch(`/admin/users/${user.id}`, { status: user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE' }); await loadAll(); ElMessage.success('用户状态已更新') } catch (e) { ElMessage.error(apiMessage(e)) }
}
async function upload(options: UploadRequestOptions) {
  const form = new FormData(); form.append('file', options.file)
  try { await api.post('/admin/media', form); options.onSuccess({}); await loadAll(); ElMessage.success('图片已上传') } catch (e) { ElMessage.error(apiMessage(e)) }
}
async function copyUrl(url: string) { await window.navigator.clipboard.writeText(url); ElMessage.success('地址已复制') }
async function logout() { await auth.logout(); router.replace('/') }
function formatDate(value?: string) { return value ? new Intl.DateTimeFormat('zh-CN', { month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' }).format(new Date(value)) : '—' }
onMounted(loadAll)
</script>

<template>
  <div class="admin-app">
    <aside class="admin-sidebar">
      <RouterLink class="admin-brand" to="/"><span class="brand-mark">A</span><span><strong>AURORA</strong><small>内容工作室</small></span></RouterLink>
      <nav aria-label="后台导航">
        <button :class="{ active:active==='overview' }" @click="active='overview'"><House />工作台</button>
        <button :class="{ active:active==='posts' }" @click="active='posts'"><Document />文章</button>
        <button :class="{ active:active==='categories' }" @click="active='categories'"><FolderOpened />分类</button>
        <button :class="{ active:active==='comments' }" @click="active='comments'"><ChatDotRound />评论<span v-if="dashboard.pendingComments" class="nav-count">{{ dashboard.pendingComments }}</span></button>
        <button :class="{ active:active==='guestbook' }" @click="active='guestbook'"><Collection />留言</button>
        <button :class="{ active:active==='users' }" @click="active='users'"><User />用户</button>
        <button :class="{ active:active==='media' }" @click="active='media'"><Picture />媒体</button>
      </nav>
      <div class="admin-user"><span class="avatar">{{ auth.user?.displayName?.slice(0,1) }}</span><div><strong>{{ auth.user?.displayName }}</strong><small>管理员</small></div><button aria-label="退出登录" @click="logout"><SwitchButton /></button></div>
    </aside>
    <main class="admin-main">
      <header class="admin-topbar"><div><p>内容运营中心</p><h1>{{ title }}</h1></div><RouterLink class="button primary compact" to="/admin/posts/new">＋ 新建文章</RouterLink></header>
      <div v-if="loading" class="admin-loading">正在同步内容数据…</div>
      <template v-else>
        <section v-if="active==='overview'" class="dashboard-view">
          <div class="metric-row"><article><span>全部文章</span><strong>{{ dashboard.posts }}</strong><small>内容总量</small></article><article><span>已发布</span><strong>{{ dashboard.published }}</strong><small>公开可见</small></article><article><span>注册用户</span><strong>{{ dashboard.users }}</strong><small>社区成员</small></article><article class="accent"><span>待审评论</span><strong>{{ dashboard.pendingComments }}</strong><small>等待处理</small></article></div>
          <div class="dashboard-columns"><section><header><h2>最近文章</h2><button class="text-button" @click="active='posts'">查看全部</button></header><div class="recent-list"><article v-for="post in posts.slice(0,5)" :key="post.id"><span class="status-dot" :class="post.status?.toLowerCase()"></span><div><strong>{{ post.title }}</strong><small>{{ post.categoryName || '未分类' }} · {{ formatDate(post.updatedAt) }}</small></div><button @click="router.push(`/admin/posts/${post.id}`)">编辑</button></article></div></section><aside class="editor-note"><p class="eyebrow">Today's focus</p><h2>让每篇文章<br>都值得被读完。</h2><p>标题负责邀请，摘要负责承诺，正文负责兑现。</p><RouterLink to="/admin/posts/new">开始写作 →</RouterLink></aside></div>
        </section>

        <section v-else-if="active==='posts'" class="admin-table-section"><header><div><h2>文章列表</h2><p>管理草稿与已发布内容。</p></div></header><div class="data-table"><div class="table-row table-head"><span>文章</span><span>状态</span><span>浏览</span><span>更新时间</span><span>操作</span></div><div v-for="post in posts" :key="post.id" class="table-row"><span><strong>{{ post.title }}</strong><small>{{ post.categoryName || '未分类' }}</small></span><span><i class="status-label" :class="post.status?.toLowerCase()">{{ post.status==='PUBLISHED'?'已发布':'草稿' }}</i></span><span>{{ post.viewCount }}</span><span>{{ formatDate(post.updatedAt) }}</span><span class="row-actions"><button @click="router.push(`/admin/posts/${post.id}`)">编辑</button><button class="danger" @click="removePost(post)">删除</button></span></div></div></section>

        <section v-else-if="active==='categories'" class="split-admin-view"><div class="admin-table-section"><h2>现有分类</h2><div class="simple-list"><article v-for="item in categories" :key="item.id"><div><strong>{{ item.name }}</strong><small>/{{ item.slug }} · {{ item.postCount }} 篇</small></div><p>{{ item.description }}</p><span><button @click="editCategory(item)">编辑</button><button class="danger" @click="removeCategory(item)">删除</button></span></article></div></div><aside class="side-form"><h2>{{ categoryForm.id?'编辑分类':'新建分类' }}</h2><form @submit.prevent="saveCategory"><label>名称<input v-model="categoryForm.name" required maxlength="48"></label><label>英文标识<input v-model="categoryForm.slug" required pattern="[a-z0-9-]+"></label><label>说明<textarea v-model="categoryForm.description" maxlength="200"></textarea></label><label>排序<input v-model.number="categoryForm.sortOrder" type="number"></label><button class="button primary wide">保存分类</button></form></aside></section>

        <section v-else-if="active==='comments' || active==='guestbook'" class="admin-table-section"><header><div><h2>{{ active==='comments'?'评论审核':'留言审核' }}</h2><p>公开前确认内容友善且有价值。</p></div></header><div class="moderation-list"><article v-for="item in active==='comments'?comments:guestbook" :key="item.id"><div class="moderation-meta"><strong>{{ item.authorName }}</strong><span>{{ 'postTitle' in item && item.postTitle ? `评论《${item.postTitle}》` : '留言簿' }}</span><time>{{ formatDate(item.createdAt) }}</time><i class="status-label" :class="item.status?.toLowerCase()">{{ item.status }}</i></div><p>{{ item.content }}</p><footer><button @click="moderate(active, item.id, 'APPROVED')">通过</button><button @click="moderate(active, item.id, 'REJECTED')">拒绝</button><button class="danger" @click="removeItem(active, item.id)">删除</button></footer></article><p v-if="!(active==='comments'?comments:guestbook).length" class="empty-inline">暂时没有需要处理的内容。</p></div></section>

        <section v-else-if="active==='users'" class="admin-table-section"><header><div><h2>注册用户</h2><p>管理员账号不可在此停用。</p></div></header><div class="data-table users"><div class="table-row table-head"><span>用户</span><span>角色</span><span>状态</span><span>加入时间</span><span>操作</span></div><div v-for="user in users" :key="user.id" class="table-row"><span><strong>{{ user.displayName }}</strong><small>@{{ user.username }} · {{ user.email }}</small></span><span>{{ user.role }}</span><span><i class="status-label" :class="user.status.toLowerCase()">{{ user.status }}</i></span><span>{{ formatDate(user.createdAt) }}</span><span><button v-if="user.role!=='ADMIN'" @click="toggleUser(user)">{{ user.status==='ACTIVE'?'停用':'启用' }}</button><small v-else>受保护</small></span></div></div></section>

        <section v-else-if="active==='media'" class="media-view"><header><div><h2>媒体库</h2><p>仅支持 5MB 内的 JPEG、PNG 和 WebP。</p></div><el-upload :show-file-list="false" accept="image/jpeg,image/png,image/webp" :http-request="upload"><button class="button primary compact">上传图片</button></el-upload></header><div class="media-grid"><article v-for="item in media" :key="item.id"><img :src="item.publicUrl" :alt="item.originalName" loading="lazy"><div><strong>{{ item.originalName }}</strong><small>{{ Math.round(item.sizeBytes/1024) }} KB</small><button @click="copyUrl(item.publicUrl)">复制地址</button></div></article><p v-if="!media.length" class="empty-inline">媒体库还是空的。</p></div></section>
      </template>
    </main>
  </div>
</template>
