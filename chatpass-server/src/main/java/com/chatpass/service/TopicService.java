package com.chatpass.service;

import com.chatpass.entity.Message;
import com.chatpass.entity.Recipient;
import com.chatpass.entity.Stream;
import com.chatpass.repository.MessageRepository;
import com.chatpass.repository.RecipientRepository;
import com.chatpass.repository.StreamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TopicService
 * 
 * 话题管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {

    private final MessageRepository messageRepository;
    private final RecipientRepository recipientRepository;
    private final StreamRepository streamRepository;

    /**
     * 重命名话题
     * 
     * 将 Stream 中某话题的所有消息改为新话题名
     */
    @Transactional
    public int renameTopic(Long streamId, String oldTopic, String newTopic) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        if (oldTopic == null || oldTopic.trim().isEmpty()) {
            throw new IllegalArgumentException("原话题名称不能为空");
        }
        
        if (newTopic == null || newTopic.trim().isEmpty()) {
            throw new IllegalArgumentException("新话题名称不能为空");
        }
        
        // 获取该 Stream + Topic 的所有消息
        List<Message> messages = messageRepository.findByStreamIdAndTopic(streamId, oldTopic);
        
        if (messages.isEmpty()) {
            log.warn("No messages found for topic {} in stream {}", oldTopic, streamId);
            return 0;
        }
        
        // 更新所有消息的 subject
        int count = 0;
        for (Message message : messages) {
            message.setSubject(newTopic);
            messageRepository.save(message);
            count++;
        }
        
        log.info("Renamed topic '{}' to '{}' in stream {}, updated {} messages", 
                oldTopic, newTopic, streamId, count);
        
        return count;
    }

    /**
     * 合并话题
     * 
     * 将源话题的所有消息合并到目标话题
     */
    @Transactional
    public int mergeTopic(Long streamId, String sourceTopic, String targetTopic) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        if (sourceTopic.equals(targetTopic)) {
            throw new IllegalArgumentException("源话题和目标话题不能相同");
        }
        
        // 获取源话题的所有消息
        List<Message> messages = messageRepository.findByStreamIdAndTopic(streamId, sourceTopic);
        
        if (messages.isEmpty()) {
            log.warn("No messages found for source topic {}", sourceTopic);
            return 0;
        }
        
        // 更新所有消息到目标话题
        int count = 0;
        for (Message message : messages) {
            message.setSubject(targetTopic);
            messageRepository.save(message);
            count++;
        }
        
        log.info("Merged topic '{}' to '{}' in stream {}, moved {} messages", 
                sourceTopic, targetTopic, streamId, count);
        
        return count;
    }

    /**
     * 获取频道的话题列表
     */
    public List<String> getStreamTopics(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        List<Message> messages = messageRepository.findByStreamId(streamId);
        
        return messages.stream()
                .map(Message::getSubject)
                .filter(t -> t != null && !t.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取频道的话题统计
     */
    public Map<String, Long> getStreamTopicStats(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        List<Message> messages = messageRepository.findByStreamId(streamId);
        
        return messages.stream()
                .filter(m -> m.getSubject() != null && !m.getSubject().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        Message::getSubject,
                        Collectors.counting()
                ));
    }

    /**
     * 获取热门话题（消息数最多）
     */
    public List<Map<String, Object>> getHotTopics(Long streamId, int limit) {
        Map<String, Long> stats = getStreamTopicStats(streamId);
        
        return stats.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(e -> Map.<String, Object>of(
                        "topic", e.getKey(),
                        "message_count", e.getValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 搜索话题（模糊匹配）
     */
    public List<String> searchTopics(Long streamId, String query) {
        List<String> topics = getStreamTopics(streamId);
        
        if (query == null || query.trim().isEmpty()) {
            return topics;
        }
        
        String lowerQuery = query.toLowerCase();
        
        return topics.stream()
                .filter(t -> t.toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * 获取话题的最新消息时间
     */
    public java.time.LocalDateTime getTopicLastMessageTime(Long streamId, String topic) {
        List<Message> messages = messageRepository.findByStreamIdAndTopic(streamId, topic);
        
        return messages.stream()
                .map(Message::getDateSent)
                .max(java.time.LocalDateTime::compareTo)
                .orElse(null);
    }

    /**
     * 获取话题的消息总数
     */
    public Long getTopicMessageCount(Long streamId, String topic) {
        List<Message> messages = messageRepository.findByStreamIdAndTopic(streamId, topic);
        
        return (long) messages.size();
    }
}