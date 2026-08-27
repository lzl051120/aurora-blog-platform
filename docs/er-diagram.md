# ER 图

```mermaid
erDiagram
  USERS ||--o{ POSTS : writes
  USERS ||--o{ COMMENTS : creates
  USERS ||--o{ GUESTBOOK_MESSAGES : leaves
  USERS ||--o{ MEDIA_ASSETS : uploads
  USERS ||--o{ AUDIT_LOGS : acts
  CATEGORIES ||--o{ POSTS : contains
  POSTS ||--o{ COMMENTS : receives
  COMMENTS o|--o{ COMMENTS : replies
  USERS { bigint id PK; varchar username UK; varchar email UK; varchar password_hash; enum role; enum status }
  CATEGORIES { bigint id PK; varchar name UK; varchar slug UK; int sort_order }
  POSTS { bigint id PK; bigint author_id FK; bigint category_id FK; varchar slug UK; enum status; bigint view_count }
  COMMENTS { bigint id PK; bigint post_id FK; bigint user_id FK; bigint parent_id FK; enum status }
  GUESTBOOK_MESSAGES { bigint id PK; bigint user_id FK; enum status }
  MEDIA_ASSETS { bigint id PK; bigint uploader_id FK; varchar stored_name UK; bigint size_bytes }
  AUDIT_LOGS { bigint id PK; bigint actor_id FK; varchar action; varchar target_type }
```
