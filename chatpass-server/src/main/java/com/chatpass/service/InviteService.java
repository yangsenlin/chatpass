package com.chatpass.service;

import com.chatpass.entity.Realm;
import com.chatpass.entity.RealmInvite;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.BusinessException;
import com.chatpass.repository.RealmInviteRepository;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * InviteService
 * 
 * 邀请系统服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InviteService {

    private final RealmInviteRepository inviteRepository;
    private final UserProfileRepository userRepository;
    private final RealmRepository realmRepository;
    private final PasswordEncoder passwordEncoder;

    // 默认邀请链接有效期（天）
    private static final int DEFAULT_EXPIRE_DAYS = 7;

    /**
     * 创建邀请链接
     */
    @Transactional
    public RealmInvite createInvite(Long realmId, Long inviterId, String email, Integer maxUses, Integer expireDays) {
        // 获取 Realm 和 Inviter
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new BusinessException("组织不存在"));
        
        UserProfile inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 生成唯一邀请链接
        String inviteLink = UUID.randomUUID().toString().replace("-", "");
        
        // 设置过期时间
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(
                expireDays != null && expireDays > 0 ? expireDays : DEFAULT_EXPIRE_DAYS
        );

        RealmInvite invite = RealmInvite.builder()
                .realm(realm)
                .invitedByUser(inviter)
                .inviteLink(inviteLink)
                .email(email)
                .status(RealmInvite.STATUS_PENDING)
                .expiresAt(expiresAt)
                .maxUses(maxUses != null ? maxUses : 1)
                .build();

        inviteRepository.save(invite);
        log.info("Created invite link {} for realm {} by user {}", inviteLink, realmId, inviterId);
        
        return invite;
    }

    /**
     * 使用邀请链接注册
     */
    @Transactional
    public UserProfile acceptInvite(String inviteLink, String email, String fullName, String password) {
        // 验证邀请链接
        RealmInvite invite = inviteRepository.findValidInvite(inviteLink, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("无效或已过期的邀请链接"));

        // 检查使用次数
        if (invite.getCurrentUses() >= invite.getMaxUses()) {
            throw new BusinessException("邀请链接已达到最大使用次数");
        }

        // 检查邮箱（如果邀请指定了邮箱）
        if (invite.getEmail() != null && !invite.getEmail().equals(email)) {
            throw new BusinessException("此邀请链接仅适用于邮箱: " + invite.getEmail());
        }

        // 检查用户是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("该邮箱已注册");
        }

        // 创建用户
        UserProfile user = UserProfile.builder()
                .realm(invite.getRealm())
                .email(email)
                .fullName(fullName)
                .password(passwordEncoder.encode(password)) // 加密处理
                .isActive(true)
                .role(100) // 普通用户
                .build();
        
        userRepository.save(user);

        // 更新邀请状态
        invite.setCurrentUses(invite.getCurrentUses() + 1);
        invite.setAcceptedAt(LocalDateTime.now());
        invite.setAcceptedUser(user);
        
        if (invite.getCurrentUses() >= invite.getMaxUses()) {
            invite.setStatus(RealmInvite.STATUS_ACCEPTED);
        }
        
        inviteRepository.save(invite);

        log.info("User {} accepted invite {} for realm {}", user.getId(), inviteLink, invite.getRealm().getId());
        
        return user;
    }

    /**
     * 获取组织的所有邀请
     */
    public List<RealmInvite> getRealmInvites(Long realmId) {
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new BusinessException("组织不存在"));
        return inviteRepository.findByRealmOrderByDateCreatedDesc(realm);
    }

    /**
     * 获取用户创建的邀请
     */
    public List<RealmInvite> getUserInvites(Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return inviteRepository.findByInvitedByUserOrderByDateCreatedDesc(user);
    }

    /**
     * 撤销邀请
     */
    @Transactional
    public void revokeInvite(Long inviteId, Long userId) {
        RealmInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new BusinessException("邀请不存在"));

        if (!invite.getInvitedByUser().getId().equals(userId)) {
            throw new BusinessException("无权撤销此邀请");
        }

        if (invite.getStatus() != RealmInvite.STATUS_PENDING) {
            throw new BusinessException("邀请已被使用或已过期");
        }

        invite.setStatus(RealmInvite.STATUS_REVOKED);
        inviteRepository.save(invite);
        
        log.info("Invite {} revoked by user {}", inviteId, userId);
    }

    /**
     * 验证邀请链接
     */
    public boolean isValidInvite(String inviteLink) {
        return inviteRepository.findValidInvite(inviteLink, LocalDateTime.now()).isPresent();
    }

    /**
     * 获取邀请详情
     */
    public RealmInvite getInviteByLink(String inviteLink) {
        return inviteRepository.findByInviteLink(inviteLink)
                .orElseThrow(() -> new BusinessException("邀请链接不存在"));
    }
}