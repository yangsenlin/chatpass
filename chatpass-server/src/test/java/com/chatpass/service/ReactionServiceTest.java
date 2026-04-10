package com.chatpass.service;

import com.chatpass.dto.ReactionDTO;
import com.chatpass.entity.*;
import com.chatpass.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReactionService 测试
 */
@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

    @Mock private ReactionRepository reactionRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private UserProfileRepository userRepository;
    @Mock private RealmRepository realmRepository;

    @InjectMocks
    private ReactionService reactionService;

    private UserProfile testUser;
    private Realm testRealm;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        testRealm = Realm.builder().id(1L).stringId("test").build();
        testUser = UserProfile.builder().id(1L).email("test@test.com").fullName("Test").realm(testRealm).build();
        testMessage = Message.builder().id(1L).sender(testUser).realm(testRealm).build();
    }

    @Test
    @DisplayName("添加表情反应")
    void addReaction_success() {
        ReactionDTO.AddRequest request = ReactionDTO.AddRequest.builder()
                .messageId(1L).emojiCode("👍").emojiName("thumbs_up").build();

        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(realmRepository.findById(1L)).thenReturn(Optional.of(testRealm));
        when(reactionRepository.existsByUserIdAndMessageIdAndEmojiCode(1L, 1L, "👍")).thenReturn(false);
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(inv -> {
            Reaction r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReactionDTO.Response response = reactionService.addReaction(1L, 1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getEmojiCode()).isEqualTo("👍");
    }

    @Test
    @DisplayName("移除表情反应")
    void removeReaction_success() {
        reactionService.removeReaction(1L, 1L, "👍");
        verify(reactionRepository).deleteByUserIdAndMessageIdAndEmojiCode(1L, 1L, "👍");
    }

    @Test
    @DisplayName("获取消息反应 - 聚合")
    void getMessageReactions_aggregated() {
        Reaction r1 = Reaction.builder().id(1L).message(testMessage).user(testUser)
                .emojiCode("👍").emojiName("thumbs_up").build();
        Reaction r2 = Reaction.builder().id(2L).message(testMessage).user(testUser)
                .emojiCode("❤️").emojiName("heart").build();

        when(reactionRepository.findByMessageIdOrderByEmoji(1L)).thenReturn(List.of(r1, r2));

        ReactionDTO.ListResponse response = reactionService.getMessageReactions(1L);

        assertThat(response).isNotNull();
        assertThat(response.getReactions()).hasSize(2);
    }
}