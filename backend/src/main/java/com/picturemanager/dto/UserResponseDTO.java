// src/main/java/com/picturemanager/dto/UserResponseDTO.java
package com.picturemanager.dto;

import com.picturemanager.entity.User;
import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String token;

    public UserResponseDTO(User user, String token) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.avatarUrl = user.getAvatarUrl();
        this.token = token;
    }

    public UserResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}