// src/main/java/com/picturemanager/service/UserService.java
package com.picturemanager.service;

import com.picturemanager.entity.User;
import com.picturemanager.dto.UserDTO;
import com.picturemanager.dto.LoginDTO;
import com.picturemanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 完全移除 PasswordEncoder 字段和相关注解

    @Transactional
    public User register(UserDTO userDTO) {
        try {
            // 创建用户
            User user = new User();
            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());

            // 直接存储密码（仅用于开发测试）
            user.setPasswordHash(userDTO.getPassword());

            user.setDisplayName(userDTO.getDisplayName() != null ?
                    userDTO.getDisplayName() : userDTO.getUsername());
            user.setAvatarUrl("/assets/default-avatar.png");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            return userRepository.save(user);
        } catch (Exception e) {
            // 检查是否是唯一约束冲突
            if (e.getMessage().contains("username") && e.getMessage().contains("Duplicate entry")) {
                throw new RuntimeException("用户名已存在");
            } else if (e.getMessage().contains("email") && e.getMessage().contains("Duplicate entry")) {
                throw new RuntimeException("邮箱已被注册");
            } else {
                // 重新抛出其他异常
                throw e;
            }
        }
    }

    @Transactional
    public String login(LoginDTO loginDTO) {
//        System.out.println("@@@@====@@@@");
//        System.out.println("UserName: " + loginDTO.getUsernameOrEmail());
//        System.out.println("Password: " + loginDTO.getPassword());
//        System.out.println("@@@@@@@@@@");
        Optional<User> userOptional;

        // 判断是用户名还是邮箱登录
        if (loginDTO.getUsernameOrEmail().contains("@")) {
            userOptional = userRepository.findByEmail(loginDTO.getUsernameOrEmail());
        } else {
            userOptional = userRepository.findByUsername(loginDTO.getUsernameOrEmail());
        }

        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOptional.get();

//        System.out.println("@@@@@@@@@@@");
//        System.out.println("User password in db: " + user.getPasswordHash());
//        System.out.println("@@@@@@@@@@@");

        // 简单明文密码比较
        if (!loginDTO.getPassword().equals(user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return "LOGIN_SUCCESS";
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateProfile(Long userId, String displayName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (displayName != null && !displayName.trim().isEmpty()) {
            user.setDisplayName(displayName.trim());
        }
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证当前密码
        if (!currentPassword.equals(user.getPasswordHash())) {
            throw new RuntimeException("当前密码错误");
        }
        
        // 更新密码
        user.setPasswordHash(newPassword);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
    }
}