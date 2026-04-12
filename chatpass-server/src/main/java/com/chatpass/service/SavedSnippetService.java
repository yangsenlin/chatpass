package com.chatpass.service;

import com.chatpass.dto.SavedSnippetDTO;
import com.chatpass.entity.Realm;
import com.chatpass.entity.SavedSnippet;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.SavedSnippetRepository;
import com.chatpass.repository.UserProfileRepository;
import com.chatpass.repository.RealmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 保存片段服务
 * 
 * 用户预设回复片段管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SavedSnippetService {

    private final SavedSnippetRepository savedSnippetRepository;
    private final UserProfileRepository userRepository;
    private final RealmRepository realmRepository;

    /**
     * 创建保存片段
     */
    @Transactional
    public SavedSnippetDTO.Response createSnippet(Long userId, Long realmId, SavedSnippetDTO.CreateRequest request) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", realmId));

        // 验证标题长度
        if (request.getTitle() != null && request.getTitle().length() > SavedSnippet.MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title cannot exceed " + SavedSnippet.MAX_TITLE_LENGTH + " characters");
        }

        SavedSnippet snippet = SavedSnippet.builder()
                .user(user)
                .realm(realm)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        snippet = savedSnippetRepository.save(snippet);
        log.info("User {} created saved snippet: {}", userId, snippet.getId());

        return toResponse(snippet);
    }

    /**
     * 更新保存片段
     */
    @Transactional
    public SavedSnippetDTO.Response updateSnippet(Long userId, Long snippetId, SavedSnippetDTO.UpdateRequest request) {
        SavedSnippet snippet = savedSnippetRepository.findById(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSnippet", snippetId));

        if (!snippet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only modify your own saved snippets");
        }

        if (request.getTitle() != null) {
            if (request.getTitle().length() > SavedSnippet.MAX_TITLE_LENGTH) {
                throw new IllegalArgumentException("Title cannot exceed " + SavedSnippet.MAX_TITLE_LENGTH + " characters");
            }
            snippet.setTitle(request.getTitle());
        }

        if (request.getContent() != null) {
            snippet.setContent(request.getContent());
        }

        snippet = savedSnippetRepository.save(snippet);
        log.info("User {} updated saved snippet: {}", userId, snippetId);

        return toResponse(snippet);
    }

    /**
     * 删除保存片段
     */
    @Transactional
    public void deleteSnippet(Long userId, Long snippetId) {
        SavedSnippet snippet = savedSnippetRepository.findById(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSnippet", snippetId));

        if (!snippet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own saved snippets");
        }

        savedSnippetRepository.delete(snippet);
        log.info("User {} deleted saved snippet: {}", userId, snippetId);
    }

    /**
     * 获取用户的所有保存片段
     */
    @Transactional(readOnly = true)
    public SavedSnippetDTO.ListResponse getUserSnippets(Long userId) {
        List<SavedSnippet> snippets = savedSnippetRepository.findByUserIdOrderByDateCreatedDesc(userId);
        
        List<SavedSnippetDTO.Response> responses = snippets.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return SavedSnippetDTO.ListResponse.builder()
                .savedSnippets(responses)
                .count(responses.size())
                .build();
    }

    /**
     * 获取单个保存片段
     */
    @Transactional(readOnly = true)
    public SavedSnippetDTO.Response getSnippet(Long userId, Long snippetId) {
        SavedSnippet snippet = savedSnippetRepository.findById(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSnippet", snippetId));

        if (!snippet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only view your own saved snippets");
        }

        return toResponse(snippet);
    }

    /**
     * 搜索保存片段
     */
    @Transactional(readOnly = true)
    public SavedSnippetDTO.ListResponse searchSnippets(Long userId, String keyword) {
        List<SavedSnippet> snippets = savedSnippetRepository.searchByKeyword(userId, keyword);
        
        List<SavedSnippetDTO.Response> responses = snippets.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return SavedSnippetDTO.ListResponse.builder()
                .savedSnippets(responses)
                .count(responses.size())
                .build();
    }

    /**
     * 获取用户的片段数量
     */
    @Transactional(readOnly = true)
    public long getUserSnippetCount(Long userId) {
        return savedSnippetRepository.countByUserId(userId);
    }

    /**
     * 清空用户的所有片段
     */
    @Transactional
    public void clearAllSnippets(Long userId) {
        savedSnippetRepository.deleteByUserId(userId);
        log.info("Cleared all saved snippets for user {}", userId);
    }

    /**
     * 转换为响应
     */
    private SavedSnippetDTO.Response toResponse(SavedSnippet snippet) {
        return SavedSnippetDTO.Response.builder()
                .id(snippet.getId())
                .title(snippet.getTitle())
                .content(snippet.getContent())
                .dateCreated(snippet.getDateCreated())
                .build();
    }
}