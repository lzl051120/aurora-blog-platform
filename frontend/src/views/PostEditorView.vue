<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { EditorContent, useEditor } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import { ElMessage, ElMessageBox, type UploadRequestOptions } from 'element-plus'
import { ArrowLeft, EditPen, List, Picture, Reading, RefreshLeft } from '@element-plus/icons-vue'
import { api, apiMessage } from '../api/client'
import type { Category } from '../types'

const route=useRoute();const router=useRouter();const id=computed(()=>route.params.id?Number(route.params.id):null)
const categories=ref<Category[]>([]);const loading=ref(Boolean(id.value));const saving=ref(false);const dirty=ref(false);const preview=ref(false)
const form=reactive({ title:'',slug:'',summary:'',categoryId:undefined as number|undefined,coverUrl:'',status:'DRAFT' as 'DRAFT'|'PUBLISHED',contentHtml:'<p></p>' })
const editor=useEditor({ content:form.contentHtml, extensions:[StarterKit,Placeholder.configure({placeholder:'从一个清楚的念头开始…'})], onUpdate:({editor})=>{form.contentHtml=editor.getHTML();dirty.value=true} })
const wordCount=computed(()=>editor.value?.getText().replace(/\s/g,'').length||0);const draftKey=computed(()=>`aurora-draft-${id.value||'new'}`)
async function load(){loading.value=true;try{categories.value=(await api.get<Category[]>('/categories')).data;if(id.value){const {data}=await api.get(`/admin/posts/${id.value}`);Object.assign(form,data);editor.value?.commands.setContent(data.contentHtml||'<p></p>');dirty.value=false}else{const local=localStorage.getItem(draftKey.value);if(local&&await ElMessageBox.confirm('发现未提交的本地草稿，是否恢复？','恢复草稿',{confirmButtonText:'恢复',cancelButtonText:'忽略'}).catch(()=>false)){Object.assign(form,JSON.parse(local));editor.value?.commands.setContent(form.contentHtml)}}}catch(e){ElMessage.error(apiMessage(e,'编辑器加载失败'))}finally{loading.value=false}}
async function save(status=form.status){if(!form.title.trim()||!form.summary.trim()||wordCount.value<10)return ElMessage.warning('请补充标题、摘要和至少10个字的正文');saving.value=true;try{const payload={...form,status,categoryId:form.categoryId||null};if(id.value)await api.put(`/admin/posts/${id.value}`,payload);else await api.post('/admin/posts',payload);form.status=status;dirty.value=false;localStorage.removeItem(draftKey.value);ElMessage.success(status==='PUBLISHED'?'文章已发布':'草稿已保存');router.replace('/admin')}catch(e){ElMessage.error(apiMessage(e))}finally{saving.value=false}}
async function upload(options:UploadRequestOptions){const data=new FormData();data.append('file',options.file);try{const res=await api.post<{url:string}>('/admin/media',data);form.coverUrl=res.data.url;dirty.value=true;options.onSuccess(res.data);ElMessage.success('封面已上传')}catch(e){ElMessage.error(apiMessage(e))}}
function autosave(){localStorage.setItem(draftKey.value,JSON.stringify(form))}
function beforeUnload(event:BeforeUnloadEvent){if(dirty.value){event.preventDefault();event.returnValue=''}}
const autosaveTimer=window.setInterval(()=>{if(dirty.value)autosave()},15000)
watch(()=>[form.title,form.slug,form.summary,form.categoryId,form.coverUrl],()=>dirty.value=true,{deep:true})
onMounted(()=>{window.addEventListener('beforeunload',beforeUnload);load()});onBeforeUnmount(()=>{clearInterval(autosaveTimer);window.removeEventListener('beforeunload',beforeUnload);editor.value?.destroy()})
</script>
<template>
  <div class="editor-page"><header class="editor-topbar"><button class="icon-button" aria-label="返回后台" @click="router.push('/admin')"><ArrowLeft /></button><div><strong>{{ id?'编辑文章':'新建文章' }}</strong><small>{{ dirty?'有未保存修改':'已保存' }} · {{ wordCount }} 字</small></div><div class="editor-actions"><button class="button secondary compact" @click="preview=!preview"><Reading />{{ preview?'继续编辑':'预览' }}</button><button class="button secondary compact" :disabled="saving" @click="save('DRAFT')">保存草稿</button><button class="button primary compact" :disabled="saving" @click="save('PUBLISHED')">发布文章</button></div></header>
      <main v-loading="loading" class="editor-workspace"><section v-if="!preview" class="editor-main"><div class="title-fields"><input v-model="form.title" class="title-input" maxlength="160" placeholder="文章标题"><input v-model="form.summary" class="summary-input" maxlength="360" placeholder="用一两句话告诉读者，这篇文章值得读完的原因。"></div><div class="editor-toolbar" role="toolbar" aria-label="富文本格式"><button :class="{active:editor?.isActive('bold')}" aria-label="加粗" @click="editor?.chain().focus().toggleBold().run()"><strong>B</strong></button><button :class="{active:editor?.isActive('heading',{level:2})}" @click="editor?.chain().focus().toggleHeading({level:2}).run()">H2</button><button :class="{active:editor?.isActive('bulletList')}" aria-label="无序列表" @click="editor?.chain().focus().toggleBulletList().run()"><List /></button><button :class="{active:editor?.isActive('blockquote')}" aria-label="引用" @click="editor?.chain().focus().toggleBlockquote().run()">“ ”</button><button aria-label="撤销" @click="editor?.chain().focus().undo().run()"><RefreshLeft /></button></div><EditorContent :editor="editor" class="rich-editor" /></section>
      <article v-else class="editor-preview"><p class="eyebrow">Preview</p><h1>{{ form.title||'未命名文章' }}</h1><p class="article-summary">{{ form.summary }}</p><img v-if="form.coverUrl" :src="form.coverUrl" alt="文章封面预览"><div class="article-content" v-html="form.contentHtml"></div></article>
      <aside class="editor-sidebar"><section><h2><EditPen />发布设置</h2><label>状态<select v-model="form.status"><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option></select></label><label>分类<select v-model="form.categoryId"><option :value="undefined">未分类</option><option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option></select></label><label>英文链接<input v-model="form.slug" pattern="[a-z0-9-]*" placeholder="留空自动生成"></label></section><section><h2><Picture />文章封面</h2><div v-if="form.coverUrl" class="cover-preview"><img :src="form.coverUrl" alt="当前封面"><button @click="form.coverUrl=''">移除</button></div><el-upload v-else drag :show-file-list="false" accept="image/jpeg,image/png,image/webp" :http-request="upload"><el-icon><Picture /></el-icon><p>拖入或点击上传</p><small>JPEG / PNG / WebP · 5MB</small></el-upload></section><p class="autosave-note">编辑内容每 15 秒保存到本地，成功提交后自动清除。</p></aside>
    </main>
  </div>
</template>
