package com.chatpass.service;

import com.chatpass.dto.MessageDTO;
import com.chatpass.entity.Message;
import com.chatpass.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 话题管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {
    
    private final MessageRepository messageRepository;
    
    /**
     * 解析话题名称（自动提取）
     */
    public String parseTopic(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        
        // 简单的话题提取逻辑：查找第一个话题标记
        // Zulip 格式: #**topic name** 或直接使用 subject 字段
        
        // 查找 #**...** 格式
        int start = content.indexOf("#**");
        if (start >= 0) {
            int end = content.indexOf("**", start + 3);
            if (end > start) {
                return content.substring(start + 3, end);
            }
        }
        
        return null;
    }
    
    /**
     * 获取Stream的所有话题
     */
    public List<String> getStreamTopics(Long streamId) {
        List<Message> messages = messageRepository.findByRecipient_Stream_IdOrderByDateSentDesc(streamId);
        
        return messages.stream()
                .filter(m -> m.getSubject() != null && !m.getSubject().isEmpty())
                .map(Message::getSubject)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * 获取话题的消息数量
     */
    public Map<String, Long> getTopicStats(Long streamId) {
        List<Message> messages = messageRepository.findByRecipient_Stream_IdOrderByDateSentDesc(streamId);
        
        return messages.stream()
                .filter(m -> m.getSubject() != null && !m.getSubject().isEmpty())
                .collect(Collectors.groupingBy(
                    Message::getSubject,
                    Collectors.counting()
                ));
    }
    
    /**
     * 重命名话题
     */
    @Transactional
    public int renameTopic(Long streamId, String oldTopic, String newTopic) {
        List<Message> messages = messageRepository.findByRecipient_Stream_IdAndSubject(streamId, oldTopic);
        
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("话题不存在: " + oldTopic);
        }
        
        // 检查新话题是否已存在
        boolean newTopicExists = messageRepository.findByRecipient_Stream_IdAndSubject(streamId, newTopic)
                .stream()
                .findAny()
                .isPresent();
        
        if (newTopicExists) {
            throw new IllegalArgumentException("目标话题已存在: " + newTopic);
        }
        
        // 更新所有消息的话题
        messages.forEach(m -> m.setSubject(newTopic));
        messageRepository.saveAll(messages);
        
        log.info("重命名话题: {} -> {} (streamId: {}, count: {})", 
                 oldTopic, newTopic, streamId, messages.size());
        
        return messages.size();
    }
    
    /**
     * 合并话题
     */
    @Transactional
    public int mergeTopics(Long streamId, List<String> sourceTopics, String targetTopic) {
        int count = 0;
        
        for (String sourceTopic : sourceTopics) {
            List<Message> messages = messageRepository.findByRecipient_Stream_IdAndSubject(streamId, sourceTopic);
            
            if (!messages.isEmpty()) {
                messages.forEach(m -> m.setSubject(targetTopic));
                messageRepository.saveAll(messages);
                count += messages.size();
                
                log.info("合并话题: {} -> {} (count: {})", sourceTopic, targetTopic, messages.size());
            }
        }
        
        return count;
    }
    
    /**
     * 拆分话题（将指定消息移动到新话题）
     */
    @Transactional
    public int splitTopic(Long streamId, String sourceTopic, String newTopic, List<Long> messageIds) {
        List<Message> messages = messageRepository.findAllById(messageIds);
        
        // 验证消息属于该话题
        messages.forEach(m -> {
            if (!m.getRecipient().getStreamId().equals(streamId) ||
                !m.getSubject().equals(sourceTopic)) {
                throw new IllegalArgumentException("消息不属于该话题");
            }
        });
        
        // 移动到新话题
        messages.forEach(m -> m.setSubject(newTopic));
        messageRepository.saveAll(messages);
        
        log.info("拆分话题: {} -> {} (messageIds: {})", sourceTopic, newTopic, messageIds);
        
        return messages.size();
    }
    
    /**
     * 获取话题详情（最新消息）
     */
    public Optional<MessageDTO.Response> getTopicLatestMessage(Long streamId, String topic) {
        List<Message> messages = messageRepository.findByRecipient_Stream_IdAndSubjectOrderByDateSentDesc(streamId, topic);
        
        if (messages.isEmpty()) {
            return Optional.empty();
        }
        
        Message latest = messages.get(0);
        // 这里需要调用 MessageService 的转换方法
        // 暂时返回简化信息
        return Optional.of(MessageDTO.Response.builder()
                .id(latest.getId())
                .subject(latest.getSubject())
                .content(latest.getContent())
                .dateSent(latest.getDateSent())
                .build());
    }
    
    /**
     * 获取话题的参与者
     */
    public Set<Long> getTopicParticipants(Long streamId, String topic) {
        List<Message> messages = messageRepository.findByRecipient_Stream_IdAndSubject(streamId, topic);
        
        return messages.stream()
                .map(Message::getSender)
                .map(sender -> sender.getId())
                .collect(Collectors.toSet());
    }
    
    /**
     * 搜索话题
     */
    public List<String> searchTopics(Long streamId, String keyword) {
        List<String> topics = getStreamTopics(streamId);
        
        if (keyword == null || keyword.isEmpty()) {
            return topics;
        }
        
        return topics.stream()
                .filter(t -> t.toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
}
