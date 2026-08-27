import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'

const router = createRouter({
  history: createWebHistory(), scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/', component: () => import('./views/HomeView.vue'), meta: { title: 'Aurora 微光编辑部' } },
    { path: '/posts', component: () => import('./views/PostsView.vue'), meta: { title: '全部文章' } },
    { path: '/posts/:slug', component: () => import('./views/PostView.vue'), meta: { title: '阅读文章' } },
    { path: '/guestbook', component: () => import('./views/GuestbookView.vue'), meta: { title: '留言簿' } },
    { path: '/auth', component: () => import('./views/AuthView.vue'), meta: { title: '登录与注册' } },
    { path: '/profile', component: () => import('./views/ProfileView.vue'), meta: { title: '个人中心', auth: true } },
    { path: '/admin', component: () => import('./views/AdminView.vue'), meta: { title: '内容后台', admin: true } },
    { path: '/admin/posts/new', component: () => import('./views/PostEditorView.vue'), meta: { title: '新建文章', admin: true } },
    { path: '/admin/posts/:id', component: () => import('./views/PostEditorView.vue'), meta: { title: '编辑文章', admin: true } },
    { path: '/:pathMatch(.*)*', component: () => import('./views/NotFoundView.vue'), meta: { title: '页面未找到' } },
  ],
})
router.beforeEach(async (to) => {
  const auth = useAuthStore(); await auth.bootstrap()
  document.title = `${String(to.meta.title || 'Aurora')} · Aurora`
  if (to.meta.admin && !auth.isAdmin) return { path: '/auth', query: { redirect: to.fullPath } }
  if (to.meta.auth && !auth.authenticated) return { path: '/auth', query: { redirect: to.fullPath } }
  if (to.path === '/auth' && auth.authenticated) return auth.isAdmin ? '/admin' : '/profile'
  return true
})
export default router
