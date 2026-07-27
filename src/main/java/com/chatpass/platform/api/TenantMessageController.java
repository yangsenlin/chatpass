package com.chatpass.platform.api;

import com.chatpass.platform.history.HistoryService;
import com.chatpass.platform.message.MessageIngressService;
import com.chatpass.platform.message.MessageProcessingResult;
import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.tenant.TenantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}")
public class TenantMessageController {

    private final TenantService tenantService;
    private final MessageIngressService messageIngressService;
    private final HistoryService historyService;

    public TenantMessageController(
        TenantService tenantService,
        MessageIngressService messageIngressService,
        HistoryService historyService
    ) {
        this.tenantService = tenantService;
        this.messageIngressService = messageIngressService;
        this.historyService = historyService;
    }

    @PostMapping("/messages/ingress")
    public MessageProcessingResult ingress(@PathVariable String tenantId, @Valid @RequestBody UnifiedMessage message) {
        tenantService.get(tenantId).orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        message.setTenantId(tenantId);
        return messageIngressService.receive(message);
    }

    @GetMapping("/conversations")
    public List<String> conversations(
        @PathVariable String tenantId,
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "50") int limit
    ) {
        tenantService.get(tenantId).orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return historyService.conversations(tenantId, offset, limit);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<Object> messages(
        @PathVariable String tenantId,
        @PathVariable String conversationId,
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "50") int limit
    ) {
        tenantService.get(tenantId).orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return historyService.messages(tenantId, conversationId, offset, limit);
    }
}
