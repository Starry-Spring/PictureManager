// src/main/java/com/picturemanager/PictureManagerApplication.java
package com.picturemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.picturemanager.config.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class PictureManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureManagerApplication.class, args);
    }
}