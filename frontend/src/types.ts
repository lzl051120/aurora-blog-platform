export interface UserProfile {
  id: number; username: string; email: string; displayName: string; bio?: string; avatarUrl?: string
  role: 'ADMIN' | 'USER'; status: 'ACTIVE' | 'DISABLED'; createdAt?: string
}
export interface Category {
  id: number; name: string; slug: string; description: string; sortOrder: number; postCount: number
}
export interface PostSummary {
  id: number; title: string; slug: string; summary: string; coverUrl?: string; categoryName?: string
  categorySlug?: string; authorName: string; viewCount: number; publishedAt?: string
  status?: 'DRAFT' | 'PUBLISHED'; updatedAt?: string
}
export interface CommentItem {
  id: number; content: string; status?: 'PENDING' | 'APPROVED' | 'REJECTED'; authorName: string
  authorAvatar?: string; postTitle?: string; createdAt: string
}
export interface PostDetail extends PostSummary {
  contentHtml: string; categoryId?: number; comments: CommentItem[]; createdAt: string; updatedAt: string
}
export interface PagedPosts { items: PostSummary[]; page: number; size: number; total: number; pages: number }
