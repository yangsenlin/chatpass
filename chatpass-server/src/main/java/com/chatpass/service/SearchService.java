package com.chatpass.service;

import com.chatpass.entity.*;
import com.chatpass.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SearchService - 搜索增强服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final MessageRepository messageRepository;
    private final StreamRepository streamRepository;
    private final UserProfileRepository userRepository;

    /**
     * 全文搜索（简化版）
     */
    @Transactional(readOnly = true)
    public List<Message> searchMessages(Long realmId, String query) {
        List<Message> messages = messageRepository.findByRealmIdOrderByDateSentDesc(realmId, PageRequest.of(0, 1000)).getContent();
        
        return messages.stream()
                .filter(m -> matchesQuery(m, query))
                .collect(Collectors.toList());
    }

    /**
     * 在频道内搜索
     */
    @Transactional(readOnly = true)
    public List<Message> searchInStream(Long streamId, String query) {
        List<Message> messages = messageRepository.findAll();
        
        return messages.stream()
                .filter(m -> m.getRecipient() != null && m.getRecipient().getStreamId() != null && m.getRecipient().getStreamId().equals(streamId))
                .filter(m -> matchesQuery(m, query))
                .limit(100)
                .collect(Collectors.toList());
    }

    /**
     * 搜索用户消息
     */
    @Transactional(readOnly = true)
    public List<Message> searchByUser(Long userId, String query) {
        List<Message> messages = messageRepository.findBySenderIdOrderByDateSentDesc(userId);
        
        return messages.stream()
                .filter(m -> matchesQuery(m, query))
                .limit(100)
                .collect(Collectors.toList());
    }

    /**
     * 搜索话题
     */
    @Transactional(readOnly = true)
    public List<Message> searchByTopic(Long streamId, String topic) {
        List<Message> messages = messageRepository.findAll();
        
        return messages.stream()
                .filter(m -> m.getRecipient() != null && m.getRecipient().getStreamId() != null && m.getRecipient().getStreamId().equals(streamId))
                .filter(m -> m.getSubject() != null && m.getSubject().contains(topic))
                .limit(100)
                .collect(Collectors.toList());
    }

    /**
     * 搜索统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSearchStats(Long realmId, String query) {
        List<Message> results = searchMessages(realmId, query);
        
        // 统计频道分布
        Map<Long, Integer> streamDistribution = results.stream()
                .filter(m -> m.getRecipient() != null && m.getRecipient().getStreamId() != null)
                .collect(Collectors.groupingBy(
                        m -> m.getRecipient().getStreamId(),
                        Collectors.summingInt(e -> 1)
                ));
        
        // 统计发送者分布
        Map<Long, Integer> senderDistribution = results.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getSender().getId(),
                        Collectors.summingInt(e -> 1)
                ));
        
        return Map.of(
                "query", query,
                "total_results", results.size(),
                "stream_distribution", streamDistribution,
                "sender_distribution", senderDistribution
        );
    }

    /**
     * 检查消息是否匹配查询
     */
    private boolean matchesQuery(Message message, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        
        String lowerQuery = query.toLowerCase();
        
        // 搜索消息内容
        if (message.getContent() != null && 
            message.getContent().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        // 搜索话题
        if (message.getSubject() != null && 
            message.getSubject().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        return false;
    }
}