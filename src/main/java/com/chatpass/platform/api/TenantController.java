package com.chatpass.platform.api;

import com.chatpass.platform.tenant.ChannelIntegration;
import com.chatpass.platform.tenant.ChannelIntegrationService;
import com.chatpass.platform.tenant.Tenant;
import com.chatpass.platform.tenant.TenantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;
    private final ChannelIntegrationService channelIntegrationService;

    public TenantController(TenantService tenantService, ChannelIntegrationService channelIntegrationService) {
        this.tenantService = tenantService;
        this.channelIntegrationService = channelIntegrationService;
    }

    @PostMapping
    public Tenant create(@RequestBody Tenant tenant) {
        return tenantService.create(tenant);
    }

    @GetMapping
    public List<Tenant> list() {
        return tenantService.list();
    }

    @GetMapping("/{tenantId}")
    public Tenant get(@PathVariable String tenantId) {
        return tenantService.get(tenantId).orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }

    @PostMapping("/{tenantId}/suspend")
    public Tenant suspend(@PathVariable String tenantId) {
        return tenantService.suspend(tenantId);
    }

    @PostMapping("/{tenantId}/channels")
    public ChannelIntegration saveChannel(@PathVariable String tenantId, @RequestBody ChannelIntegration integration) {
        return channelIntegrationService.save(tenantId, integration);
    }

    @GetMapping("/{tenantId}/channels")
    public List<ChannelIntegration> listChannels(@PathVariable String tenantId) {
        return channelIntegrationService.list(tenantId);
    }
}
