package com.chatpass.platform.workflow;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowActionRegistryTest {

    @Test
    void shouldKeepExistingWorkflowActionTypesRegistered() {
        ActionRegistry registry = new ActionRegistry(List.of(
            new AutoReplyAction(),
            new TransferToAgentAction(),
            new NoopAction(),
            new HttpWebhookAction()
        ));

        assertThat(registry.find(WorkflowType.AUTO_REPLY)).isPresent();
        assertThat(registry.find(WorkflowType.TRANSFER_TO_AGENT)).isPresent();
        assertThat(registry.find(WorkflowType.NOOP)).isPresent();
        assertThat(registry.find(WorkflowType.HTTP_WEBHOOK)).isPresent();
    }
}
