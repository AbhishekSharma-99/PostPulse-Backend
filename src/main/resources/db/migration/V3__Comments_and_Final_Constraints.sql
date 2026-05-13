-- V3__Comments_and_Final_Constraints.sql
-- Comments table (depends on posts)

CREATE TABLE `comments`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `body`       TEXT     NOT NULL,
    `user_id`    BIGINT   NOT NULL,
    `post_id`    BIGINT   NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_comments_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
    INDEX `idx_comments_user_id` (`user_id`),
    INDEX `idx_comments_post_id` (`post_id`)
);