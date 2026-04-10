package com.chatpass.service;

import com.chatpass.dto.StreamDTO;
import com.chatpass.entity.Realm;
import com.chatpass.entity.Stream;
import com.chatpass.entity.Subscription;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Stream 服务
 */
@Service
@RequiredArgsConstructor
public class StreamService {

    private final StreamRepository streamRepository;
    private final RealmRepository realmRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MessageRepository messageRepository;
    private final RecipientRepository recipientRepository;

    @Transactional(readOnly = true)
    public List<StreamDTO.Response> getStreams(Long realmId) {
        return streamRepository.findByRealmIdAndDeactivatedFalse(realmId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StreamDTO.Response getById(Long realmId, Long streamId) {
        Stream stream = streamRepository.findByRealmIdAndId(realmId, streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", streamId));
        return toResponse(stream);
    }

    @Transactional
    public StreamDTO.Response create(Long realmId, Long userId, StreamDTO.CreateRequest request) {
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", realmId));

        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // 检查名称是否已存在
        if (streamRepository.existsByRealmIdAndName(realmId, request.getName())) {
            throw new IllegalArgumentException("Stream name already exists: " + request.getName());
        }

        Stream stream = Stream.builder()
                .name(request.getName())
                .description(request.getDescription() != null ? request.getDescription() : "")
                .realm(realm)
                .creator(user)
                .inviteOnly(request.getInviteOnly() != null ? request.getInviteOnly() : false)
                .isWebPublic(request.getIsWebPublic() != null ? request.getIsWebPublic() : false)
                .deactivated(false)
                .subscriberCount(0)
                .build();

        stream = streamRepository.save(stream);

        // 创建者自动订阅
        subscribe(userId, stream.getId());

        return toResponse(stream);
    }

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
        if (request.getInviteOnly() != null) {
            stream.setInviteOnly(request.getInviteOnly());
        }
        if (request.getIsWebPublic() != null) {
            stream.setIsWebPublic(request.getIsWebPublic());
        }
        if (request.getDeactivated() != null) {
            stream.setDeactivated(request.getDeactivated());
        }

        stream = streamRepository.save(stream);
        return toResponse(stream);
    }

    @Transactional
    public void delete(Long realmId, Long streamId) {
        Stream stream = streamRepository.findByRealmIdAndId(realmId, streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", streamId));

        stream.setDeactivated(true);
        streamRepository.save(stream);
    }

    @Transactional
    public void subscribe(Long userId, Long streamId) {
        if (subscriptionRepository.existsByUserProfileIdAndStreamId(userId, streamId)) {
            return; // 已订阅
        }

        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", streamId));

        Subscription subscription = Subscription.builder()
                .userProfile(user)
                .stream(stream)
                .active(true)
                .color("#c2c2c2")
                .isMuted(false)
                .build();

        subscriptionRepository.save(subscription);

        // 更新订阅者数量
        Long count = subscriptionRepository.countActiveSubscribersByStreamId(streamId);
        stream.setSubscriberCount(count.intValue());
        streamRepository.save(stream);
    }

    @Transactional
    public void unsubscribe(Long userId, Long streamId) {
        subscriptionRepository.findByUserProfileIdAndStreamId(userId, streamId)
                .ifPresent(sub -> {
                    sub.setActive(false);
                    subscriptionRepository.save(sub);
                });

        // 更新订阅者数量
        Long count = subscriptionRepository.countActiveSubscribersByStreamId(streamId);
        streamRepository.findById(streamId).ifPresent(s -> {
            s.setSubscriberCount(count.intValue());
            streamRepository.save(s);
        });
    }

    @Transactional(readOnly = true)
    public List<String> getTopics(Long realmId, Long streamId) {
        Stream stream = streamRepository.findByRealmIdAndId(realmId, streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", streamId));

        // 通过 Recipient 查询
        return recipientRepository.findStreamRecipient(streamId)
                .map(r -> messageRepository.findDistinctSubjectsByRecipientId(r.getId()))
                .orElse(List.of());
    }

    private StreamDTO.Response toResponse(Stream stream) {
        return StreamDTO.Response.builder()
                .id(stream.getId())
                .name(stream.getName())
                .description(stream.getDescription())
                .renderedDescription(stream.getRenderedDescription())
                .realmId(stream.getRealm().getId())
                .inviteOnly(stream.getInviteOnly())
                .isWebPublic(stream.getIsWebPublic())
                .deactivated(stream.getDeactivated())
                .subscriberCount(stream.getSubscriberCount())
                .dateCreated(stream.getDateCreated())
                .build();
    }
}