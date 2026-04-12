package com.chatpass.service;

import com.chatpass.dto.NavigationViewDTO;
import com.chatpass.entity.NavigationView;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.NavigationViewRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 导航视图服务
 * 
 * 用户导航栏视图配置管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NavigationViewService {

    private final NavigationViewRepository navigationViewRepository;
    private final UserProfileRepository userRepository;

    /**
     * 创建导航视图
     */
    @Transactional
    public NavigationViewDTO.Response createView(Long userId, NavigationViewDTO.CreateRequest request) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // 检查是否已存在
        if (navigationViewRepository.existsByUserIdAndFragment(userId, request.getFragment())) {
            log.debug("Navigation view already exists: {}", request.getFragment());
            return findByUserIdAndFragment(userId, request.getFragment());
        }

        NavigationView view = NavigationView.builder()
                .user(user)
                .fragment(request.getFragment())
                .isPinned(request.getIsPinned() != null ? request.getIsPinned() : false)
                .name(request.getName())
                .viewType(request.getViewType())
                .build();

        view = navigationViewRepository.save(view);
        log.info("Created navigation view {} for user {}", view.getId(), userId);

        return toResponse(view);
    }

    /**
     * 更新导航视图
     */
    @Transactional
    public NavigationViewDTO.Response updateView(Long userId, String fragment, NavigationViewDTO.UpdateRequest request) {
        NavigationView view = navigationViewRepository.findByUserIdAndFragment(userId, fragment)
                .orElseThrow(() -> new ResourceNotFoundException("Navigation view not found: " + fragment));

        if (request.getIsPinned() != null) {
            view.setIsPinned(request.getIsPinned());
        }

        if (request.getName() != null) {
            view.setName(request.getName());
        }

        view = navigationViewRepository.save(view);
        log.info("Updated navigation view {} for user {}", fragment, userId);

        return toResponse(view);
    }

    /**
     * 删除导航视图
     */
    @Transactional
    public void deleteView(Long userId, String fragment) {
        navigationViewRepository.findByUserIdAndFragment(userId, fragment)
                .ifPresent(view -> {
                    navigationViewRepository.delete(view);
                    log.info("Deleted navigation view {} for user {}", fragment, userId);
                });
    }

    /**
     * 设置固定状态
     */
    @Transactional
    public NavigationViewDTO.Response setPinned(Long userId, String fragment, Boolean isPinned) {
        NavigationView view = navigationViewRepository.findByUserIdAndFragment(userId, fragment)
                .orElse(null);

        if (view == null) {
            // 如果不存在，创建新的（默认固定）
            UserProfile user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            view = NavigationView.builder()
                    .user(user)
                    .fragment(fragment)
                    .isPinned(isPinned)
                    .build();

            view = navigationViewRepository.save(view);
        } else {
            view.setIsPinned(isPinned);
            view = navigationViewRepository.save(view);
        }

        log.info("User {} pinned/unpinned view: {}", userId, fragment);
        return toResponse(view);
    }

    /**
     * 批量设置固定状态
     */
    @Transactional
    public void batchSetPinned(Long userId, List<String> fragments, Boolean isPinned) {
        for (String fragment : fragments) {
            setPinned(userId, fragment, isPinned);
        }
        log.info("User {} batch pinned {} views", userId, fragments.size());
    }

    /**
     * 获取用户的所有导航视图
     */
    @Transactional(readOnly = true)
    public NavigationViewDTO.ListResponse getUserViews(Long userId) {
        List<NavigationView> views = navigationViewRepository.findByUserId(userId);
        
        List<NavigationViewDTO.Response> responses = views.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return NavigationViewDTO.ListResponse.builder()
                .navigationViews(responses)
                .count(responses.size())
                .build();
    }

    /**
     * 获取用户的固定视图
     */
    @Transactional(readOnly = true)
    public NavigationViewDTO.PinnedResponse getPinnedViews(Long userId) {
        List<NavigationView> views = navigationViewRepository.findPinnedViews(userId);
        
        List<NavigationViewDTO.Response> responses = views.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return NavigationViewDTO.PinnedResponse.builder()
                .pinnedViews(responses)
                .count(responses.size())
                .build();
    }

    /**
     * 获取用户的隐藏视图
     */
    @Transactional(readOnly = true)
    public NavigationViewDTO.ListResponse getHiddenViews(Long userId) {
        List<NavigationView> views = navigationViewRepository.findHiddenViews(userId);
        
        List<NavigationViewDTO.Response> responses = views.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return NavigationViewDTO.ListResponse.builder()
                .navigationViews(responses)
                .count(responses.size())
                .build();
    }

    /**
     * 查找特定视图
     */
    @Transactional(readOnly = true)
    public NavigationViewDTO.Response findByUserIdAndFragment(Long userId, String fragment) {
        return navigationViewRepository.findByUserIdAndFragment(userId, fragment)
                .map(this::toResponse)
                .orElse(null);
    }

    /**
     * 清空用户的所有导航视图
     */
    @Transactional
    public void clearAllViews(Long userId) {
        navigationViewRepository.deleteByUserId(userId);
        log.info("Cleared all navigation views for user {}", userId);
    }

    /**
     * 获取固定视图数量
     */
    @Transactional(readOnly = true)
    public long getPinnedViewCount(Long userId) {
        return navigationViewRepository.countPinnedViews(userId);
    }

    /**
     * 转换为响应
     */
    private NavigationViewDTO.Response toResponse(NavigationView view) {
        return NavigationViewDTO.Response.builder()
                .id(view.getId())
                .fragment(view.getFragment())
                .isPinned(view.getIsPinned())
                .name(view.getName())
                .viewType(view.getViewType())
                .dateCreated(view.getDateCreated())
                .build();
    }
}