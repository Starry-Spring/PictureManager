// src/main/java/com/picturemanager/config/FileStorageProperties.java
package com.picturemanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {
    private String uploadDir = "./uploads";
    private String thumbnailDir = "./uploads/thumbnails";
    private int thumbnailWidth = 300;
    private String allowedTypes = "image/jpeg,image/png,image/gif,image/webp";

    // 确保 Lombok 生成 getter，或者手动添加
    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getThumbnailDir() {
        return thumbnailDir;
    }

    public void setThumbnailDir(String thumbnailDir) {
        this.thumbnailDir = thumbnailDir;
    }

    public String getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(String allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }
}