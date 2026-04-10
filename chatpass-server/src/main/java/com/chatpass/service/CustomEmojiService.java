package com.chatpass.service;

import com.chatpass.dto.CustomEmojiDTO;
import com.chatpass.entity.CustomEmoji;
import com.chatpass.entity.Realm;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.CustomEmojiRepository;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CustomEmojiService
 * 
 * 自定义表情管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomEmojiService {

    private final CustomEmojiRepository emojiRepository;
    private final RealmRepository realmRepository;
    private final UserProfileRepository userRepository;

    /**
     * 获取 Realm 的所有表情
     */
    public List<CustomEmoji> getAllEmojis(Long realmId) {
        return emojiRepository.findByRealmIdAndActive(realmId);
    }

    /**
     * 获取表情详情
     */
    public Optional<CustomEmoji> getEmoji(Long emojiId) {
        return emojiRepository.findById(emojiId)
                .filter(CustomEmoji::isActive);
    }

    /**
     * 根据名称获取表情
     */
    public Optional<CustomEmoji> getEmojiByName(Long realmId, String name) {
        return emojiRepository.findByRealmIdAndName(realmId, name);
    }

    /**
     * 创建自定义表情
     */
    @Transactional
    public CustomEmoji createEmoji(Long realmId, Long authorId, CustomEmojiDTO.CreateRequest request) {
        // 检查名称是否已存在
        if (emojiRepository.existsByRealmIdAndName(realmId, request.getName())) {
            throw new IllegalArgumentException("表情名称已存在: " + request.getName());
        }

        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm 不存在"));

        UserProfile author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        CustomEmoji emoji = CustomEmoji.builder()
                .realm(realm)
                .name(request.getName())
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getName())
                .imageUrl(request.getImageUrl())
                .author(author)
                .deactivated(false)
                .build();

        emoji = emojiRepository.save(emoji);

        log.info("Created custom emoji: {} in realm {}", emoji.getName(), realmId);

        return emoji;
    }

    /**
     * 更新表情
     */
    @Transactional
    public CustomEmoji updateEmoji(Long emojiId, CustomEmojiDTO.UpdateRequest request) {
        CustomEmoji emoji = emojiRepository.findById(emojiId)
                .orElseThrow(() -> new IllegalArgumentException("表情不存在"));

        if (!emoji.isActive()) {
            throw new IllegalArgumentException("表情已删除");
        }

        // 更新显示名称
        if (request.getDisplayName() != null) {
            emoji.setDisplayName(request.getDisplayName());
        }

        // 更新图片 URL
        if (request.getImageUrl() != null) {
            emoji.setImageUrl(request.getImageUrl());
        }

        emoji = emojiRepository.save(emoji);

        log.info("Updated custom emoji: {}", emoji.getName());

        return emoji;
    }

    /**
     * 删除表情（deactivate）
     */
    @Transactional
    public void deleteEmoji(Long emojiId) {
        CustomEmoji emoji = emojiRepository.findById(emojiId)
                .orElseThrow(() -> new IllegalArgumentException("表情不存在"));

        emoji.setDeactivated(true);
        emojiRepository.save(emoji);

        log.info("Deactivated custom emoji: {}", emoji.getName());
    }

    /**
     * 搜索表情
     */
    public List<CustomEmoji> searchEmojis(Long realmId, String query) {
        return emojiRepository.searchByName(realmId, query);
    }

    /**
     * 获取用户创建的表情
     */
    public List<CustomEmoji> getUserEmojis(Long authorId) {
        return emojiRepository.findByAuthorId(authorId);
    }

    /**
     * 统计表情数量
     */
    public Long getEmojiCount(Long realmId) {
        return emojiRepository.countByRealmIdAndActive(realmId);
    }

    /**
     * 解析消息中的自定义表情
     */
    public String parseEmojiCodes(Long realmId, String content) {
        List<CustomEmoji> emojis = getAllEmojis(realmId);

        for (CustomEmoji emoji : emojis) {
            String code = emoji.getEmojiCode();
            if (content.contains(code)) {
                // 替换为图片标签
                String imgTag = String.format("<img src=\"%s\" alt=\"%s\" class=\"emoji\" />",
                        emoji.getImageUrl(), emoji.getName());
                content = content.replace(code, imgTag);
            }
        }

        return content;
    }

    /**
     * 转换为 DTO
     */
    public CustomEmojiDTO.EmojiResponse toResponse(CustomEmoji emoji) {
        return CustomEmojiDTO.EmojiResponse.builder()
                .id(emoji.getId())
                .name(emoji.getName())
                .displayName(emoji.getDisplayName())
                .imageUrl(emoji.getImageUrl())
                .authorId(emoji.getAuthor() != null ? emoji.getAuthor().getId() : null)
                .authorName(emoji.getAuthor() != null ? emoji.getAuthor().getFullName() : null)
                .deactivated(emoji.getDeactivated())
                .dateCreated(emoji.getDateCreated().toString())
                .emojiCode(emoji.getEmojiCode())
                .build();
    }
}