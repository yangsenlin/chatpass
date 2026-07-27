package com.chatpass.platform.routing.repository;

import com.chatpass.platform.config.ChatPassRoutingProperties;
import com.chatpass.platform.routing.TriggerRule;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTriggerRuleRepository implements TriggerRuleRepository {

    private final Map<String, TriggerRule> rules = new ConcurrentHashMap<>();

    public InMemoryTriggerRuleRepository(ChatPassRoutingProperties properties) {
        properties.getRules().forEach(this::save);
    }

    @Override
    public List<TriggerRule> findAll() {
        return rules.values().stream()
            .sorted(Comparator.comparingInt(TriggerRule::getPriority).reversed())
            .toList();
    }

    @Override
    public Optional<TriggerRule> findById(String id) {
        return Optional.ofNullable(rules.get(id));
    }

    @Override
    public TriggerRule save(TriggerRule rule) {
        if (rule.getId() == null || rule.getId().isBlank()) {
            throw new IllegalArgumentException("Rule id is required");
        }
        rules.put(rule.getId(), rule);
        return rule;
    }

    @Override
    public void deleteById(String id) {
        rules.remove(id);
    }

    public void replaceAll(List<TriggerRule> newRules) {
        rules.clear();
        new ArrayList<>(newRules).forEach(this::save);
    }
}
