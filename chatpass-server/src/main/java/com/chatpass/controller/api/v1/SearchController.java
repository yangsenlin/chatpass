package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.SearchDTO;
import com.chatpass.entity.Message;
import com.chatpass.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Search 控制器
 * 
 * 搜索增强 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Search", description = "消息搜索 API")
public class SearchController {

    private final SearchService searchService;
    private final com.chatpass.security.SecurityUtil securityUtil;
    private final com.chatpass.repository.StreamRepository streamRepository;

    @GetMapping("/search")
    @Operation(summary = "全局搜索消息")
    public ResponseEntity<ApiResponse<SearchDTO.SearchResponse>> searchMessages(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        List<Message> messages = searchService.searchMessages(realmId, query, limit);
        
        List<SearchDTO.MessageResult> results = messages.stream()
                .map(this::toMessageResult)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(SearchDTO.SearchResponse.builder()
                .query(query)
                .results(results)
                .count(results.size())
                .build()));
    }

    @GetMapping("/search/paged")
    @Operation(summary = "搜索消息（分页）")
    public ResponseEntity<ApiResponse<Page<Message>>> searchMessagesPaged(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        Page<Message> messages = searchService.searchMessagesPaged(realmId, query, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/search/suggestions")
    @Operation(summary = "获取搜索建议")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @RequestParam String query) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        List<String> suggestions = searchService.getSearchSuggestions(realmId, query);
        
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    @GetMapping("/search/stats")
    @Operation(summary = "搜索结果统计")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSearchStats(
            @RequestParam String query) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        Map<String, Object> stats = searchService.getSearchStats(realmId, query);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/streams/{streamId}/search")
    @Operation(summary = "搜索频道消息")
    public ResponseEntity<ApiResponse<SearchDTO.SearchResponse>> searchStreamMessages(
            @PathVariable Long streamId,
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit) {
        List<Message> messages = searchService.searchStreamMessages(streamId, query, limit);
        
        List<SearchDTO.MessageResult> results = messages.stream()
                .map(this::toMessageResult)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(SearchDTO.SearchResponse.builder()
                .query(query)
                .streamId(streamId)
                .results(results)
                .count(results.size())
                .build()));
    }

    @GetMapping("/streams/{streamId}/topics/{topic}/search")
    @Operation(summary = "搜索话题消息")
    public ResponseEntity<ApiResponse<SearchDTO.SearchResponse>> searchTopicMessages(
            @PathVariable Long streamId,
            @PathVariable String topic,
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit) {
        List<Message> messages = searchService.searchTopicMessages(streamId, topic, query, limit);
        
        List<SearchDTO.MessageResult> results = messages.stream()
                .map(this::toMessageResult)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(SearchDTO.SearchResponse.builder()
                .query(query)
                .streamId(streamId)
                .topic(topic)
                .results(results)
                .count(results.size())
                .build()));
    }

    @GetMapping("/users/{userId}/search")
    @Operation(summary = "搜索用户消息")
    public ResponseEntity<ApiResponse<SearchDTO.SearchResponse>> searchUserMessages(
            @PathVariable Long userId,
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit) {
        List<Message> messages = searchService.searchUserMessages(userId, query, limit);
        
        List<SearchDTO.MessageResult> results = messages.stream()
                .map(this::toMessageResult)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(SearchDTO.SearchResponse.builder()
                .query(query)
                .senderId(userId)
                .results(results)
                .count(results.size())
                .build()));
    }

    @PostMapping("/search/advanced")
    @Operation(summary = "高级搜索（多条件）")
    public ResponseEntity<ApiResponse<SearchDTO.SearchResponse>> advancedSearch(
            @RequestBody SearchDTO.AdvancedRequest request) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        LocalDateTime dateFrom = request.getDateFrom() != null 
                ? LocalDateTime.parse(request.getDateFrom()) : null;
        LocalDateTime dateTo = request.getDateTo() != null 
                ? LocalDateTime.parse(request.getDateTo()) : null;
        
        List<Message> messages = searchService.advancedSearch(
                realmId,
                request.getQuery(),
                request.getStreamId(),
                request.getSenderId(),
                request.getTopic(),
                dateFrom,
                dateTo,
                request.getLimit() != null ? request.getLimit() : 20
        );
        
        List<SearchDTO.MessageResult> results = messages.stream()
                .map(this::toMessageResult)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(SearchDTO.SearchResponse.builder()
                .query(request.getQuery())
                .results(results)
                .count(results.size())
                .build()));
    }

    private SearchDTO.MessageResult toMessageResult(Message message) {
        Long streamId = message.getRecipient().getStreamId();
        String streamName = null;
        
        // 尝试获取频道名称（如果有频道信息）
        if (streamId != null) {
            streamRepository.findById(streamId).ifPresent(stream -> {
                // 使用 StringBuilder 来在 lambda 中设置值
            });
            // 直接查询获取名称
            streamName = streamRepository.findById(streamId)
                    .map(s -> s.getName())
                    .orElse(null);
        }
        
        return SearchDTO.MessageResult.builder()
                .messageId(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .streamId(streamId)
                .streamName(streamName)
                .topic(message.getSubject())
                .content(message.getContent())
                .timestamp(message.getDateSent() != null ? message.getDateSent().toString() : "")
                .build();
    }
}