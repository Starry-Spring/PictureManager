-- 创建数据库
CREATE DATABASE IF NOT EXISTS picture_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE picture_manager;

-- 1. 用户表
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(50),
    avatar_url VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 图片元数据表
CREATE TABLE image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(50) NOT NULL,
    image_width INT,
    image_height INT,
    title VARCHAR(200),
    description TEXT,
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_uploaded_at (uploaded_at),
    INDEX idx_stored_filename (stored_filename)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 标签表
CREATE TABLE tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    type ENUM('SYSTEM', 'USER', 'AI') NOT NULL,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES user(id) ON DELETE SET NULL,
    INDEX idx_type (type),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 图片-标签关联表
CREATE TABLE image_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    image_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (image_id) REFERENCES image(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE,
    UNIQUE KEY uk_image_tag (image_id, tag_id),
    INDEX idx_image_id (image_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 图片 EXIF 信息表
CREATE TABLE image_exif (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    image_id BIGINT NOT NULL UNIQUE,
    camera_make VARCHAR(100),
    camera_model VARCHAR(100),
    taken_at DATETIME,
    exposure_time VARCHAR(20),
    f_number VARCHAR(20),
    iso_speed INT,
    focal_length VARCHAR(20),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (image_id) REFERENCES image(id) ON DELETE CASCADE,
    INDEX idx_taken_at (taken_at),
    INDEX idx_camera_model (camera_model)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 插入一些系统默认标签
INSERT INTO tag (name, type) VALUES
('风景', 'SYSTEM'),
('人物', 'SYSTEM'),
('动物', 'SYSTEM'),
('建筑', 'SYSTEM'),
('夜景', 'SYSTEM'),
('美食', 'SYSTEM'),
('旅行', 'SYSTEM'),
('工作', 'SYSTEM'),
('家庭', 'SYSTEM'),
('高清', 'SYSTEM'),
('竖版', 'SYSTEM'),
('横版', 'SYSTEM');