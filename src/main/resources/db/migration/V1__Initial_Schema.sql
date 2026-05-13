-- V1__Initial_Schema.sql
-- Core tables and base schema

CREATE TABLE `users`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`       VARCHAR(255),
    `username`   VARCHAR(255) NOT NULL UNIQUE,
    `email`      VARCHAR(255) NOT NULL UNIQUE,
    `password`   VARCHAR(255) NOT NULL,
    `enabled`    BOOLEAN      NOT NULL DEFAULT TRUE,
    `created_at` DATETIME     NOT NULL,
    `updated_at` DATETIME     NOT NULL
);

CREATE TABLE `roles`
(
    `id`   BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE `users_roles`
(
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    CONSTRAINT `fk_users_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_users_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
);

-- after the users_roles table...
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_USER');