package com.chatpass.platform.protection;

import com.chatpass.platform.mqtt.session.MqttSessionRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageSizeValidatorTest {

    @Test
    void shouldRejectPayloadAboveConfiguredLimit() {
        ChatPassProtectionProperties properties = new ChatPassProtectionProperties();
        properties.setMaxMessageBytes(4);
        MessageSizeValidator validator = new MessageSizeValidator(properties, metrics());

        assertThatThrownBy(() -> validator.validateText("ingress", "12345"))
            .isInstanceOf(MessageTooLargeException.class)
            .hasMessageContaining("payload too large");
    }

    @SuppressWarnings("unchecked")
    private ChatPassMetrics metrics() {
        ObjectProvider<MqttSessionRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        return new ChatPassMetrics(new SimpleMeterRegistry(), provider);
    }
}
