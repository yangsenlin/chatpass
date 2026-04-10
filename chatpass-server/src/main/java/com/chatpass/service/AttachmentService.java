package com.chatpass.service;

import com.chatpass.dto.AttachmentDTO;
import com.chatpass.entity.*;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.*;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Attachment 服务
 * 
 * 文件上传管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userRepository;
    private final RealmRepository realmRepository;

    @Value("${attachment.upload.dir:/tmp/chatpass/uploads}")
    private String uploadDir;

    @Value("${attachment.max.size:10485760}") // 10MB
    private Long maxSize;

    @Value("${attachment.base.url:http://localhost:8080/uploads}")
    private String baseUrl;

    private static final List<String> IMAGE_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> VIDEO_TYPES = List.of(
            "video/mp4", "video/webm", "video/ogg"
    );

    /**
     * 上传文件
     */
    @Transactional
    public AttachmentDTO.Response upload(Long userId, Long realmId, Long messageId, 
                                          MultipartFile file) throws IOException {
        // 验证文件大小
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed: " + maxSize);
        }

        UserProfile owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", realmId));

        // 创建上传目录
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadPath = Paths.get(uploadDir, datePath);
        Files.createDirectories(uploadPath);

        // 生成文件名
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        String newFileName = UUID.randomUUID().toString() + extension;
        String pathId = datePath + "/" + newFileName;

        // 保存文件
        Path filePath = uploadPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath);

        // 确定文件类型
        String contentType = file.getContentType();
        int fileType = determineFileType(contentType);

        Message message = null;
        if (messageId != null) {
            message = messageRepository.findById(messageId).orElse(null);
        }

        Attachment attachment = Attachment.builder()
                .message(message)
                .owner(owner)
                .realm(realm)
                .fileName(newFileName)
                .originalFileName(originalFileName)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .contentType(contentType)
                .fileType(fileType)
                .url(baseUrl + "/" + pathId)
                .pathId(pathId)
                .isReady(true)
                .build();

        attachment = attachmentRepository.save(attachment);

        log.info("User {} uploaded file: {} ({} bytes)", userId, originalFileName, file.getSize());

        return toResponse(attachment);
    }

    /**
     * 关联附件到消息
     */
    @Transactional
    public void attachToMessage(Long attachmentId, Long messageId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        attachment.setMessage(message);
        attachmentRepository.save(attachment);

        // 更新消息的附件标志
        message.setHasAttachment(true);
        messageRepository.save(message);
    }

    /**
     * 获取消息的附件
     */
    @Transactional(readOnly = true)
    public AttachmentDTO.ListResponse getMessageAttachments(Long messageId) {
        List<Attachment> attachments = attachmentRepository.findActiveByMessageId(messageId);

        List<AttachmentDTO.Response> responses = attachments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        Long totalSize = responses.stream()
                .mapToLong(a -> a.getFileSize() != null ? a.getFileSize() : 0L)
                .sum();

        return AttachmentDTO.ListResponse.builder()
                .messageId(messageId)
                .attachments(responses)
                .count(responses.size())
                .totalSize(totalSize)
                .build();
    }

    /**
     * 删除附件
     */
    @Transactional
    public void delete(Long attachmentId, Long userId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));

        if (!attachment.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own attachments");
        }

        attachment.setIsDeleted(true);
        attachmentRepository.save(attachment);

        log.info("User {} deleted attachment: {}", userId, attachmentId);
    }

    /**
     * 获取用户的存储使用量
     */
    @Transactional(readOnly = true)
    public Long getUserStorageUsage(Long userId) {
        return attachmentRepository.getTotalSizeByOwner(userId);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private int determineFileType(String contentType) {
        if (contentType == null) {
            return Attachment.TYPE_FILE;
        }
        if (IMAGE_TYPES.contains(contentType)) {
            return Attachment.TYPE_IMAGE;
        }
        if (VIDEO_TYPES.contains(contentType)) {
            return Attachment.TYPE_VIDEO;
        }
        if ("application/pdf".equals(contentType)) {
            return Attachment.TYPE_PDF;
        }
        return Attachment.TYPE_FILE;
    }

    private AttachmentDTO.Response toResponse(Attachment attachment) {
        return AttachmentDTO.Response.builder()
                .id(attachment.getId())
                .messageId(attachment.getMessage() != null ? attachment.getMessage().getId() : null)
                .fileName(attachment.getFileName())
                .originalFileName(attachment.getOriginalFileName())
                .url(attachment.getUrl())
                .thumbnailUrl(attachment.getThumbnailUrl())
                .fileSize(attachment.getFileSize())
                .contentType(attachment.getContentType())
                .fileType(attachment.getFileType())
                .width(attachment.getWidth())
                .height(attachment.getHeight())
                .isImage(attachment.isImage())
                .dateCreated(attachment.getDateCreated())
                .build();
    }
}