package com.chatpass.platform.api;

import com.chatpass.platform.message.MessageIngressService;
import com.chatpass.platform.message.MessageProcessingResult;
import com.chatpass.platform.message.UnifiedMessage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageIngressController {

    private final MessageIngressService messageIngressService;

    public MessageIngressController(MessageIngressService messageIngressService) {
        this.messageIngressService = messageIngressService;
    }

    @PostMapping("/ingress")
    public MessageProcessingResult ingress(@Valid @RequestBody UnifiedMessage message) {
        return messageIngressService.receive(message);
    }
}
