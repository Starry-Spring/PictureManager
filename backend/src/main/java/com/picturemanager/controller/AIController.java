package com.picturemanager.controller;

import com.picturemanager.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * 分析单张图片
     */
    @PostMapping("/analyze/{imageId}")
    public ResponseEntity<?> analyzeImage(
            @PathVariable Long imageId,
            @RequestParam("userId") Long userId) {
        try {
            Map<String, Object> result = aiService.analyzeImage(imageId, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("success", "false");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 通过自然语言搜索图片
     */
    @PostMapping("/search")
    public ResponseEntity<?> searchImages(
            @RequestParam("userId") Long userId,
            @RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            if (query == null || query.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "搜索关键词不能为空");
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> result = aiService.searchImagesByQuery(query, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * AI对话接口
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestParam("userId") Long userId,
            @RequestBody Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
            
            if (message == null || message.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "消息不能为空");
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> result = aiService.chat(message, userId, history);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
