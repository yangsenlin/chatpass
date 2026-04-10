package com.chatpass.service;

import com.chatpass.dto.AttachmentDTO;
import com.chatpass.entity.*;
import com.chatpass.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AttachmentService 测试
 */
@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock private AttachmentRepository attachmentRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private UserProfileRepository userRepository;
    @Mock private RealmRepository realmRepository;

    @InjectMocks
    private AttachmentService attachmentService;

    private UserProfile testUser;
    private Realm testRealm;

    @BeforeEach
    void setUp() {
        testRealm = Realm.builder().id(1L).build();
        testUser = UserProfile.builder().id(1L).realm(testRealm).build();
    }

    @Test
    @DisplayName("上传文件")
    void upload_success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "test content".getBytes());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(realmRepository.findById(1L)).thenReturn(Optional.of(testRealm));
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(inv -> {
            Attachment a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AttachmentDTO.Response response = attachmentService.upload(1L, 1L, null, file);

        assertThat(response).isNotNull();
        assertThat(response.getOriginalFileName()).isEqualTo("test.png");
        assertThat(response.getIsImage()).isTrue();
    }

    @Test
    @DisplayName("获取存储使用量")
    void getStorageUsage() {
        when(attachmentRepository.getTotalSizeByOwner(1L)).thenReturn(1024L);

        Long usage = attachmentService.getUserStorageUsage(1L);

        assertThat(usage).isEqualTo(1024L);
    }
}