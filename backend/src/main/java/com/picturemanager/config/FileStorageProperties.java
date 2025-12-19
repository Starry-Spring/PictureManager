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
    private int thumbnailWidth = 300;
    private String allowedTypes = "image/jpeg,image/png,image/gif,image/webp";
}