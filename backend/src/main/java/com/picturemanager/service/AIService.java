package com.picturemanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.picturemanager.entity.Image;
import com.picturemanager.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class AIService {

    @Value("${app.deepseek.api-key}")
    private String apiKey;

    @Value("${app.deepseek.api-url}")
    private String apiUrl;

    @Value("${app.deepseek.model}")
    private String model;

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

    /**
     * 分析图片并生成标签
     */
    public Map<String, Object> analyzeImage(Long imageId, Long userId) throws IOException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));

        if (!image.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权访问此图片");
        }

        // 读取图片并转换为Base64
        Path imagePath = Path.of(image.getFilePath());
        byte[] imageBytes = Files.readAllBytes(imagePath);
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = image.getMimeType();

        // 构建请求
        String prompt = """
                请分析这张图片，并按以下JSON格式返回结果（只返回JSON，不要其他内容）：
                {
                    "description": "图片的详细描述",
                    "tags": ["标签1", "标签2", "标签3"],
                    "category": "主分类（如：风景、人物、动物、建筑、美食、物品、艺术等）",
                    "objects": ["识别到的物体1", "识别到的物体2"],
                    "colors": ["主要颜色1", "主要颜色2"],
                    "mood": "图片氛围（如：欢快、宁静、忧郁等）",
                    "scene": "场景类型（如：室内、室外、城市、自然等）"
                }
                """;

        try {
            // 构建消息内容
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("max_tokens", 1024);

            ArrayNode messages = requestBody.putArray("messages");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");

            // DeepSeek Chat模型使用纯文本描述
            // 如果需要真正的图片分析，需要使用支持视觉的模型
            String textPrompt = String.format("""
                    我有一张图片，文件名是"%s"，格式是%s。
                    请根据文件名和常见图片类型，推测可能的标签和分类。
                    
                    请按以下JSON格式返回结果（只返回JSON，不要其他内容）：
                    {
                        "description": "基于文件名的可能描述",
                        "tags": ["可能的标签1", "可能的标签2", "可能的标签3"],
                        "category": "可能的分类",
                        "suggestedTags": ["建议添加的标签"]
                    }
                    """, image.getOriginalFilename(), mimeType);

            userMessage.put("content", textPrompt);

            ResponseEntity<String> response = getRestTemplate().exchange(
                    apiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody.toString(), createHeaders()),
                    String.class
            );

            // 解析响应
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson.path("choices").get(0).path("message").path("content").asText();

            // 提取JSON部分
            int jsonStart = content.indexOf("{");
            int jsonEnd = content.lastIndexOf("}") + 1;
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                content = content.substring(jsonStart, jsonEnd);
            }

            Map<String, Object> result = objectMapper.readValue(content, Map.class);
            result.put("imageId", imageId);
            result.put("success", true);

            return result;

        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            errorResult.put("imageId", imageId);
            return errorResult;
        }
    }

    /**
     * 通过自然语言搜索图片
     */
    public Map<String, Object> searchImagesByQuery(String query, Long userId) {
        try {
            // 获取用户所有图片的信息
            List<Image> userImages = imageRepository.findByUserIdAndIsDeletedFalse(userId);

            if (userImages.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("images", Collections.emptyList());
                result.put("message", "没有找到图片");
                return result;
            }

            // 构建图片信息摘要
            StringBuilder imagesSummary = new StringBuilder();
            for (Image img : userImages) {
                imagesSummary.append(String.format("ID:%d, 标题:%s, 描述:%s, 标签:%s\n",
                        img.getId(),
                        img.getTitle() != null ? img.getTitle() : "无",
                        img.getDescription() != null ? img.getDescription() : "无",
                        img.getTags() != null ? img.getTags().stream()
                                .map(t -> t.getName())
                                .reduce((a, b) -> a + "," + b)
                                .orElse("无") : "无"
                ));
            }

            // 构建请求
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("max_tokens", 1024);

            ArrayNode messages = requestBody.putArray("messages");
            
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个图片检索助手。用户会给你一个搜索查询和图片列表，你需要返回最匹配的图片ID列表。只返回JSON格式。");

            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", String.format("""
                    用户搜索：%s
                    
                    可用图片列表：
                    %s
                    
                    请返回最匹配的图片ID列表，格式如下（只返回JSON）：
                    {"matchedIds": [1, 2, 3], "reason": "匹配原因"}
                    """, query, imagesSummary.toString()));

            ResponseEntity<String> response = getRestTemplate().exchange(
                    apiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody.toString(), createHeaders()),
                    String.class
            );

            // 解析响应
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson.path("choices").get(0).path("message").path("content").asText();

            // 提取JSON部分
            int jsonStart = content.indexOf("{");
            int jsonEnd = content.lastIndexOf("}") + 1;
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                content = content.substring(jsonStart, jsonEnd);
            }

            Map<String, Object> aiResult = objectMapper.readValue(content, Map.class);
            List<Integer> matchedIds = (List<Integer>) aiResult.get("matchedIds");

            // 获取匹配的图片详情
            List<Map<String, Object>> matchedImages = new ArrayList<>();
            if (matchedIds != null) {
                for (Integer id : matchedIds) {
                    Optional<Image> img = imageRepository.findById(id.longValue());
                    if (img.isPresent() && img.get().getUser().getId().equals(userId)) {
                        Map<String, Object> imageInfo = new HashMap<>();
                        imageInfo.put("id", img.get().getId());
                        imageInfo.put("title", img.get().getTitle());
                        imageInfo.put("description", img.get().getDescription());
                        imageInfo.put("thumbnailPath", img.get().getThumbnailPath());
                        matchedImages.add(imageInfo);
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("query", query);
            result.put("images", matchedImages);
            result.put("reason", aiResult.get("reason"));
            result.put("totalMatched", matchedImages.size());

            return result;

        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    /**
     * AI对话接口
     */
    public Map<String, Object> chat(String message, Long userId, List<Map<String, String>> history) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("max_tokens", 2048);

            ArrayNode messages = requestBody.putArray("messages");

            // 系统消息
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", """
                    你是一个智能图片管理助手。你可以帮助用户：
                    1. 搜索和查找图片（使用 [SEARCH:关键词] 格式触发搜索）
                    2. 分析图片内容
                    3. 管理图片标签和分类
                    4. 回答关于图片管理的问题
                    
                    当用户想搜索图片时，请在回复中包含 [SEARCH:搜索关键词] 格式的标记。
                    """);

            // 历史消息
            if (history != null) {
                for (Map<String, String> msg : history) {
                    ObjectNode historyMsg = messages.addObject();
                    historyMsg.put("role", msg.get("role"));
                    historyMsg.put("content", msg.get("content"));
                }
            }

            // 用户消息
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);

            ResponseEntity<String> response = getRestTemplate().exchange(
                    apiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody.toString(), createHeaders()),
                    String.class
            );

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson.path("choices").get(0).path("message").path("content").asText();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", content);

            // 检查是否需要触发搜索
            if (content.contains("[SEARCH:")) {
                int start = content.indexOf("[SEARCH:") + 8;
                int end = content.indexOf("]", start);
                if (end > start) {
                    String searchQuery = content.substring(start, end);
                    Map<String, Object> searchResult = searchImagesByQuery(searchQuery, userId);
                    result.put("searchResult", searchResult);
                    result.put("hasSearch", true);
                }
            }

            return result;

        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }
}
