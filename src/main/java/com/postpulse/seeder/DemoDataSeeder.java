package com.postpulse.seeder;

import com.postpulse.entity.Category;
import com.postpulse.entity.Comment;
import com.postpulse.entity.Post;
import com.postpulse.entity.User;
import com.postpulse.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
@ConditionalOnProperty(value = "app.seed-data.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class DemoDataSeeder implements CommandLineRunner, Ordered {

    private final UserSeeder userSeeder;
    private final CategorySeeder categorySeeder;
    private final PostSeeder postSeeder;
    private final CommentSeeder commentSeeder;
    private final UserRepository userRepository;

    public DemoDataSeeder(UserSeeder userSeeder,
                          CategorySeeder categorySeeder,
                          PostSeeder postSeeder,
                          CommentSeeder commentSeeder,
                          UserRepository userRepository) {
        this.userSeeder = userSeeder;
        this.categorySeeder = categorySeeder;
        this.postSeeder = postSeeder;
        this.commentSeeder = commentSeeder;
        this.userRepository = userRepository;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping...");
            return;
        }

        log.info("Seeding demo data...");
        try {
            List<User>     users      = userSeeder.seed();
            List<Category> categories = categorySeeder.seed();
            List<Post>     posts      = postSeeder.seed(users, categories);
            List<Comment>  comments   = commentSeeder.seed(users, posts);

            log.info("Seeded {} users, {} categories, {} posts, {} comments",
                    users.size(), categories.size(), posts.size(), comments.size());
        } catch (Exception e) {
            log.error("Seeding failed", e);
            throw new RuntimeException("Demo data seeding failed", e);
        }
    }
}