package com.picturemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.picturemanager.entity.Image;
import com.picturemanager.repository.ImageRepository;
import com.picturemanager.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MCP (Model Context Protocol) 接口
 * 提供给大模型的工具接口，用于检索和管理图片
 */
@RestController
@RequestMapping("/api/mcp")
@CrossOrigin(origins = "*")
public class MCPController {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取可用工具列表
     */
    @GetMapping("/tools")
    public ResponseEntity<?> listTools() {
        List<Map<String, Object>> tools = new ArrayList<>();

        // 搜索图片工具
        Map<String, Object> searchTool = new HashMap<>();
        searchTool.put("name", "search_images");
        searchTool.put("description", "通过关键词搜索图片。可以搜索标题、描述或标签中包含关键词的图片。");
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("type", "object");
        Map<String, Object> searchProperties = new HashMap<>();
        searchProperties.put("query", Map.of("type", "string", "description", "搜索关键词"));
        searchProperties.put("userId", Map.of("type", "integer", "description", "用户ID"));
        searchParams.put("properties", searchProperties);
        searchParams.put("required", List.of("query", "userId"));
        searchTool.put("inputSchema", searchParams);
        tools.add(searchTool);

        // 获取图片信息工具
        Map<String, Object> getImageTool = new HashMap<>();
        getImageTool.put("name", "get_image_info");
        getImageTool.put("description", "获取指定图片的详细信息，包括标题、描述、标签、尺寸等。");
        Map<String, Object> getParams = new HashMap<>();
        getParams.put("type", "object");
        Map<String, Object> getProperties = new HashMap<>();
        getProperties.put("imageId", Map.of("type", "integer", "description", "图片ID"));
        getProperties.put("userId", Map.of("type", "integer", "description", "用户ID"));
        getParams.put("properties", getProperties);
        getParams.put("required", List.of("imageId", "userId"));
        getImageTool.put("inputSchema", getParams);
        tools.add(getImageTool);

        // 列出所有图片工具
        Map<String, Object> listTool = new HashMap<>();
        listTool.put("name", "list_images");
        listTool.put("description", "列出用户的所有图片，返回图片列表及其基本信息。");
        Map<String, Object> listParams = new HashMap<>();
        listParams.put("type", "object");
        Map<String, Object> listProperties = new HashMap<>();
        listProperties.put("userId", Map.of("type", "integer", "description", "用户ID"));
        listProperties.put("limit", Map.of("type", "integer", "description", "返回数量限制，默认20", "default", 20));
        listParams.put("properties", listProperties);
        listParams.put("required", List.of("userId"));
        listTool.put("inputSchema", listParams);
        tools.add(listTool);

        // AI搜索工具
        Map<String, Object> aiSearchTool = new HashMap<>();
        aiSearchTool.put("name", "ai_search");
        aiSearchTool.put("description", "使用AI理解自然语言查询并搜索最匹配的图片。支持模糊搜索和语义搜索。");
        Map<String, Object> aiSearchParams = new HashMap<>();
        aiSearchParams.put("type", "object");
        Map<String, Object> aiSearchProperties = new HashMap<>();
        aiSearchProperties.put("query", Map.of("type", "string", "description", "自然语言搜索查询"));
        aiSearchProperties.put("userId", Map.of("type", "integer", "description", "用户ID"));
        aiSearchParams.put("properties", aiSearchProperties);
        aiSearchParams.put("required", List.of("query", "userId"));
        aiSearchTool.put("inputSchema", aiSearchParams);
        tools.add(aiSearchTool);

        // 获取标签列表工具
        Map<String, Object> tagsTool = new HashMap<>();
        tagsTool.put("name", "list_tags");
        tagsTool.put("description", "获取用户所有图片的标签列表。");
        Map<String, Object> tagsParams = new HashMap<>();
        tagsParams.put("type", "object");
        Map<String, Object> tagsProperties = new HashMap<>();
        tagsProperties.put("userId", Map.of("type", "integer", "description", "用户ID"));
        tagsParams.put("properties", tagsProperties);
        tagsParams.put("required", List.of("userId"));
        tagsTool.put("inputSchema", tagsParams);
        tools.add(tagsTool);

        Map<String, Object> response = new HashMap<>();
        response.put("tools", tools);
        return ResponseEntity.ok(response);
    }

    /**
     * 执行工具调用
     */
    @PostMapping("/tools/call")
    public ResponseEntity<?> callTool(@RequestBody Map<String, Object> request) {
        String toolName = (String) request.get("name");
        Map<String, Object> arguments = (Map<String, Object>) request.get("arguments");

        if (toolName == null || arguments == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "缺少必要参数"));
        }

        try {
            Map<String, Object> result = switch (toolName) {
                case "search_images" -> searchImages(arguments);
                case "get_image_info" -> getImageInfo(arguments);
                case "list_images" -> listImages(arguments);
                case "ai_search" -> aiSearch(arguments);
                case "list_tags" -> listTags(arguments);
                default -> Map.of("error", "未知工具: " + toolName);
            };

            Map<String, Object> response = new HashMap<>();
            response.put("content", List.of(Map.of("type", "text", "text", objectMapper.writeValueAsString(result))));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> searchImages(Map<String, Object> args) {
        String query = (String) args.get("query");
        Long userId = Long.valueOf(args.get("userId").toString());

        List<Image> images = imageRepository.findByUserIdAndIsDeletedFalse(userId);

        // 简单的关键词搜索
        List<Map<String, Object>> results = images.stream()
                .filter(img -> {
                    String title = img.getTitle() != null ? img.getTitle().toLowerCase() : "";
                    String desc = img.getDescription() != null ? img.getDescription().toLowerCase() : "";
                    String tags = img.getTags() != null ? 
                            img.getTags().stream().map(t -> t.getName().toLowerCase()).collect(Collectors.joining(" ")) : "";
                    String searchLower = query.toLowerCase();
                    return title.contains(searchLower) || desc.contains(searchLower) || tags.contains(searchLower);
                })
                .map(this::imageToMap)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("query", query);
        response.put("totalResults", results.size());
        response.put("images", results);
        return response;
    }

    private Map<String, Object> getImageInfo(Map<String, Object> args) {
        Long imageId = Long.valueOf(args.get("imageId").toString());
        Long userId = Long.valueOf(args.get("userId").toString());

        Optional<Image> imageOpt = imageRepository.findById(imageId);
        if (imageOpt.isEmpty()) {
            return Map.of("success", false, "error", "图片不存在");
        }

        Image image = imageOpt.get();
        if (!image.getUser().getId().equals(userId)) {
            return Map.of("success", false, "error", "无权访问此图片");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("image", imageToDetailMap(image));
        return response;
    }

    private Map<String, Object> listImages(Map<String, Object> args) {
        Long userId = Long.valueOf(args.get("userId").toString());
        int limit = args.containsKey("limit") ? Integer.parseInt(args.get("limit").toString()) : 20;

        List<Image> images = imageRepository.findByUserIdAndIsDeletedFalse(userId);
        
        List<Map<String, Object>> results = images.stream()
                .limit(limit)
                .map(this::imageToMap)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("totalImages", images.size());
        response.put("returnedCount", results.size());
        response.put("images", results);
        return response;
    }

    private Map<String, Object> aiSearch(Map<String, Object> args) {
        String query = (String) args.get("query");
        Long userId = Long.valueOf(args.get("userId").toString());

        return aiService.searchImagesByQuery(query, userId);
    }

    private Map<String, Object> listTags(Map<String, Object> args) {
        Long userId = Long.valueOf(args.get("userId").toString());

        List<Image> images = imageRepository.findByUserIdAndIsDeletedFalse(userId);
        
        Set<String> allTags = images.stream()
                .filter(img -> img.getTags() != null)
                .flatMap(img -> img.getTags().stream())
                .map(tag -> tag.getName())
                .collect(Collectors.toSet());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("totalTags", allTags.size());
        response.put("tags", new ArrayList<>(allTags));
        return response;
    }

    private Map<String, Object> imageToMap(Image image) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", image.getId());
        map.put("title", image.getTitle());
        map.put("description", image.getDescription());
        if (image.getTags() != null) {
            map.put("tags", image.getTags().stream().map(t -> t.getName()).collect(Collectors.toList()));
        }
        return map;
    }

    private Map<String, Object> imageToDetailMap(Image image) {
        Map<String, Object> map = imageToMap(image);
        map.put("originalFilename", image.getOriginalFilename());
        map.put("width", image.getImageWidth());
        map.put("height", image.getImageHeight());
        map.put("fileSize", image.getFileSize());
        map.put("mimeType", image.getMimeType());
        map.put("uploadedAt", image.getUploadedAt() != null ? image.getUploadedAt().toString() : null);
        map.put("thumbnailPath", image.getThumbnailPath());
        
        if (image.getExif() != null) {
            Map<String, Object> exif = new HashMap<>();
            exif.put("cameraMake", image.getExif().getCameraMake());
            exif.put("cameraModel", image.getExif().getCameraModel());
            exif.put("takenAt", image.getExif().getTakenAt() != null ? image.getExif().getTakenAt().toString() : null);
            map.put("exif", exif);
        }
        return map;
    }
}
