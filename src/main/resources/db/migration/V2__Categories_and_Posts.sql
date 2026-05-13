-- V2__Categories_and_Posts.sql
-- Categories and Posts tables

CREATE TABLE `categories`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(500),
    `created_at`  DATETIME     NOT NULL,
    `updated_at`  DATETIME     NOT NULL
);

CREATE TABLE `posts`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title`       VARCHAR(255) NOT NULL UNIQUE,
    `description` VARCHAR(500) NOT NULL,
    `content`     TEXT         NOT NULL,
    `slug`        VARCHAR(255) NOT NULL UNIQUE,
    `user_id`     BIGINT       NOT NULL,
    `category_id` BIGINT       NOT NULL,
    `created_at`  DATETIME     NOT NULL,
    `updated_at`  DATETIME     NOT NULL,
    CONSTRAINT `fk_posts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_posts_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE,
    INDEX `idx_posts_user_id` (`user_id`),
    INDEX `idx_posts_category_id` (`category_id`)
);