package com.chatpass.service;

import com.chatpass.dto.MessageEditDTO;
import com.chatpass.entity.Message;
import com.chatpass.entity.MessageEditHistory;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.MessageEditHistoryRepository;
import com.chatpass.repository.MessageRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MessageEditHistoryService
 * 
 * 消息编辑历史管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageEditHistoryService {

    private final MessageEditHistoryRepository editHistoryRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userRepository;

    /**
     * 记录消息编辑
     */
    @Transactional
    public MessageEditHistory recordEdit(Long messageId, Long editorId, 
            String prevContent, String prevTopic, String newContent, String newTopic) {
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在"));

        UserProfile editor = userRepository.findById(editorId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 判断编辑类型
        int editType = determineEditType(prevContent, newContent, prevTopic, newTopic);

        MessageEditHistory history = MessageEditHistory.builder()
                .message(message)
                .editor(editor)
                .prevContent(prevContent)
                .prevTopic(prevTopic)
                .newContent(newContent)
                .newTopic(newTopic)
                .editType(editType)
                .build();

        history = editHistoryRepository.save(history);

        // 更新消息的编辑时间
        message.setLastEditTime(history.getEditTime());
        messageRepository.save(message);

        log.info("Recorded message edit: messageId={}, editorId={}, type={}", 
                messageId, editorId, editType);

        return history;
    }

    /**
     * 确定编辑类型
     */
    private int determineEditType(String prevContent, String newContent, 
            String prevTopic, String newTopic) {
        boolean contentChanged = !equalsOrBothNull(prevContent, newContent);
        boolean topicChanged = !equalsOrBothNull(prevTopic, newTopic);

        if (contentChanged && topicChanged) {
            return MessageEditHistory.EDIT_TYPE_BOTH;
        } else if (contentChanged) {
            return MessageEditHistory.EDIT_TYPE_CONTENT;
        } else if (topicChanged) {
            return MessageEditHistory.EDIT_TYPE_TOPIC;
        }
        return MessageEditHistory.EDIT_TYPE_CONTENT; // 默认
    }

    /**
     * 比较两个字符串（允许 null）
     */
    private boolean equalsOrBothNull(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * 获取消息的编辑历史
     */
    public List<MessageEditHistory> getEditHistory(Long messageId) {
        return editHistoryRepository.findByMessageId(messageId);
    }

    /**
     * 获取消息的最近编辑
     */
    public Optional<MessageEditHistory> getLatestEdit(Long messageId) {
        MessageEditHistory latest = editHistoryRepository.findLatestByMessageId(messageId);
        return Optional.ofNullable(latest);
    }

    /**
     * 获取用户编辑的消息列表
     */
    public List<MessageEditHistory> getUserEdits(Long editorId) {
        return editHistoryRepository.findByEditorId(editorId);
    }

    /**
     * 统计消息的编辑次数
     */
    public Long getEditCount(Long messageId) {
        return editHistoryRepository.countByMessageId(messageId);
    }

    /**
     * 获取内容编辑历史
     */
    public List<MessageEditHistory> getContentEdits(Long messageId) {
        return editHistoryRepository.findContentEditsByMessageId(messageId);
    }

    /**
     * 获取 Topic 编辑历史
     */
    public List<MessageEditHistory> getTopicEdits(Long messageId) {
        return editHistoryRepository.findTopicEditsByMessageId(messageId);
    }

    /**
     * 恢复到历史版本
     */
    @Transactional
    public Message restoreToVersion(Long messageId, Long historyId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在"));

        MessageEditHistory history = editHistoryRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("编辑历史不存在"));

        if (!history.getMessage().getId().equals(messageId)) {
            throw new IllegalArgumentException("编辑历史不属于该消息");
        }

        // 恢复内容
        if (history.getPrevContent() != null) {
            message.setContent(history.getPrevContent());
        }

        // 恢复 Topic
        if (history.getPrevTopic() != null) {
            message.setSubject(history.getPrevTopic());
        }

        message = messageRepository.save(message);

        log.info("Restored message {} to version {}", messageId, historyId);

        return message;
    }

    /**
     * 获取编辑历史详情
     */
    public MessageEditDTO.EditHistoryResponse toResponse(MessageEditHistory history) {
        return MessageEditDTO.EditHistoryResponse.builder()
                .id(history.getId())
                .messageId(history.getMessage().getId())
                .editorId(history.getEditor().getId())
                .editorName(history.getEditor().getFullName())
                .prevContent(history.getPrevContent())
                .prevTopic(history.getPrevTopic())
                .newContent(history.getNewContent())
                .newTopic(history.getNewTopic())
                .editTime(history.getEditTime().toString())
                .editType(history.getEditType())
                .isContentEdit(history.isContentEdit())
                .isTopicEdit(history.isTopicEdit())
                .build();
    }

    /**
     * 获取消息编辑摘要
     */
    public MessageEditDTO.EditSummary getEditSummary(Long messageId) {
        Long count = getEditCount(messageId);
        Optional<MessageEditHistory> latest = getLatestEdit(messageId);

        return MessageEditDTO.EditSummary.builder()
                .messageId(messageId)
                .editCount(count)
                .lastEditTime(latest.map(h -> h.getEditTime().toString()).orElse(null))
                .lastEditorName(latest.map(h -> h.getEditor().getFullName()).orElse(null))
                .build();
    }
}