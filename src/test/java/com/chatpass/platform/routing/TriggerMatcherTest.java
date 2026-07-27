package com.chatpass.platform.routing;

import com.chatpass.platform.message.ChannelType;
import com.chatpass.platform.message.UnifiedMessage;
import com.chatpass.platform.workflow.WorkflowType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TriggerMatcherTest {

    private final TriggerMatcher triggerMatcher = new TriggerMatcher(new ConditionMatcher());

    @Test
    void shouldMatchKeywordRuleByPriority() {
        UnifiedMessage message = new UnifiedMessage();
        message.setMessageId("m1");
        message.setChannel(ChannelType.MQTT);
        message.setSenderId("visitor-1");
        message.setText("I need AI help");

        TriggerRule fallback = rule("fallback", 0, WorkflowType.AUTO_REPLY);
        TriggerRule keyword = rule("keyword", 100, WorkflowType.DIFY_CHAT);
        RuleCondition condition = new RuleCondition();
        condition.setField("content.text");
        condition.setOperator(MatchOperator.CONTAINS);
        condition.setValue("ai");
        keyword.setConditions(List.of(condition));

        TriggerRule matched = triggerMatcher.firstMatch(
            List.of(fallback, keyword),
            TriggerContext.from(message)
        ).orElseThrow();

        assertThat(matched.getId()).isEqualTo("keyword");
    }

    @Test
    void shouldSupportAnyConditionGroup() {
        UnifiedMessage message = new UnifiedMessage();
        message.setMessageId("m2");
        message.setChannel(ChannelType.WHATSAPP);
        message.setSenderId("visitor-2");
        message.setText("hello");

        RuleCondition channelCondition = new RuleCondition();
        channelCondition.setField("channel");
        channelCondition.setOperator(MatchOperator.EQUALS);
        channelCondition.setValue("WHATSAPP");

        RuleCondition senderCondition = new RuleCondition();
        senderCondition.setField("sender.id");
        senderCondition.setOperator(MatchOperator.EQUALS);
        senderCondition.setValue("vip");

        TriggerRule rule = rule("social", 10, WorkflowType.AUTO_REPLY);
        rule.setConditionOperator(ConditionGroupOperator.ANY);
        rule.setConditions(List.of(channelCondition, senderCondition));

        assertThat(triggerMatcher.matches(rule, TriggerContext.from(message))).isTrue();
    }

    private TriggerRule rule(String id, int priority, WorkflowType workflowType) {
        TriggerRule rule = new TriggerRule();
        rule.setId(id);
        rule.setName(id);
        rule.setPriority(priority);
        WorkflowTarget target = new WorkflowTarget();
        target.setType(workflowType);
        rule.setWorkflow(target);
        return rule;
    }
}
