package com.chatpass.service;

import com.chatpass.entity.MessageQueue;
import com.chatpass.entity.QueuedMessage;
import com.chatpass.repository.MessageQueueRepository;
import com.chatpass.repository.QueuedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MessageQueueService
 * 消息队列服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageQueueService {

    private final MessageQueueRepository queueRepository;
    private final QueuedMessageRepository queuedMessageRepository;

    /**
     * 创建队列配置
     */
    @Transactional
    public MessageQueue createQueue(Long realmId, String queueName, String queueType,
                                     String brokerUrl, String exchangeName, String routingKey) {
        MessageQueue queue = MessageQueue.builder()
                .realmId(realmId)
                .queueName(queueName)
                .queueType(queueType)
                .brokerUrl(brokerUrl)
                .exchangeName(exchangeName)
                .routingKey(routingKey)
                .isActive(true)
                .dateCreated(LocalDateTime.now())
                .build();

        return queueRepository.save(queue);
    }

    /**
     * 获取队列配置
     */
    public Optional<MessageQueue> getQueue(Long queueId) {
        return queueRepository.findById(queueId);
    }

    /**
     * 获取队列配置（按名称）
     */
    public Optional<MessageQueue> getQueueByName(String queueName) {
        return queueRepository.findByQueueName(queueName);
    }

    /**
     * 获取 Realm 所有队列
     */
    public List<MessageQueue> getRealmQueues(Long realmId) {
        return queueRepository.findByRealmId(realmId);
    }

    /**
     * 获取活跃队列
     */
    public List<MessageQueue> getActiveQueues(Long realmId) {
        return queueRepository.findByRealmIdAndIsActiveTrue(realmId);
    }

    /**
     * 更新队列状态
     */
    @Transactional
    public MessageQueue updateQueueStatus(Long queueId, Boolean isActive) {
        MessageQueue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new IllegalArgumentException("队列不存在"));

        queue.setIsActive(isActive);
        queue.setLastUpdated(LocalDateTime.now());

        return queueRepository.save(queue);
    }

    /**
     * 删除队列
     */
    @Transactional
    public void deleteQueue(Long queueId) {
        queueRepository.deleteById(queueId);
    }

    /**
     * 添加消息到队列
     */
    @Transactional
    public QueuedMessage enqueue(Long queueId, Long messageId, String payload) {
        QueuedMessage queuedMessage = QueuedMessage.builder()
                .queueId(queueId)
                .messageId(messageId)
                .payload(payload)
                .status(QueuedMessage.STATUS_PENDING)
                .retryCount(0)
                .dateQueued(LocalDateTime.now())
                .build();

        return queuedMessageRepository.save(queuedMessage);
    }

    /**
     * 获取待处理消息
     */
    public List<QueuedMessage> getPendingMessages(Integer maxRetry) {
        return queuedMessageRepository.findPendingMessages(maxRetry);
    }

    /**
     * 标记消息已发送
     */
    @Transactional
    public void markSent(Long queuedMessageId) {
        queuedMessageRepository.updateStatus(queuedMessageId, QueuedMessage.STATUS_SENT, LocalDateTime.now());
    }

    /**
     * 标记消息失败
     */
    @Transactional
    public void markFailed(Long queuedMessageId, String error) {
        queuedMessageRepository.markFailed(queuedMessageId, error);
    }

    /**
     * 重试消息
     */
    @Transactional
    public void retry(Long queuedMessageId) {
        queuedMessageRepository.incrementRetry(queuedMessageId, LocalDateTime.now());
    }

    /**
     * 统计队列状态
     */
    public Long countQueueMessages(Long queueId, String status) {
        return queuedMessageRepository.countByQueueIdAndStatus(queueId, status);
    }

    /**
     * 发送队列消息（模拟）
     */
    @Transactional
    public boolean sendMessage(Long queuedMessageId) {
        QueuedMessage qm = queuedMessageRepository.findById(queuedMessageId)
                .orElseThrow(() -> new IllegalArgumentException("队列消息不存在"));

        MessageQueue queue = queueRepository.findById(qm.getQueueId())
                .orElseThrow(() -> new IllegalArgumentException("队列配置不存在"));

        try {
            // 模拟发送到消息队列
            log.info("Sending message {} to queue {} (type: {})", qm.getMessageId(), queue.getQueueName(), queue.getQueueType());

            // 实际实现需要根据 queueType 调用不同的消息队列客户端
            // RabbitMQ: 使用 RabbitTemplate
            // Kafka: 使用 KafkaTemplate
            // SQS: 使用 AmazonSQS

            markSent(queuedMessageId);
            return true;
        } catch (Exception e) {
            log.error("Failed to send message to queue: {}", e.getMessage());

            if (qm.getRetryCount() < queue.getMaxRetry()) {
                retry(queuedMessageId);
            } else {
                markFailed(queuedMessageId, e.getMessage());
            }

            return false;
        }
    }
}