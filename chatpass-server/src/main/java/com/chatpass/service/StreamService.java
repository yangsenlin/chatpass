package com.chatpass.service;

import com.chatpass.dto.StreamDTO;
import com.chatpass.entity.Realm;
import com.chatpass.entity.Recipient;
import com.chatpass.entity.Stream;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.RecipientRepository;
import com.chatpass.repository.StreamRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Stream 服务 - Zulip 频道/流管理
 */
@Service
@RequiredArgsConstructor
public class StreamService {

    private final StreamRepository streamRepository;
    private final RealmRepository realmRepository;
    private final UserProfileRepository userRepository;
    private final RecipientRepository recipientRepository;

    /**
     * 创建 Stream
     */
    @Transactional
    public StreamDTO.Response create(Long realmId, Long creatorId, StreamDTO.CreateRequest request) {
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", realmId));

        UserProfile creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", creatorId));

        // 检查名称是否重复
        if (streamRepository.existsByRealmIdAndName(realmId, request.getName())) {
            throw new IllegalArgumentException("Stream with this name already exists");
        }

        Stream stream = Stream.builder()
                .realm(realm)
                .name(request.getName())
                .creator(creator)
                .description(request.getDescription() != null ? request.getDescription() : "")
                .inviteOnly(request.getInviteOnly() != null ? request.getInviteOnly() : false)
                .isWebPublic(request.getIsWebPublic() != null ? request.getIsWebPublic() : false)
                .build();

        stream = streamRepository.save(stream);

        // 创建 Recipient
        Recipient recipient = Recipient.builder()
                .type(Recipient.TYPE_STREAM)
                .streamId(stream.getId())
                .build();
        recipient = recipientRepository.save(recipient);

        stream.setRecipient(recipient);
        stream = streamRepository.save(stream);

        return toResponse(stream);
    }

    /**
     * 获取 Realm 下所有 Stream
     */
    @Transactional(readOnly = true)
    public List<StreamDTO.Response> list(Long realmId, boolean includeDeactivated) {
        List<Stream> streams = includeDeactivated 
                ? streamRepository.findByRealmId(realmId)
                : streamRepository.findByRealmIdAndDeactivatedFalse(realmId);
        
        return streams.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取 Stream 详情
     */
    @Transactional(readOnly = true)
    public StreamDTO.Response getById(Long realmId, Long streamId) {
        Stream stream = streamRepository.findByRealmIdAndId(realmId, streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", streamId));
        
        return toResponse(stream);
    }

    /**
     * 更新 Stream
     */
    @Transactional
    public StreamDTO.Response update(Long realmId, Long streamId, StreamDTO.UpdateRequest request) {
        Stream stream = streamRepository.findByRealmIdAndId(realmId, streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", streamId));

        if (request.getName() != null) {
            stream.setName(request.getName());
        }
        if (request.getDescription() != null) {
            stream.setDescription(request.getDescription());
        }

        stream = streamRepository.save(stream);
        return toResponse(stream);
    }

    /**
     * 删除（停用）Stream
     */
    @Transactional
    public void deactivate(Long realmId, Long streamId) {
        Stream stream = streamRepository.findByRealmIdAndId(realmId, streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", streamId));

        stream.setDeactivated(true);
        streamRepository.save(stream);
    }

    private StreamDTO.Response toResponse(Stream stream) {
        return StreamDTO.Response.builder()
                .id(stream.getId())
                .name(stream.getName())
                .description(stream.getDescription())
                .renderedDescription(stream.getRenderedDescription())
                .inviteOnly(stream.getInviteOnly())
                .isWebPublic(stream.getIsWebPublic())
                .deactivated(stream.getDeactivated())
                .subscriberCount(stream.getSubscriberCount())
                .streamPostPolicy(stream.getStreamPostPolicy())
                .dateCreated(stream.getDateCreated())
                .build();
    }
}