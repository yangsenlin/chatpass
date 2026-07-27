package com.chatpass.platform.config;

import com.chatpass.platform.routing.TriggerRule;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "chatpass.routing")
public class ChatPassRoutingProperties {

    private List<TriggerRule> rules = new ArrayList<>();

    public List<TriggerRule> getRules() {
        return rules;
    }

    public void setRules(List<TriggerRule> rules) {
        this.rules = rules == null ? new ArrayList<>() : rules;
    }
}
