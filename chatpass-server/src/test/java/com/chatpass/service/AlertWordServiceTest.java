package com.chatpass.service;

import com.chatpass.dto.AlertWordDTO;
import com.chatpass.entity.AlertWord;
import com.chatpass.entity.Realm;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.AlertWordRepository;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.UserProfileRepository;
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
 * AlertWordService 测试
 */
@ExtendWith(MockitoExtension.class)
class AlertWordServiceTest {

    @Mock
    private AlertWordRepository alertWordRepository;

    @Mock
    private UserProfileRepository userRepository;

    @Mock
    private RealmRepository realmRepository;

    @InjectMocks
    private AlertWordService alertWordService;

    private UserProfile testUser;
    private Realm testRealm;

    @BeforeEach
    void setUp() {
        testRealm = Realm.builder().id(1L).stringId("test").name("Test").build();
        testUser = UserProfile.builder().id(1L).email("test@test.com").realm(testRealm).build();
    }

    @Test
    @DisplayName("添加 Alert Word")
    void addAlertWord_success() {
        // Given
        AlertWordDTO.CreateRequest request = AlertWordDTO.CreateRequest.builder()
                .word("urgent")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(realmRepository.findById(1L)).thenReturn(Optional.of(testRealm));
        when(alertWordRepository.existsByUserIdAndWord(1L, "urgent")).thenReturn(false);
        when(alertWordRepository.save(any(AlertWord.class))).thenAnswer(invocation -> {
            AlertWord aw = invocation.getArgument(0);
            aw.setId(1L);
            return aw;
        });

        // When
        AlertWordDTO.Response response = alertWordService.addAlertWord(1L, 1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getWord()).isEqualTo("urgent");
        verify(alertWordRepository).save(any(AlertWord.class));
    }

    @Test
    @DisplayName("添加已存在的 Alert Word - 返回现有的")
    void addAlertWord_existing() {
        // Given
        AlertWordDTO.CreateRequest request = AlertWordDTO.CreateRequest.builder()
                .word("urgent")
                .build();

        AlertWord existing = AlertWord.builder()
                .id(1L)
                .word("urgent")
                .user(testUser)
                .realm(testRealm)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(realmRepository.findById(1L)).thenReturn(Optional.of(testRealm));
        when(alertWordRepository.existsByUserIdAndWord(1L, "urgent")).thenReturn(true);
        when(alertWordRepository.findByUserIdAndWord(1L, "urgent")).thenReturn(Optional.of(existing));

        // When
        AlertWordDTO.Response response = alertWordService.addAlertWord(1L, 1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(alertWordRepository, never()).save(any());
    }

    @Test
    @DisplayName("检测消息包含 Alert Word")
    void containsAlertWord_true() {
        // Given
        Set<String> words = Set.of("urgent", "important");
        when(alertWordRepository.findWordsByUserId(1L)).thenReturn(words);

        // When
        boolean result = alertWordService.containsAlertWord(1L, "This is URGENT please check");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检测消息不包含 Alert Word")
    void containsAlertWord_false() {
        // Given
        Set<String> words = Set.of("urgent", "important");
        when(alertWordRepository.findWordsByUserId(1L)).thenReturn(words);

        // When
        boolean result = alertWordService.containsAlertWord(1L, "This is a normal message");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("删除 Alert Word")
    void removeAlertWord_success() {
        // Given
        AlertWord alertWord = AlertWord.builder()
                .id(1L)
                .word("urgent")
                .user(testUser)
                .build();

        when(alertWordRepository.findById(1L)).thenReturn(Optional.of(alertWord));

        // When
        alertWordService.removeAlertWord(1L, 1L);

        // Then
        verify(alertWordRepository).delete(alertWord);
    }

    @Test
    @DisplayName("删除其他用户的 Alert Word - 失败")
    void removeAlertWord_wrongUser() {
        // Given
        UserProfile otherUser = UserProfile.builder().id(2L).build();
        AlertWord alertWord = AlertWord.builder()
                .id(1L)
                .word("urgent")
                .user(otherUser)
                .build();

        when(alertWordRepository.findById(1L)).thenReturn(Optional.of(alertWord));

        // When & Then
        assertThatThrownBy(() -> alertWordService.removeAlertWord(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own alert words");
    }

    @Test
    @DisplayName("批量添加 Alert Words")
    void addAlertWords_batch() {
        // Given
        AlertWordDTO.BatchRequest request = AlertWordDTO.BatchRequest.builder()
                .words(Set.of("urgent", "important", "critical"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(realmRepository.findById(1L)).thenReturn(Optional.of(testRealm));
        when(alertWordRepository.existsByUserIdAndWord(anyLong(), anyString())).thenReturn(false);
        when(alertWordRepository.save(any(AlertWord.class))).thenAnswer(invocation -> {
            AlertWord aw = invocation.getArgument(0);
            aw.setId((long) (Math.random() * 1000));
            return aw;
        });

        // When
        AlertWordDTO.ListResponse response = alertWordService.addAlertWords(1L, 1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCount()).isEqualTo(3);
    }
}