// src/main/java/com/picturemanager/controller/AuthController.java
package com.picturemanager.controller;

import com.picturemanager.dto.UserDTO;
import com.picturemanager.dto.LoginDTO;
import com.picturemanager.dto.UserResponseDTO;
import com.picturemanager.entity.User;
import com.picturemanager.security.JwtTokenProvider;
import com.picturemanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/hello")
    public String hello() {
        return "Hello from TestController!";
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        try {
            User user = userService.register(userDTO);

            // 创建响应对象，不生成token
            UserResponseDTO response = new UserResponseDTO();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setDisplayName(user.getDisplayName());
            response.setAvatarUrl(user.getAvatarUrl());
            response.setToken("NO_TOKEN_NEEDED"); // 简单占位符

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
//            System.out.println("=======");
//            System.out.println(loginDTO);
//            System.out.println("=======");
            String result = userService.login(loginDTO);

            // 直接查找用户，不进行密码验证
            Optional<User> userOptional;
            if (loginDTO.getUsernameOrEmail().contains("@")) {
                userOptional = userService.getUserByEmail(loginDTO.getUsernameOrEmail());
            } else {
                userOptional = userService.getUserByUsername(loginDTO.getUsernameOrEmail());
            }

            if (userOptional.isEmpty()) {
                throw new RuntimeException("用户不存在");
            }

            User user = userOptional.get();

            // 生成JWT令牌
            String token = jwtTokenProvider.generateToken(user.getUsername());

            // 创建响应对象
            UserResponseDTO response = new UserResponseDTO();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setDisplayName(user.getDisplayName());
            response.setAvatarUrl(user.getAvatarUrl());
            response.setToken(token);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                Optional<User> userOptional = userService.getUserByUsername(username);
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    
                    // 创建响应对象
                    UserResponseDTO response = new UserResponseDTO();
                    response.setId(user.getId());
                    response.setUsername(user.getUsername());
                    response.setEmail(user.getEmail());
                    response.setDisplayName(user.getDisplayName());
                    response.setAvatarUrl(user.getAvatarUrl());
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "无效的令牌"));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestParam Long userId, @RequestBody Map<String, String> request) {
        try {
            String displayName = request.get("displayName");
            User user = userService.updateProfile(userId, displayName);
            
            UserResponseDTO response = new UserResponseDTO();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setDisplayName(user.getDisplayName());
            response.setAvatarUrl(user.getAvatarUrl());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam Long userId, @RequestBody Map<String, String> request) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                throw new RuntimeException("参数不完整");
            }
            
            userService.changePassword(userId, currentPassword, newPassword);
            
            return ResponseEntity.ok(Map.of("message", "密码修改成功"));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}