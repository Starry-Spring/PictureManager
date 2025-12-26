// src/main/java/com/picturemanager/controller/ImageController.java
package com.picturemanager.controller;

import com.picturemanager.dto.ImageDTO;
import com.picturemanager.dto.ImageResponseDTO;
import com.picturemanager.dto.PaginatedResponse;
import com.picturemanager.service.ImageService;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    @Autowired
    private ImageService imageService;

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
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {

        PaginatedResponse<ImageResponseDTO> response = imageService.getUserImages(userId, page, size, sortBy, direction);
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
            Path imagePath = Paths.get(image.getFilePath());
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateImage(
            @PathVariable Long id,
            @RequestParam("userId") Long userId,
            @Valid @RequestBody ImageDTO imageDTO) {
        try {
            ImageResponseDTO response = imageService.updateImage(userId, id, imageDTO);
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
}