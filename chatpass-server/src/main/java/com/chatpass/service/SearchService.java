package com.chatpass.service;

import com.chatpass.entity.Message;
import com.chatpass.entity.Realm;
import com.chatpass.entity.Stream;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.MessageRepository;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.StreamRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * SearchService
 * 
 * 消息搜索服务（增强版）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final MessageRepository messageRepository;
    private final RealmRepository realmRepository;
    private final StreamRepository streamRepository;
    private final UserProfileRepository userRepository;

    /**
     * 全局搜索消息（内容匹配）
     */
    public List<Message> searchMessages(Long realmId, String query, int limit) {
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm 不存在"));
        
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Message> messages = messageRepository.searchByContent(realmId, query);
        
        return messages.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 搜索消息（带分页）
     */
    public Page<Message> searchMessagesPaged(Long realmId, String query, int page, int size) {
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm 不存在"));
        
        if (query == null || query.trim().isEmpty()) {
            return Page.empty();
        }
        
        return messageRepository.searchByContentPaged(realmId, query, PageRequest.of(page, size));
    }

    /**
     * 搜索特定频道的消息
     */
    public List<Message> searchStreamMessages(Long streamId, String query, int limit) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        List<Message> messages = messageRepository.searchByStreamIdAndContent(streamId, query);
        
        return messages.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 搜索特定话题的消息
     */
    public List<Message> searchTopicMessages(Long streamId, String topic, String query, int limit) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        List<Message> messages = messageRepository.searchByStreamIdTopicAndContent(streamId, topic, query);
        
        return messages.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 搜索用户发送的消息
     */
    public List<Message> searchUserMessages(Long userId, String query, int limit) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        List<Message> messages = messageRepository.searchBySenderIdAndContent(userId, query);
        
        return messages.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 搜索建议（返回关键词提示）
     */
    public List<String> getSearchSuggestions(Long realmId, String query) {
        // 从消息中提取关键词建议
        List<Message> messages = searchMessages(realmId, query, 20);
        
        Set<String> suggestions = new HashSet<>();
        for (Message msg : messages) {
            // 提取消息内容中的关键词
            String content = msg.getContent();
            if (content != null) {
                // 简单关键词提取（按空格分割）
                Arrays.stream(content.toLowerCase().split("\\s+"))
                        .filter(word -> word.length() > 2)
                        .filter(word -> word.contains(query.toLowerCase()))
                        .forEach(suggestions::add);
            }
        }
        
        return suggestions.stream()
                .sorted()
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 搜索历史记录（返回用户最近搜索过的关键词）
     */
    public Map<String, Object> getSearchHistory(Long userId, int limit) {
        // TODO: 实现搜索历史存储
        // 目前返回空列表
        return Map.of(
                "user_id", userId,
                "recent_searches", Collections.emptyList(),
                "message", "搜索历史功能待实现"
        );
    }

    /**
     * 高级搜索（多条件组合）
     */
    public List<Message> advancedSearch(
            Long realmId,
            String query,
            Long streamId,
            Long senderId,
            String topic,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            int limit) {
        
        List<Message> baseMessages = searchMessages(realmId, query, 1000);
        
        // 过滤条件
        List<Message> filtered = baseMessages.stream()
                .filter(m -> streamId == null || m.getRecipient().getStreamId() != null 
                        && m.getRecipient().getStreamId().equals(streamId))
                .filter(m -> senderId == null || m.getSender().getId().equals(senderId))
                .filter(m -> topic == null || topic.isEmpty() 
                        || (m.getSubject() != null && m.getSubject().equals(topic)))
                .filter(m -> dateFrom == null || m.getDateSent().isAfter(dateFrom))
                .filter(m -> dateTo == null || m.getDateSent().isBefore(dateTo))
                .limit(limit)
                .collect(Collectors.toList());
        
        return filtered;
    }

    /**
     * 搜索结果统计
     */
    public Map<String, Object> getSearchStats(Long realmId, String query) {
        List<Message> messages = searchMessages(realmId, query, 1000);
        
        // 按频道分组统计
        Map<String, Long> byStream = messages.stream()
                .filter(m -> m.getRecipient().getStreamId() != null)
                .collect(Collectors.groupingBy(
                        m -> String.valueOf(m.getRecipient().getStreamId()),
                        Collectors.counting()
                ));
        
        // 按话题分组统计
        Map<String, Long> byTopic = messages.stream()
                .filter(m -> m.getSubject() != null && !m.getSubject().isEmpty())
                .collect(Collectors.groupingBy(
                        Message::getSubject,
                        Collectors.counting()
                ));
        
        // 按发送者分组统计
        Map<String, Long> bySender = messages.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getSender().getFullName(),
                        Collectors.counting()
                ));
        
        return Map.of(
                "total_results", messages.size(),
                "by_stream", byStream,
                "by_topic", byTopic,
                "by_sender", bySender
        );
    }
}