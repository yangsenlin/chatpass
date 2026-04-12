package com.chatpass.service;

import com.chatpass.dto.CustomEmojiDTO;
import com.chatpass.entity.CustomEmoji;
import com.chatpass.repository.CustomEmojiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 自定义表情服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomEmojiService {
    
    private final CustomEmojiRepository emojiRepository;
    
    @Value("${emoji.upload.dir:/tmp/chatpass/emojis}")
    private String uploadDir;
    
    @Value("${emoji.base.url:http://localhost:8080/emojis}")
    private String baseUrl;
    
    /**
     * 上传自定义表情
     */
    @Transactional
    public CustomEmojiDTO uploadEmoji(Long realmId, String name, String aliases, 
                                        Long authorId, MultipartFile file) throws IOException {
        
        // 检查名称是否已存在
        if (emojiRepository.existsByRealmIdAndName(realmId, name)) {
            throw new IllegalArgumentException("表情名称已存在: " + name);
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (!isImageFile(contentType)) {
            throw new IllegalArgumentException("只支持图片文件");
        }
        
        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir, String.valueOf(realmId));
        Files.createDirectories(uploadPath);
        
        // 生成文件名
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + extension;
        
        // 保存文件
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        // 构建URL
        String imageUrl = baseUrl + "/" + realmId + "/" + fileName;
        String imagePath = filePath.toString();
        
        // 创建表情记录
        CustomEmoji emoji = CustomEmoji.builder()
                .name(name)
                .aliases(aliases)
                .imageUrl(imageUrl)
                .imagePath(imagePath)
                .realmId(realmId)
                .authorId(authorId)
                .deactivated(false)
                .usageCount(0)
                .build();
        
        emoji = emojiRepository.save(emoji);
        log.info("上传自定义表情: {} (realmId: {}, author: {})", name, realmId, authorId);
        
        return toDTO(emoji);
    }
    
    /**
     * 获取组织的所有表情
     */
    public List<CustomEmojiDTO> getEmojisByRealm(Long realmId) {
        return emojiRepository.findByRealmIdAndDeactivatedFalseOrderByUsageCountDesc(realmId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取表情详情
     */
    public Optional<CustomEmojiDTO> getEmojiById(Long emojiId) {
        return emojiRepository.findById(emojiId).map(this::toDTO);
    }
    
    /**
     * 根据名称获取表情
     */
    public Optional<CustomEmojiDTO> getEmojiByName(Long realmId, String name) {
        return emojiRepository.findByRealmIdAndNameAndDeactivatedFalse(realmId, name)
                .map(this::toDTO);
    }
    
    /**
     * 搜索表情
     */
    public List<CustomEmojiDTO> searchEmojis(Long realmId, String keyword) {
        return emojiRepository.searchByKeyword(realmId, keyword)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新表情别名
     */
    @Transactional
    public CustomEmojiDTO updateAliases(Long emojiId, String aliases) {
        CustomEmoji emoji = emojiRepository.findById(emojiId)
                .orElseThrow(() -> new IllegalArgumentException("表情不存在: " + emojiId));
        
        emoji.setAliases(aliases);
        emoji = emojiRepository.save(emoji);
        
        log.info("更新表情别名: {} (aliases: {})", emoji.getName(), aliases);
        return toDTO(emoji);
    }
    
    /**
     * 禁用表情
     */
    @Transactional
    public void deactivateEmoji(Long emojiId) {
        CustomEmoji emoji = emojiRepository.findById(emojiId)
                .orElseThrow(() -> new IllegalArgumentException("表情不存在: " + emojiId));
        
        emoji.setDeactivated(true);
        emojiRepository.save(emoji);
        
        log.info("禁用表情: {}", emoji.getName());
    }
    
    /**
     * 恢复表情
     */
    @Transactional
    public void reactivateEmoji(Long emojiId) {
        CustomEmoji emoji = emojiRepository.findById(emojiId)
                .orElseThrow(() -> new IllegalArgumentException("表情不存在: " + emojiId));
        
        emoji.setDeactivated(false);
        emojiRepository.save(emoji);
        
        log.info("恢复表情: {}", emoji.getName());
    }
    
    /**
     * 记录使用
     */
    @Transactional
    public void recordUsage(Long emojiId) {
        emojiRepository.incrementUsageCount(emojiId);
    }
    
    /**
     * 获取用户创建的表情
     */
    public List<CustomEmojiDTO> getEmojisByAuthor(Long authorId) {
        return emojiRepository.findByAuthorIdOrderByCreatedAtDesc(authorId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    private CustomEmojiDTO toDTO(CustomEmoji emoji) {
        List<String> aliasList = null;
        if (emoji.getAliases() != null && !emoji.getAliases().isEmpty()) {
            aliasList = Arrays.asList(emoji.getAliases().split(","));
        }
        
        return CustomEmojiDTO.builder()
                .id(emoji.getId())
                .name(emoji.getName())
                .aliases(emoji.getAliases())
                .imageUrl(emoji.getImageUrl())
                .realmId(emoji.getRealmId())
                .authorId(emoji.getAuthorId())
                .deactivated(emoji.getDeactivated())
                .usageCount(emoji.getUsageCount())
                .createdAt(emoji.getCreatedAt())
                .updatedAt(emoji.getUpdatedAt())
                .aliasList(aliasList)
                .build();
    }
    
    private boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".png";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
