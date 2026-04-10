package com.chatpass.service;

import com.chatpass.dto.NarrowDTO;
import com.chatpass.entity.*;
import com.chatpass.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NarrowService 测试
 */
@ExtendWith(MockitoExtension.class)
class NarrowServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private StreamRepository streamRepository;

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private UserProfileRepository userRepository;

    @Mock
    private UserMessageRepository userMessageRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private RealmRepository realmRepository;

    @InjectMocks
    private NarrowService narrowService;

    private Realm testRealm;
    private UserProfile testUser;
    private Stream testStream;
    private Recipient testRecipient;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        testRealm = Realm.builder().id(1L).stringId("test").name("Test").build();
        testUser = UserProfile.builder().id(1L).email("test@test.com").fullName("Test").realm(testRealm).build();
        testRecipient = Recipient.builder().id(1L).type(Recipient.TYPE_STREAM).streamId(1L).build();
        testStream = Stream.builder().id(1L).name("general").realm(testRealm).recipient(testRecipient).build();
        
        testMessage = Message.builder()
                .id(1L)
                .sender(testUser)
                .recipient(testRecipient)
                .realm(testRealm)
                .subject("Test Topic")
                .content("Hello World")
                .isChannelMessage(true)
                .build();
    }

    @Test
    @DisplayName("Narrow 查询 - Stream 过滤")
    void query_streamFilter() {
        // Given
        NarrowDTO.Filter filter = NarrowDTO.Filter.builder()
                .operator(NarrowDTO.Operators.STREAM)
                .operand("general")
                .build();

        NarrowDTO.Request request = NarrowDTO.Request.builder()
                .narrow(List.of(filter))
                .numBefore(50)
                .numAfter(0)
                .build();

        when(streamRepository.findByRealmIdAndName(1L, "general")).thenReturn(Optional.of(testStream));
        when(recipientRepository.findByTypeAndStreamId(Recipient.TYPE_STREAM, 1L)).thenReturn(Optional.of(testRecipient));
        when(messageRepository.findByRecipientIdInOrderByDateSentDesc(anyList(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testMessage)));
        when(streamRepository.findById(1L)).thenReturn(Optional.of(testStream));

        // When
        NarrowDTO.Response response = narrowService.query(1L, 1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessages()).hasSize(1);
        assertThat(response.getMessages().get(0).getStreamName()).isEqualTo("general");
    }

    @Test
    @DisplayName("Narrow 查询 - Stream + Topic 过滤")
    void query_streamAndTopicFilter() {
        // Given
        NarrowDTO.Filter streamFilter = NarrowDTO.Filter.builder()
                .operator(NarrowDTO.Operators.STREAM)
                .operand("general")
                .build();

        NarrowDTO.Filter topicFilter = NarrowDTO.Filter.builder()
                .operator(NarrowDTO.Operators.TOPIC)
                .operand("Test Topic")
                .build();

        NarrowDTO.Request request = NarrowDTO.Request.builder()
                .narrow(List.of(streamFilter, topicFilter))
                .build();

        when(streamRepository.findByRealmIdAndName(1L, "general")).thenReturn(Optional.of(testStream));
        when(recipientRepository.findByTypeAndStreamId(Recipient.TYPE_STREAM, 1L)).thenReturn(Optional.of(testRecipient));
        when(messageRepository.findByRecipientIdAndSubjectOrderByDateSentAsc(1L, "Test Topic"))
                .thenReturn(List.of(testMessage));
        when(streamRepository.findById(1L)).thenReturn(Optional.of(testStream));

        // When
        NarrowDTO.Response response = narrowService.query(1L, 1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessages()).hasSize(1);
    }

    @Test
    @DisplayName("Narrow 查询 - 空过滤返回 Home View")
    void query_emptyFilter_returnsHomeView() {
        // Given
        NarrowDTO.Request request = NarrowDTO.Request.builder()
                .narrow(Collections.emptyList())
                .numBefore(50)
                .numAfter(0)
                .build();

        Subscription sub = Subscription.builder()
                .userProfile(testUser)
                .stream(testStream)
                .active(true)
                .build();

        when(subscriptionRepository.findByUserProfileIdAndActiveTrue(1L)).thenReturn(List.of(sub));
        when(recipientRepository.findByStreamIdIn(anyList())).thenReturn(List.of(testRecipient));
        when(messageRepository.findByRecipientIdInOrderByDateSentDesc(anyList(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testMessage)));
        when(streamRepository.findById(1L)).thenReturn(Optional.of(testStream));

        // When
        NarrowDTO.Response response = narrowService.query(1L, 1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessages()).hasSize(1);
    }

    @Test
    @DisplayName("Narrow 查询 - 发送者过滤")
    void query_senderFilter() {
        // Given
        NarrowDTO.Filter filter = NarrowDTO.Filter.builder()
                .operator(NarrowDTO.Operators.SENDER)
                .operand("test@test.com")
                .build();

        NarrowDTO.Request request = NarrowDTO.Request.builder()
                .narrow(List.of(filter))
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(messageRepository.findBySenderIdOrderByDateSentDesc(1L)).thenReturn(List.of(testMessage));
        when(streamRepository.findById(1L)).thenReturn(Optional.of(testStream));

        // When
        NarrowDTO.Response response = narrowService.query(1L, 1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessages()).hasSize(1);
    }

    @Test
    @DisplayName("Narrow 查询 - 搜索内容")
    void query_searchFilter() {
        // Given
        NarrowDTO.Filter filter = NarrowDTO.Filter.builder()
                .operator(NarrowDTO.Operators.SEARCH)
                .operand("Hello")
                .build();

        NarrowDTO.Request request = NarrowDTO.Request.builder()
                .narrow(List.of(filter))
                .build();

        when(messageRepository.searchByContent(1L, "Hello")).thenReturn(List.of(testMessage));
        when(streamRepository.findById(1L)).thenReturn(Optional.of(testStream));

        // When
        NarrowDTO.Response response = narrowService.query(1L, 1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessages()).hasSize(1);
    }
}