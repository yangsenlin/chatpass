package com.chatpass.service;

import com.chatpass.dto.MessageDTO;
import com.chatpass.entity.*;
import com.chatpass.repository.*;
import com.chatpass.websocket.WebSocketEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MessageService 测试
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserProfileRepository userRepository;

    @Mock
    private StreamRepository streamRepository;

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private RealmRepository realmRepository;

    @Mock
    private UserMessageRepository userMessageRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private MarkdownService markdownService;

    @Mock
    private AlertWordService alertWordService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MessageService messageService;

    private UserProfile testUser;
    private Realm testRealm;
    private Stream testStream;
    private Recipient testRecipient;

    @BeforeEach
    void setUp() {
        testRealm = Realm.builder()
                .id(1L)
                .stringId("test")
                .name("Test Realm")
                .build();

        testUser = UserProfile.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .realm(testRealm)
                .isActive(true)
                .build();

        testRecipient = Recipient.builder()
                .id(1L)
                .type(Recipient.TYPE_STREAM)
                .streamId(1L)
                .build();

        testStream = Stream.builder()
                .id(1L)
                .name("general")
                .realm(testRealm)
                .recipient(testRecipient)
                .build();
    }

    @Test
    @DisplayName("发送 Stream 消息成功")
    void sendStreamMessage_success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(streamRepository.findByRealmIdAndId(1L, 1L)).thenReturn(Optional.of(testStream));
        when(realmRepository.findById(1L)).thenReturn(Optional.of(testRealm));
        when(recipientRepository.findByTypeAndStreamId(Recipient.TYPE_STREAM, 1L)).thenReturn(Optional.of(testRecipient));
        when(markdownService.detectMentions(anyString())).thenReturn(new MarkdownService.MentionResult());
        when(markdownService.render(anyString())).thenReturn("<p>test</p>");
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(1L);
            return msg;
        });
        when(subscriptionRepository.findByStreamId(1L)).thenReturn(java.util.Collections.emptyList());

        // When
        MessageDTO.Response response = messageService.sendStreamMessage(
                1L, 1L, 1L, "Test Topic", "Hello World");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Hello World");
        assertThat(response.getType()).isEqualTo("stream");
        assertThat(response.getSubject()).isEqualTo("Test Topic");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("发送私信成功")
    void sendDirectMessage_success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(realmRepository.findById(1L)).thenReturn(Optional.of(testRealm));
        when(recipientRepository.save(any(Recipient.class))).thenAnswer(invocation -> {
            Recipient r = invocation.getArgument(0);
            r.setId(2L);
            return r;
        });
        when(markdownService.render(anyString())).thenReturn("<p>test</p>");
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(1L);
            return msg;
        });
        when(userMessageRepository.save(any())).thenReturn(null);

        // When
        MessageDTO.Response response = messageService.sendDirectMessage(
                1L, 1L, java.util.List.of(2L), "Hello");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo("private");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("获取消息详情成功")
    void getById_success() {
        // Given
        Message message = Message.builder()
                .id(1L)
                .sender(testUser)
                .recipient(testRecipient)
                .realm(testRealm)
                .content("Test")
                .isChannelMessage(true)
                .build();

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(streamRepository.findById(1L)).thenReturn(Optional.of(testStream));

        // When
        MessageDTO.Response response = messageService.getById(1L, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("@提及检测")
    void mentionDetection() {
        // Given
        MarkdownService.MentionResult result = new MarkdownService.MentionResult();
        result.mentionedUsers.add("test@example.com");
        result.hasWildcardMention = true;

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(streamRepository.findByRealmIdAndId(1L, 1L)).thenReturn(Optional.of(testStream));
        when(realmRepository.findById(1L)).thenReturn(Optional.of(testRealm));
        when(recipientRepository.findByTypeAndStreamId(Recipient.TYPE_STREAM, 1L)).thenReturn(Optional.of(testRecipient));
        when(markdownService.detectMentions("@test@example.com @all")).thenReturn(result);
        when(markdownService.render(anyString())).thenReturn("<p>test</p>");
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(1L);
            return msg;
        });

        Subscription sub = Subscription.builder()
                .userProfile(testUser)
                .stream(testStream)
                .active(true)
                .build();
        when(subscriptionRepository.findByStreamId(1L)).thenReturn(java.util.List.of(sub));

        // When
        messageService.sendStreamMessage(1L, 1L, 1L, "Test", "@test@example.com @all");

        // Then
        verify(userMessageRepository).save(argThat(um -> 
                (um.getFlags() & UserMessage.FLAG_MENTIONED) != 0 &&
                (um.getFlags() & UserMessage.FLAG_WILDCARD_MENTIONED) != 0
        ));
    }
}