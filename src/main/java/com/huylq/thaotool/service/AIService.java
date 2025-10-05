package com.huylq.thaotool.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

  private static final String URL = "http://localhost:1234/v1/chat/completions";
  private final RestTemplate restTemplate = new RestTemplate();
  private final String FIXED_PROMPT = "Viết lại đoạn văn này hay và dài hơn phài đảm bảo nội dung tốt như đoạn văn gốc, " +
      "lưu ý không chia các đoạn quá nhỏ mỗi đoạn tầm 5 đến 6 dòng, phân cách mỗi đoạn bạn thêm tag </br> cho tôi. " +
      "Chỉ trả kết quả dạng text và không giải thích gì thêm.";

  public String rewriteText(String userContent) {
    Map<String, Object> systemMsg = Map.of(
        "role", "system",
        "content", FIXED_PROMPT
    );

    Map<String, Object> userMsg = Map.of(
        "role", "user",
        "content", userContent
    );

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", "qwen/qwen3-8b");
    requestBody.put("messages", List.of(systemMsg, userMsg));
    requestBody.put("temperature", 0.7);
    requestBody.put("max_tokens", -1);
    requestBody.put("stream", false);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response = restTemplate.exchange(
        URL,
        HttpMethod.POST,
        entity,
        String.class
    );

    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response.getBody());
      return root.path("choices").get(0).path("message").path("content").asText();
    } catch (Exception e) {
      throw new RuntimeException("ERROR when parse JSON from AI response", e);
    }
  }
}
