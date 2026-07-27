package com.chatpass.platform.routing.repository;

import com.chatpass.platform.routing.TriggerRule;

import java.util.List;
import java.util.Optional;

public interface TriggerRuleRepository {

    List<TriggerRule> findAll();

    Optional<TriggerRule> findById(String id);

    TriggerRule save(TriggerRule rule);

    void deleteById(String id);
}
