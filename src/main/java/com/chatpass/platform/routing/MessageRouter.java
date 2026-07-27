package com.chatpass.platform.routing;

import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.routing.repository.TriggerRuleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class MessageRouter {

    private final TriggerRuleRepository ruleRepository;
    private final TriggerMatcher triggerMatcher;

    public MessageRouter(TriggerRuleRepository ruleRepository, TriggerMatcher triggerMatcher) {
        this.ruleRepository = ruleRepository;
        this.triggerMatcher = triggerMatcher;
    }

    public RouteDecision route(UnifiedMessage message) {
        TriggerContext context = TriggerContext.from(message);
        TriggerRule matchedRule = triggerMatcher.firstMatch(ruleRepository.findAll(), context).orElse(null);
        return new RouteDecision(UUID.randomUUID().toString(), Instant.now(), matchedRule);
    }
}
