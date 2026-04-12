package com.chatpass.service;

import com.chatpass.dto.MessageDraftDTO;
import com.chatpass.entity.MessageDraft;
import com.chatpass.repository.MessageDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 消息草稿服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageDraftService {
    
    private final MessageDraftRepository draftRepository;
    
    /**
     * 保存草稿
     */
    @Transactional
    public MessageDraftDTO saveDraft(Long userId, String type, Long streamId, 
                                       String toUserIds, String topic, String content) {
        
        MessageDraft draft = null;
        
        // 查找现有草稿
        if ("stream".equals(type) && streamId != null) {
            if (topic != null) {
                draft = draftRepository.findByUserIdAndStreamIdAndTopic(userId, streamId, topic)
                        .orElse(null);
            } else {
                draft = draftRepository.findByUserIdAndStreamId(userId, streamId)
                        .orElse(null);
            }
        } else if ("private".equals(type) && toUserIds != null) {
            draft = draftRepository.findByUserIdAndToUserIds(userId, toUserIds)
                    .orElse(null);
        }
        
        // 更新或创建草稿
        if (draft == null) {
            draft = MessageDraft.builder()
                    .userId(userId)
                    .type(type)
                    .streamId(streamId)
                    .toUserIds(toUserIds)
                    .topic(topic)
                    .build();
        }
        
        draft.setContent(content);
        draft = draftRepository.save(draft);
        
        log.info("保存草稿: userId={}, type={}", userId, type);
        
        return toDTO(draft);
    }
    
    /**
     * 获取用户所有草稿
     */
    public List<MessageDraftDTO> getUserDrafts(Long userId) {
        return draftRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取草稿详情
     */
    public Optional<MessageDraftDTO> getDraftById(Long draftId) {
        return draftRepository.findById(draftId).map(this::toDTO);
    }
    
    /**
     * 获取Stream草稿
     */
    public Optional<MessageDraftDTO> getStreamDraft(Long userId, Long streamId, String topic) {
        if (topic != null) {
            return draftRepository.findByUserIdAndStreamIdAndTopic(userId, streamId, topic)
                    .map(this::toDTO);
        } else {
            return draftRepository.findByUserIdAndStreamId(userId, streamId)
                    .map(this::toDTO);
        }
    }
    
    /**
     * 获取私信草稿
     */
    public Optional<MessageDraftDTO> getPrivateDraft(Long userId, String toUserIds) {
        return draftRepository.findByUserIdAndToUserIds(userId, toUserIds)
                .map(this::toDTO);
    }
    
    /**
     * 删除草稿
     */
    @Transactional
    public void deleteDraft(Long userId, Long draftId) {
        draftRepository.deleteByUserIdAndId(userId, draftId);
        log.info("删除草稿: userId={}, draftId={}", userId, draftId);
    }
    
    /**
     * 清空用户草稿
     */
    @Transactional
    public void clearUserDrafts(Long userId) {
        draftRepository.deleteAllByUserId(userId);
        log.info("清空草稿: userId={}", userId);
    }
    
    private MessageDraftDTO toDTO(MessageDraft draft) {
        return MessageDraftDTO.builder()
                .id(draft.getId())
                .userId(draft.getUserId())
                .streamId(draft.getStreamId())
                .toUserIds(draft.getToUserIds())
                .topic(draft.getTopic())
                .content(draft.getContent())
                .type(draft.getType())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }
}
