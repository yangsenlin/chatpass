package com.chatpass.service;

import com.chatpass.entity.Message;
import com.chatpass.entity.ReadReceipt;
import com.chatpass.entity.Recipient;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.MessageRepository;
import com.chatpass.repository.ReadReceiptRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ReadReceiptService
 * 
 * 消息阅读回执服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReadReceiptService {

    private final ReadReceiptRepository readReceiptRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 标记消息已读
     */
    @Transactional
    public ReadReceipt markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在"));
        
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // 检查是否已标记
        Optional<ReadReceipt> existing = readReceiptRepository.findByMessageAndUser(message, user);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // 创建阅读回执
        ReadReceipt receipt = ReadReceipt.builder()
                .message(message)
                .user(user)
                .readAt(LocalDateTime.now())
                .build();
        
        readReceiptRepository.save(receipt);
        
        // 推送阅读通知给发送者
        notifySender(message, user, receipt.getReadAt());
        
        log.info("User {} read message {}", userId, messageId);
        
        return receipt;
    }

    /**
     * 批量标记消息已读
     */
    @Transactional
    public List<ReadReceipt> markBatchAsRead(List<Long> messageIds, Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        List<ReadReceipt> receipts = messageIds.stream()
                .map(messageId -> {
                    Message message = messageRepository.findById(messageId).orElse(null);
                    if (message == null) return null;
                    
                    Optional<ReadReceipt> existing = readReceiptRepository.findByMessageAndUser(message, user);
                    if (existing.isPresent()) return existing.get();
                    
                    return ReadReceipt.builder()
                            .message(message)
                            .user(user)
                            .readAt(LocalDateTime.now())
                            .build();
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
        
        readReceiptRepository.saveAll(receipts);
        
        log.info("User {} marked {} messages as read", userId, receipts.size());
        
        return receipts;
    }

    /**
     * 获取消息的阅读状态
     */
    public List<ReadReceipt> getMessageReadStatus(Long messageId) {
        return readReceiptRepository.findByMessageId(messageId);
    }

    /**
     * 获取用户已读的消息列表
     */
    public List<ReadReceipt> getUserReadHistory(Long userId) {
        return readReceiptRepository.findByUserId(userId);
    }

    /**
     * 获取消息的阅读人数
     */
    public Long getReadCount(Long messageId) {
        return readReceiptRepository.countByMessageId(messageId);
    }

    /**
     * 获取对话中最后已读时间
     */
    public LocalDateTime getLastReadTime(Long userId, Long recipientId) {
        return readReceiptRepository.findLastReadTime(userId, recipientId);
    }

    /**
     * 检查消息是否被特定用户已读
     */
    public boolean isReadByUser(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        UserProfile user = userRepository.findById(userId).orElse(null);
        
        if (message == null || user == null) return false;
        
        return readReceiptRepository.existsByMessageAndUser(message, user);
    }

    /**
     * 推送阅读通知给发送者
     */
    private void notifySender(Message message, UserProfile reader, LocalDateTime readAt) {
        UserProfile sender = message.getSender();
        
        // WebSocket 推送
        Map<String, Object> notification = Map.of(
                "type", "read_receipt",
                "message_id", message.getId(),
                "reader_id", reader.getId(),
                "reader_name", reader.getFullName(),
                "read_at", readAt.toString()
        );
        
        messagingTemplate.convertAndSendToUser(
                sender.getEmail(),
                "/queue/notifications",
                notification
        );
        
        log.debug("Sent read receipt notification to user {}", sender.getId());
    }
}