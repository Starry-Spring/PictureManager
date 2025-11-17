-- 创建数据库
CREATE DATABASE IF NOT EXISTS picture_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE picture_manager;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 图片表
CREATE TABLE IF NOT EXISTS images (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      user_id BIGINT NOT NULL,
                                      original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) UNIQUE NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    thumbnail_path VARCHAR(500),
    file_size BIGINT,
    mime_type VARCHAR(100),
    title VARCHAR(200),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 图片元数据表 (EXIF信息)
CREATE TABLE IF NOT EXISTS image_metadata (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              image_id BIGINT NOT NULL,
                                              camera_model VARCHAR(100),
    taken_time TIMESTAMP NULL,
    location VARCHAR(200),
    width INT,
    height INT,
    file_format VARCHAR(50),
    FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    name VARCHAR(50) UNIQUE NOT NULL,
    type ENUM('AUTO', 'MANUAL') DEFAULT 'MANUAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 图片-标签关联表
CREATE TABLE IF NOT EXISTS image_tags (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          image_id BIGINT NOT NULL,
                                          tag_id BIGINT NOT NULL,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    UNIQUE KEY unique_image_tag (image_id, tag_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始测试数据
INSERT IGNORE INTO users (id, username, password, email) VALUES
(1, 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV2UiC', 'test@example.com');

-- 插入一些示例标签
INSERT IGNORE INTO tags (id, name, type) VALUES
(1, '风景', 'AUTO'),
(2, '人物', 'AUTO'),
(3, '建筑', 'AUTO'),
(4, '美食', 'AUTO'),
(5, '旅行', 'MANUAL');