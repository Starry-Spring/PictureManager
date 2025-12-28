// src/main/java/com/picturemanager/controller/ImageController.java
package com.picturemanager.controller;

import com.picturemanager.dto.ImageDTO;
import com.picturemanager.dto.ImageResponseDTO;
import com.picturemanager.dto.ImageUpdateDTO;
import com.picturemanager.dto.PaginatedResponse;
import com.picturemanager.entity.User;
import com.picturemanager.repository.UserRepository;
import com.picturemanager.security.JwtTokenProvider;
import com.picturemanager.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    @Autowired
    private ImageService imageService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("userId") Long userId,
            @Valid @ModelAttribute ImageDTO imageDTO) {
        try {
            ImageResponseDTO response = imageService.uploadImage(userId, imageDTO);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ImageResponseDTO>> getUserImages(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "uploadedAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "tag", required = false) String tag) {

        PaginatedResponse<ImageResponseDTO> response = imageService.getUserImages(userId, page, size, sortBy, direction, keyword, searchType, tag);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getImage(
            @PathVariable Long id,
            @RequestParam("userId") Long userId) {
        try {
            ImageResponseDTO response = imageService.getImageById(userId, id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }
    
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getImageFile(
            @PathVariable Long id,
            @RequestParam("userId") Long userId) {
        try {
            ImageResponseDTO image = imageService.getImageById(userId, id);
            System.out.println("=== 图片访问调试信息 (userId方式) ===");
            System.out.println("图片ID: " + id);
            System.out.println("用户ID: " + userId);
            System.out.println("文件路径: " + image.getFilePath());
            System.out.println("存储文件名: " + image.getStoredFilename());
            System.out.println("MIME类型: " + image.getMimeType());
            
            Path imagePath = Paths.get(image.getFilePath());
            System.out.println("解析的路径: " + imagePath.toString());
            System.out.println("文件是否存在: " + Files.exists(imagePath));
            System.out.println("文件是否可读: " + Files.isReadable(imagePath));
            System.out.println("===================");
            
            Resource resource = new UrlResource(imagePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.getMimeType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getStoredFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            System.out.println("运行时异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            System.out.println("其他异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping(value = "/{id}/file", params = "token")
    public ResponseEntity<Resource> getImageFileWithToken(
            @PathVariable Long id,
            @RequestParam("token") String token,
            HttpServletRequest request) {
        try {
            // 从token中解析用户信息
            String username = jwtTokenProvider.getUsernameFromToken(token);
            
            // 验证token
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 通过用户名获取用户
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Long userId = userOptional.get().getId();
            
            // 获取图片信息
            ImageResponseDTO image = imageService.getImageById(userId, id);
            System.out.println("=== 图片访问调试信息 ===");
            System.out.println("图片ID: " + id);
            System.out.println("用户ID: " + userId);
            System.out.println("文件路径: " + image.getFilePath());
            System.out.println("存储文件名: " + image.getStoredFilename());
            System.out.println("MIME类型: " + image.getMimeType());
            
            Path imagePath = Paths.get(image.getFilePath());
            System.out.println("解析的路径: " + imagePath.toString());
            System.out.println("文件是否存在: " + Files.exists(imagePath));
            System.out.println("文件是否可读: " + Files.isReadable(imagePath));
            System.out.println("===================");
            
            Resource resource = new UrlResource(imagePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.getMimeType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getStoredFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            System.out.println("运行时异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            System.out.println("其他异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateImage(
            @PathVariable Long id,
            @RequestParam("userId") Long userId,
            @RequestBody ImageUpdateDTO imageUpdateDTO) {
        try {
            ImageResponseDTO response = imageService.updateImage(userId, id, imageUpdateDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(
            @PathVariable Long id,
            @RequestParam("userId") Long userId) {
        try {
            imageService.deleteImage(userId, id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "图片删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ImageResponseDTO>> getRecentImages(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        System.out.println("========================================");
        System.out.println("GET /recent 被调用！");
        System.out.println("userId: " + userId);
        System.out.println("limit: " + limit);
        System.out.println("========================================");

        List<ImageResponseDTO> response = imageService.getRecentImages(userId, limit);
        System.out.println("返回数据量: " + response.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getUserTags(@RequestParam("userId") Long userId) {
        List<String> tags = imageService.getUserTags(userId);
        return ResponseEntity.ok(tags);
    }
    
    @GetMapping("/{id}/exif")
    public ResponseEntity<?> getImageExif(
            @PathVariable Long id,
            @RequestParam("userId") Long userId) {
        try {
            ImageResponseDTO response = imageService.getImageById(userId, id);
            // 返回包含EXIF信息的响应
            Map<String, Object> exifData = new HashMap<>();
            exifData.put("DateTime", response.getTakenAt());
            exifData.put("Make", response.getCameraMake());
            exifData.put("Model", response.getCameraModel());
            exifData.put("ExposureTime", response.getExposureTime());
            exifData.put("FNumber", response.getFNumber());
            exifData.put("ISO", response.getIsoSpeed());
            exifData.put("FocalLength", response.getFocalLength());
            exifData.put("GPSLatitude", response.getLatitude());
            exifData.put("GPSLongitude", response.getLongitude());
            
            return ResponseEntity.ok(exifData);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }
}