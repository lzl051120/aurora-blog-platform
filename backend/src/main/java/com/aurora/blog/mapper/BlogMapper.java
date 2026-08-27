package com.aurora.blog.mapper;

import com.aurora.blog.model.NewUser;
import com.aurora.blog.model.PostDraft;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface BlogMapper {
    @Select("""
        SELECT id, username, email, password_hash AS passwordHash, display_name AS displayName,
               bio, avatar_url AS avatarUrl, role, status, created_at AS createdAt
        FROM users WHERE username = #{login} OR email = #{login} LIMIT 1
        """)
    Map<String, Object> findAuthUser(@Param("login") String login);

    @Select("""
        SELECT id, username, email, display_name AS displayName, bio, avatar_url AS avatarUrl,
               role, status, created_at AS createdAt
        FROM users WHERE id = #{id}
        """)
    Map<String, Object> findSafeUser(@Param("id") long id);

    @Insert("""
        INSERT INTO users(username, email, password_hash, display_name, role)
        VALUES(#{username}, #{email}, #{passwordHash}, #{displayName}, #{role})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(NewUser user);

    @Update("""
        UPDATE users SET display_name=#{displayName}, bio=#{bio}, avatar_url=#{avatarUrl}
        WHERE id=#{id}
        """)
    int updateProfile(@Param("id") long id, @Param("displayName") String displayName,
                      @Param("bio") String bio, @Param("avatarUrl") String avatarUrl);

    @Update("UPDATE users SET password_hash=#{passwordHash} WHERE id=#{id}")
    int updatePassword(@Param("id") long id, @Param("passwordHash") String passwordHash);

    @Select("SELECT COUNT(*) FROM users")
    long countUsers();

    @Select("SELECT COUNT(*) FROM users WHERE username=#{username} OR email=#{email}")
    int countDuplicateUser(@Param("username") String username, @Param("email") String email);

    @Select("""
        SELECT id, name, slug, description, sort_order AS sortOrder,
               (SELECT COUNT(*) FROM posts p WHERE p.category_id=c.id AND p.status='PUBLISHED') AS postCount
        FROM categories c ORDER BY sort_order, name
        """)
    List<Map<String, Object>> listCategories();

    @Select("SELECT id FROM categories WHERE slug=#{slug} LIMIT 1")
    Long findCategoryIdBySlug(@Param("slug") String slug);

    @Insert("INSERT INTO categories(name, slug, description, sort_order) VALUES(#{name}, #{slug}, #{description}, #{sortOrder})")
    int insertCategory(@Param("name") String name, @Param("slug") String slug,
                       @Param("description") String description, @Param("sortOrder") int sortOrder);

    @Update("UPDATE categories SET name=#{name}, slug=#{slug}, description=#{description}, sort_order=#{sortOrder} WHERE id=#{id}")
    int updateCategory(@Param("id") long id, @Param("name") String name, @Param("slug") String slug,
                       @Param("description") String description, @Param("sortOrder") int sortOrder);

    @Delete("DELETE FROM categories WHERE id=#{id}")
    int deleteCategory(@Param("id") long id);

    @Select("""
        <script>
        SELECT p.id, p.title, p.slug, p.summary, p.cover_url AS coverUrl, p.view_count AS viewCount,
               p.published_at AS publishedAt, p.created_at AS createdAt,
               c.name AS categoryName, c.slug AS categorySlug,
               u.display_name AS authorName
        FROM posts p
        JOIN users u ON u.id=p.author_id
        LEFT JOIN categories c ON c.id=p.category_id
        WHERE p.status='PUBLISHED'
        <if test='search != null and search != ""'>
          AND (p.title LIKE CONCAT('%', #{search}, '%') OR p.summary LIKE CONCAT('%', #{search}, '%'))
        </if>
        <if test='category != null and category != ""'>AND c.slug=#{category}</if>
        <choose>
          <when test='sort == "hot"'>ORDER BY p.view_count DESC, p.published_at DESC</when>
          <otherwise>ORDER BY p.published_at DESC</otherwise>
        </choose>
        LIMIT #{limit} OFFSET #{offset}
        </script>
        """)
    List<Map<String, Object>> listPublishedPosts(@Param("search") String search,
                                                  @Param("category") String category,
                                                  @Param("sort") String sort,
                                                  @Param("limit") int limit,
                                                  @Param("offset") int offset);

    @Select("""
        <script>
        SELECT COUNT(*) FROM posts p LEFT JOIN categories c ON c.id=p.category_id
        WHERE p.status='PUBLISHED'
        <if test='search != null and search != ""'>
          AND (p.title LIKE CONCAT('%', #{search}, '%') OR p.summary LIKE CONCAT('%', #{search}, '%'))
        </if>
        <if test='category != null and category != ""'>AND c.slug=#{category}</if>
        </script>
        """)
    long countPublishedPosts(@Param("search") String search, @Param("category") String category);

    @Select("""
        SELECT p.id, p.title, p.slug, p.summary, p.content_html AS contentHtml,
               p.cover_url AS coverUrl, p.status, p.view_count AS viewCount,
               p.published_at AS publishedAt, p.created_at AS createdAt, p.updated_at AS updatedAt,
               p.category_id AS categoryId, c.name AS categoryName, c.slug AS categorySlug,
               u.id AS authorId, u.display_name AS authorName, u.avatar_url AS authorAvatar
        FROM posts p JOIN users u ON u.id=p.author_id
        LEFT JOIN categories c ON c.id=p.category_id
        WHERE p.slug=#{slug} AND p.status='PUBLISHED'
        """)
    Map<String, Object> findPublishedPostBySlug(@Param("slug") String slug);

    @Select("""
        SELECT p.id, p.title, p.slug, p.summary, p.content_html AS contentHtml,
               p.cover_url AS coverUrl, p.status, p.view_count AS viewCount,
               p.published_at AS publishedAt, p.created_at AS createdAt, p.updated_at AS updatedAt,
               p.category_id AS categoryId, c.name AS categoryName
        FROM posts p LEFT JOIN categories c ON c.id=p.category_id WHERE p.id=#{id}
        """)
    Map<String, Object> findPostForAdmin(@Param("id") long id);

    @Update("UPDATE posts SET view_count=view_count+1 WHERE id=#{id}")
    int incrementPostView(@Param("id") long id);

    @Select("SELECT view_count FROM posts WHERE id=#{id}")
    long findPostViewCount(@Param("id") long id);

    @Select("SELECT COUNT(*) FROM posts")
    long countPosts();

    @Select("SELECT COUNT(*) FROM posts WHERE status='PUBLISHED'")
    long countPublished();

    @Select("SELECT COUNT(*) FROM posts WHERE slug=#{slug} AND (#{id} IS NULL OR id != #{id})")
    int countPostSlug(@Param("slug") String slug, @Param("id") Long id);

    @Insert("""
        INSERT INTO posts(author_id, category_id, title, slug, summary, content_html, cover_url, status, published_at)
        VALUES(#{authorId}, #{categoryId}, #{title}, #{slug}, #{summary}, #{contentHtml}, #{coverUrl}, #{status},
               CASE WHEN #{status}='PUBLISHED' THEN CURRENT_TIMESTAMP ELSE NULL END)
        """)
    int insertPost(PostDraft post);

    @Update("""
        UPDATE posts SET category_id=#{post.categoryId}, title=#{post.title}, slug=#{post.slug},
            summary=#{post.summary}, content_html=#{post.contentHtml}, cover_url=#{post.coverUrl},
            status=#{post.status},
            published_at=CASE WHEN #{post.status}='PUBLISHED' AND published_at IS NULL THEN CURRENT_TIMESTAMP
                              WHEN #{post.status}='DRAFT' THEN NULL ELSE published_at END
        WHERE id=#{id}
        """)
    int updatePost(@Param("id") long id, @Param("post") PostDraft post);

    @Delete("DELETE FROM posts WHERE id=#{id}")
    int deletePost(@Param("id") long id);

    @Select("""
        <script>
        SELECT p.id, p.title, p.slug, p.summary, p.cover_url AS coverUrl, p.status,
               p.view_count AS viewCount, p.updated_at AS updatedAt, p.published_at AS publishedAt,
               c.name AS categoryName
        FROM posts p LEFT JOIN categories c ON c.id=p.category_id
        <if test='status != null and status != ""'>WHERE p.status=#{status}</if>
        ORDER BY p.updated_at DESC
        </script>
        """)
    List<Map<String, Object>> listAdminPosts(@Param("status") String status);

    @Select("""
        SELECT cm.id, cm.content, cm.status, cm.created_at AS createdAt,
               u.display_name AS authorName, u.avatar_url AS authorAvatar
        FROM comments cm JOIN users u ON u.id=cm.user_id
        WHERE cm.post_id=#{postId} AND cm.status='APPROVED' ORDER BY cm.created_at
        """)
    List<Map<String, Object>> listApprovedComments(@Param("postId") long postId);

    @Insert("INSERT INTO comments(post_id, user_id, parent_id, content) VALUES(#{postId}, #{userId}, #{parentId}, #{content})")
    int insertComment(@Param("postId") long postId, @Param("userId") long userId,
                      @Param("parentId") Long parentId, @Param("content") String content);

    @Select("""
        SELECT cm.id, cm.content, cm.status, cm.created_at AS createdAt,
               p.title AS postTitle, u.display_name AS authorName
        FROM comments cm JOIN posts p ON p.id=cm.post_id JOIN users u ON u.id=cm.user_id
        ORDER BY cm.created_at DESC
        """)
    List<Map<String, Object>> listAllComments();

    @Update("UPDATE comments SET status=#{status} WHERE id=#{id}")
    int moderateComment(@Param("id") long id, @Param("status") String status);

    @Delete("DELETE FROM comments WHERE id=#{id}")
    int deleteComment(@Param("id") long id);

    @Select("SELECT COUNT(*) FROM comments WHERE status='PENDING'")
    long countPendingComments();

    @Insert("INSERT INTO guestbook_messages(user_id, content) VALUES(#{userId}, #{content})")
    int insertGuestbook(@Param("userId") long userId, @Param("content") String content);

    @Select("""
        SELECT g.id, g.content, g.created_at AS createdAt,
               u.display_name AS authorName, u.avatar_url AS authorAvatar
        FROM guestbook_messages g JOIN users u ON u.id=g.user_id
        WHERE g.status='APPROVED' ORDER BY g.created_at DESC LIMIT 100
        """)
    List<Map<String, Object>> listApprovedGuestbook();

    @Select("""
        SELECT g.id, g.content, g.status, g.created_at AS createdAt, u.display_name AS authorName
        FROM guestbook_messages g JOIN users u ON u.id=g.user_id ORDER BY g.created_at DESC
        """)
    List<Map<String, Object>> listAllGuestbook();

    @Update("UPDATE guestbook_messages SET status=#{status} WHERE id=#{id}")
    int moderateGuestbook(@Param("id") long id, @Param("status") String status);

    @Delete("DELETE FROM guestbook_messages WHERE id=#{id}")
    int deleteGuestbook(@Param("id") long id);

    @Select("""
        SELECT id, username, email, display_name AS displayName, role, status, created_at AS createdAt
        FROM users ORDER BY created_at DESC
        """)
    List<Map<String, Object>> listUsers();

    @Update("UPDATE users SET status=#{status} WHERE id=#{id} AND role!='ADMIN'")
    int updateUserStatus(@Param("id") long id, @Param("status") String status);

    @Insert("""
        INSERT INTO media_assets(uploader_id, original_name, stored_name, content_type, size_bytes, public_url)
        VALUES(#{uploaderId}, #{originalName}, #{storedName}, #{contentType}, #{sizeBytes}, #{publicUrl})
        """)
    int insertMedia(@Param("uploaderId") long uploaderId, @Param("originalName") String originalName,
                    @Param("storedName") String storedName, @Param("contentType") String contentType,
                    @Param("sizeBytes") long sizeBytes, @Param("publicUrl") String publicUrl);

    @Select("""
        SELECT id, original_name AS originalName, content_type AS contentType, size_bytes AS sizeBytes,
               public_url AS publicUrl, created_at AS createdAt FROM media_assets ORDER BY created_at DESC
        """)
    List<Map<String, Object>> listMedia();

    @Insert("""
        INSERT INTO audit_logs(actor_id, action, target_type, target_id, detail)
        VALUES(#{actorId}, #{action}, #{targetType}, #{targetId}, #{detail})
        """)
    int insertAudit(@Param("actorId") Long actorId, @Param("action") String action,
                    @Param("targetType") String targetType, @Param("targetId") String targetId,
                    @Param("detail") String detail);
}
