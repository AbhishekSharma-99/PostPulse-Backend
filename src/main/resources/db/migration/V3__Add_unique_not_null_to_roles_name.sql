-- V3__Add_unique_not_null_to_roles.sql
ALTER TABLE roles MODIFY COLUMN name VARCHAR(255) NOT NULL;
ALTER TABLE roles ADD CONSTRAINT uq_roles_name UNIQUE (name);
