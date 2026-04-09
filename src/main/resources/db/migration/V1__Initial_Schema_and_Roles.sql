-- 1. Create Independent Tables (Parents)
CREATE TABLE `categories` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `description` varchar(255) DEFAULT NULL,
                              `name` varchar(255) DEFAULT NULL,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `roles` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `name` varchar(255) DEFAULT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `users` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `email` varchar(255) NOT NULL,
                         `name` varchar(255) DEFAULT NULL,
                         `password` varchar(255) NOT NULL,
                         `username` varchar(255) NOT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `UK_email` (`email`),
                         UNIQUE KEY `UK_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Create Dependent Tables (Children)
CREATE TABLE `posts` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `content` varchar(255) NOT NULL,
                         `description` varchar(255) NOT NULL,
                         `title` varchar(255) NOT NULL,
                         `category_id` bigint DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `UK_title` (`title`),
                         CONSTRAINT `FK_post_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `comment` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `body` varchar(255) DEFAULT NULL,
                           `email` varchar(255) DEFAULT NULL,
                           `name` varchar(255) DEFAULT NULL,
                           `post_id` bigint NOT NULL,
                           PRIMARY KEY (`id`),
                           CONSTRAINT `FK_comment_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `users_roles` (
                               `user_id` bigint NOT NULL,
                               `role_id` bigint NOT NULL,
                               PRIMARY KEY (`user_id`,`role_id`),
                               CONSTRAINT `FK_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                               CONSTRAINT `FK_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. Seed Initial Data
INSERT INTO `roles` (id, name) VALUES (1,'ROLE_ADMIN');
INSERT INTO `roles` (id, name) VALUES (2,'ROLE_USER');