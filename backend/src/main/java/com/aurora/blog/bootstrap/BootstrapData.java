package com.aurora.blog.bootstrap;

import com.aurora.blog.mapper.BlogMapper;
import com.aurora.blog.model.NewUser;
import com.aurora.blog.model.PostDraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class BootstrapData implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BootstrapData.class);
    private final BlogMapper mapper;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;
    private final String email;

    public BootstrapData(BlogMapper mapper, PasswordEncoder encoder,
                         @Value("${aurora.bootstrap-admin.username}") String username,
                         @Value("${aurora.bootstrap-admin.password}") String password,
                         @Value("${aurora.bootstrap-admin.email}") String email) {
        this.mapper = mapper;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Object> admin = mapper.findAuthUser(username);
        if (admin == null) {
            NewUser user = new NewUser(username, email, encoder.encode(password), "Aurora 编辑部", "ADMIN");
            mapper.insertUser(user);
            admin = mapper.findAuthUser(username);
            log.info("已创建本地管理员账号：{}。首次登录后请修改密码。", username);
        }
        if (mapper.countPosts() == 0) {
            long adminId = ((Number) admin.get("id")).longValue();
            Long engineering = mapper.findCategoryIdBySlug("engineering");
            Long design = mapper.findCategoryIdBySlug("design");
            mapper.insertPost(new PostDraft(adminId, engineering, "把复杂系统写成一封清楚的信",
                    "writing-systems-clearly", "一次关于代码边界、可靠反馈与工程表达的实践记录。",
                    "<h2>先让系统说人话</h2><p>好的后端不仅返回数据，也应该让失败可理解、让恢复有路径。</p><blockquote>可靠不是没有错误，而是错误发生时仍然知道下一步。</blockquote><h2>从一个闭环开始</h2><p>认证、内容、审核与部署必须在同一条真实路径里被验证。</p>",
                    null, "PUBLISHED"));
            mapper.insertPost(new PostDraft(adminId, design, "留白不是空白：阅读界面的节奏",
                    "rhythm-of-reading", "从排版、色彩和交互状态出发，构建真正适合阅读的博客。",
                    "<h2>内容先于容器</h2><p>卡片不是默认答案。标题、摘要和时间本身就能形成稳定的阅读节奏。</p><h2>交互应该有回声</h2><p>加载、错误、空状态和成功反馈，都是界面叙事的一部分。</p>",
                    null, "PUBLISHED"));
        }
    }
}
