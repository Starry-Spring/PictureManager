package com.picturemanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.picturemanager.entity.Image;
import com.picturemanager.entity.Tag;
import com.picturemanager.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AIService {

    @Value("${app.deepseek.api-key}")
    private String apiKey;

    @Value("${app.deepseek.api-url}")
    private String apiUrl;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private RestTemplate restTemplate;

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    // ==================== MCP 工具实现 ====================

    /**
     * MCP工具：获取用户图片统计
     */
    private Map<String, Object> toolGetStats(Long userId) {
        List<Image> images = imageRepository.findByUserIdAndIsDeletedFalse(userId);
        
        Map<String, Integer> tagCounts = new HashMap<>();
        for (Image img : images) {
            if (img.getTags() != null) {
                for (Tag tag : img.getTags()) {
                    tagCounts.merge(tag.getName(), 1, Integer::sum);
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalImages", images.size());
        result.put("tags", tagCounts);
        return result;
    }

    /**
     * MCP工具：搜索图片
     */
    private Map<String, Object> toolSearchImages(String query, Long userId) {
        List<Image> allImages = imageRepository.findByUserIdAndIsDeletedFalse(userId);
        String lowerQuery = query.toLowerCase();
        
        List<Map<String, Object>> matched = new ArrayList<>();
        for (Image img : allImages) {
            boolean match = false;
            
            // 匹配标题
            if (img.getTitle() != null && img.getTitle().toLowerCase().contains(lowerQuery)) {
                match = true;
            }
            // 匹配描述
            if (img.getDescription() != null && img.getDescription().toLowerCase().contains(lowerQuery)) {
                match = true;
            }
            // 匹配标签
            if (img.getTags() != null) {
                for (Tag tag : img.getTags()) {
                    if (tag.getName().toLowerCase().contains(lowerQuery)) {
                        match = true;
                        break;
                    }
                }
            }
            // 匹配文件名
            if (img.getOriginalFilename() != null && img.getOriginalFilename().toLowerCase().contains(lowerQuery)) {
                match = true;
            }
            
            if (match) {
                Map<String, Object> imageInfo = new HashMap<>();
                imageInfo.put("id", img.getId());
                imageInfo.put("title", img.getTitle() != null ? img.getTitle() : "未命名");
                imageInfo.put("description", img.getDescription());
                imageInfo.put("tags", img.getTags() != null ? 
                    img.getTags().stream().map(Tag::getName).collect(Collectors.toList()) : 
                    Collections.emptyList());
                matched.add(imageInfo);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("query", query);
        result.put("count", matched.size());
        result.put("images", matched);
        return result;
    }

    /**
     * MCP工具：列出所有标签
     */
    private Map<String, Object> toolListTags(Long userId) {
        List<Image> images = imageRepository.findByUserIdAndIsDeletedFalse(userId);
        
        Map<String, Integer> tagCounts = new HashMap<>();
        for (Image img : images) {
            if (img.getTags() != null) {
                for (Tag tag : img.getTags()) {
                    tagCounts.merge(tag.getName(), 1, Integer::sum);
                }
            }
        }
        
        List<Map<String, Object>> tagList = tagCounts.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .map(e -> {
                Map<String, Object> m = new HashMap<>();
                m.put("name", e.getKey());
                m.put("count", e.getValue());
                return m;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("tags", tagList);
        result.put("totalTags", tagList.size());
        return result;
    }

    // ==================== 意图检测 ====================

    private enum Intent {
        SEARCH_IMAGES,      // 搜索图片
        GET_STATS,          // 获取统计信息
        LIST_TAGS,          // 列出标签
        GENERAL_CHAT        // 普通对话
    }

    private Intent detectIntent(String message) {
        String lower = message.toLowerCase();
        
        // 搜索图片
        if (lower.contains("找") || lower.contains("搜") || lower.contains("查") ||
            lower.contains("有没有") || lower.contains("哪些") || lower.contains("哪张") ||
            lower.contains("search") || lower.contains("find") || lower.contains("show me")) {
            return Intent.SEARCH_IMAGES;
        }
        
        // 统计信息
        if (lower.contains("多少") || lower.contains("几张") || lower.contains("统计") ||
            lower.contains("总共") || lower.contains("count") || lower.contains("how many")) {
            return Intent.GET_STATS;
        }
        
        // 列出标签
        if (lower.contains("标签") || lower.contains("分类") || lower.contains("tag") ||
            lower.contains("列出") || lower.contains("list")) {
            return Intent.LIST_TAGS;
        }
        
        return Intent.GENERAL_CHAT;
    }

    private String extractSearchQuery(String message) {
        // 移除常见询问词
        String[] patterns = {
            "帮我", "请", "能不能", "可以", "麻烦", "给我",
            "找一张", "找一下", "找找", "找", "搜一下", "搜索", "搜", "查找", "查一下", "查",
            "看看有没有", "看看", "有没有", "有什么",
            "的图片", "的照片", "图片", "照片",
            "标签为", "标签是", "标签", "tag",
            "关于", "相关的", "类似的", "吗", "呢", "？", "?"
        };
        
        String result = message;
        for (String p : patterns) {
            result = result.replace(p, " ");
        }
        return result.trim().replaceAll("\\s+", " ");
    }

    // ==================== AI对话主接口 ====================

    /**
     * AI对话接口 - 使用MCP工具调用模式
     */
    public Map<String, Object> chat(String message, Long userId, List<Map<String, String>> history) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        try {
            // 1. 检测用户意图
            Intent intent = detectIntent(message);
            
            // 2. 根据意图调用MCP工具
            Map<String, Object> toolResult = null;
            String toolName = null;
            
            switch (intent) {
                case SEARCH_IMAGES:
                    String query = extractSearchQuery(message);
                    if (!query.isEmpty()) {
                        toolResult = toolSearchImages(query, userId);
                        toolName = "search_images";
                    }
                    break;
                case GET_STATS:
                    toolResult = toolGetStats(userId);
                    toolName = "get_stats";
                    break;
                case LIST_TAGS:
                    toolResult = toolListTags(userId);
                    toolName = "list_tags";
                    break;
                default:
                    break;
            }
            
            // 3. 构建AI请求，包含工具调用结果
            String aiResponse = callDeepSeekWithContext(message, toolName, toolResult, userId);
            response.put("message", aiResponse);
            
            // 4. 如果是搜索，附加图片结果供前端渲染
            if (intent == Intent.SEARCH_IMAGES && toolResult != null) {
                List<Map<String, Object>> images = (List<Map<String, Object>>) toolResult.get("images");
                if (images != null && !images.isEmpty()) {
                    response.put("hasSearch", true);
                    response.put("searchResult", toolResult);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "抱歉，我遇到了一些问题：" + e.getMessage());
        }
        
        return response;
    }

    /**
     * 调用DeepSeek，传入工具调用结果作为上下文
     */
    private String callDeepSeekWithContext(String userMessage, String toolName, 
                                            Map<String, Object> toolResult, Long userId) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("max_tokens", 512);
            
            ArrayNode messages = requestBody.putArray("messages");
            
            // 系统消息
            ObjectNode sysMsg = messages.addObject();
            sysMsg.put("role", "system");
            
            StringBuilder sysContent = new StringBuilder();
            sysContent.append("你是一个图片管理助手。请根据以下信息回答用户问题。\n\n");
            
            if (toolName != null && toolResult != null) {
                sysContent.append("【工具调用结果】\n");
                sysContent.append("调用工具: ").append(toolName).append("\n");
                sysContent.append("返回数据: ").append(objectMapper.writeValueAsString(toolResult)).append("\n\n");
            }
            
            sysContent.append("回复要求：简洁友好，如果搜索到图片告知用户数量，图片会自动显示。");
            sysMsg.put("content", sysContent.toString());
            
            // 用户消息
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            
            // 调用API
            ResponseEntity<String> resp = getRestTemplate().exchange(
                apiUrl,
                HttpMethod.POST,
                new HttpEntity<>(requestBody.toString(), createHeaders()),
                String.class
            );
            
            System.out.println("DeepSeek响应: " + resp.getBody());
            
            JsonNode respJson = objectMapper.readTree(resp.getBody());
            JsonNode choices = respJson.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText();
                if (content != null && !content.isEmpty()) {
                    return content;
                }
            }
            
            // API返回为空，使用本地回复
            return generateLocalResponse(toolName, toolResult);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DeepSeek API调用失败: " + e.getMessage());
            // API调用失败时，生成本地回复
            return generateLocalResponse(toolName, toolResult);
        }
    }

    /**
     * API失败时生成本地回复
     */
    private String generateLocalResponse(String toolName, Map<String, Object> toolResult) {
        if (toolName == null || toolResult == null) {
            return "您好，有什么可以帮您的？";
        }
        
        switch (toolName) {
            case "search_images":
                int count = (int) toolResult.get("count");
                String query = (String) toolResult.get("query");
                if (count > 0) {
                    return String.format("为您找到了 %d 张与\"%s\"相关的图片，已显示在下方。", count, query);
                } else {
                    return String.format("没有找到与\"%s\"相关的图片。您可以尝试其他关键词。", query);
                }
            case "get_stats":
                int total = (int) toolResult.get("totalImages");
                return String.format("您当前共有 %d 张图片。", total);
            case "list_tags":
                int tagCount = (int) toolResult.get("totalTags");
                return String.format("您共有 %d 个标签。", tagCount);
            default:
                return "操作完成。";
        }
    }

    // ==================== 图片分析接口 ====================

    public Map<String, Object> analyzeImage(Long imageId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("图片不存在"));
            
            if (!image.getUser().getId().equals(userId)) {
                throw new RuntimeException("无权访问此图片");
            }
            
            // 构建分析请求
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("max_tokens", 1024);
            
            ArrayNode messages = requestBody.putArray("messages");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            
            String prompt = String.format("""
                    我有一张图片，文件名是"%s"。
                    请根据文件名推测可能的内容，并按以下JSON格式返回结果（只返回JSON）：
                    {
                        "description": "图片的可能描述",
                        "tags": ["标签1", "标签2", "标签3"],
                        "category": "可能的分类",
                        "suggestedTags": ["建议标签1", "建议标签2"]
                    }
                    """, image.getOriginalFilename());
            
            userMessage.put("content", prompt);
            
            ResponseEntity<String> response = getRestTemplate().exchange(
                    apiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody.toString(), createHeaders()),
                    String.class
            );
            
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode choices = responseJson.path("choices");
            
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText();
                
                // 提取JSON部分
                int jsonStart = content.indexOf("{");
                int jsonEnd = content.lastIndexOf("}") + 1;
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    content = content.substring(jsonStart, jsonEnd);
                    Map<String, Object> aiResult = objectMapper.readValue(content, Map.class);
                    result.putAll(aiResult);
                }
            }
            
            result.put("success", true);
            result.put("imageId", imageId);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    public Map<String, Object> searchImagesByQuery(String query, Long userId) {
        return toolSearchImages(query, userId);
    }
}
