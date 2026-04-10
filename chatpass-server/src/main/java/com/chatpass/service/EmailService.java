package com.chatpass.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * EmailService
 * 
 * 邮件发送服务
 */
@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * 发送简单文本邮件
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        if (mailSender == null) {
            log.warn("Mail sender not configured, skipping email to {}", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * 发送 HTML 邮件
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (mailSender == null) {
            log.warn("Mail sender not configured, skipping email to {}", to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * 发送邀请邮件
     */
    public void sendInviteEmail(String to, String inviteLink, String inviterName, String realmName) {
        String subject = "您收到了来自 " + realmName + " 的邀请";
        
        String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f5f5f5; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #333;">欢迎加入 %s</h2>
                    <p style="color: #666;">%s 邀请您加入组织。</p>
                    <p style="margin: 20px 0;">
                        <a href="https://chatpass.com/invite/%s" 
                           style="background-color: #4CAF50; color: white; padding: 10px 20px; 
                                  text-decoration: none; border-radius: 5px;">
                            点击加入
                        </a>
                    </p>
                    <p style="color: #999; font-size: 12px;">
                        此邀请链接将在 7 天后过期
                    </p>
                </div>
            </body>
            </html>
            """.formatted(realmName, inviterName, inviteLink);
        
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * 发送提及通知邮件
     */
    public void sendMentionEmail(String to, String senderName, String contentPreview, Long messageId) {
        String subject = senderName + " 在消息中提及了您";
        
        String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f5f5f5; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #333;">您被提及了</h2>
                    <p style="color: #666;">%s 在消息中提及了您：</p>
                    <div style="background-color: white; padding: 15px; border-radius: 5px; margin: 15px 0;">
                        <p style="color: #333;">%s</p>
                    </div>
                    <p style="margin: 20px 0;">
                        <a href="https://chatpass.com/messages/%d" 
                           style="background-color: #2196F3; color: white; padding: 10px 20px; 
                                  text-decoration: none; border-radius: 5px;">
                            查看消息
                        </a>
                    </p>
                </div>
            </body>
            </html>
            """.formatted(senderName, contentPreview, messageId);
        
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * 发送私信通知邮件
     */
    public void sendDirectMessageEmail(String to, String senderName, String contentPreview) {
        String subject = "您收到了来自 " + senderName + " 的私信";
        
        String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f5f5f5; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #333;">新私信</h2>
                    <p style="color: #666;">%s 发送给您一条私信：</p>
                    <div style="background-color: white; padding: 15px; border-radius: 5px; margin: 15px 0;">
                        <p style="color: #333;">%s</p>
                    </div>
                    <p style="margin: 20px 0;">
                        <a href="https://chatpass.com/dm" 
                           style="background-color: #2196F3; color: white; padding: 10px 20px; 
                                  text-decoration: none; border-radius: 5px;">
                            查看私信
                        </a>
                    </p>
                </div>
            </body>
            </html>
            """.formatted(senderName, contentPreview);
        
        sendHtmlEmail(to, subject, htmlContent);
    }
}