package com.chatpass.platform.security;

import com.chatpass.platform.message.ChannelType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

@Component
public class IngressSecurityVerifier {

    private static final String API_KEY_HEADER = "X-ChatPass-Api-Key";
    private static final String SIGNATURE_HEADER = "X-ChatPass-Signature";

    private final IngressSecurityProperties properties;

    public IngressSecurityVerifier(IngressSecurityProperties properties) {
        this.properties = properties;
    }

    public void verify(ChannelType channel, HttpHeaders headers, String rawBody) {
        if (!properties.isEnabled()) {
            return;
        }
        verifyApiKey(headers);
        verifySignatureIfConfigured(channel, headers, rawBody);
    }

    private void verifyApiKey(HttpHeaders headers) {
        String apiKey = headers.getFirst(API_KEY_HEADER);
        if (!constantTimeEquals(properties.getApiKey(), apiKey)) {
            throw new SecurityException("Invalid ingress API key");
        }
    }

    private void verifySignatureIfConfigured(ChannelType channel, HttpHeaders headers, String rawBody) {
        String secret = properties.getWebhookSecrets().get(channel.name().toLowerCase(Locale.ROOT));
        if (secret == null || secret.isBlank()) {
            return;
        }
        String signature = headers.getFirst(SIGNATURE_HEADER);
        String expected = "sha256=" + hmacSha256(secret, rawBody == null ? "" : rawBody);
        if (!constantTimeEquals(expected, signature)) {
            throw new SecurityException("Invalid webhook signature");
        }
    }

    private String hmacSha256(String secret, String rawBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to calculate webhook signature", ex);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            actual.getBytes(StandardCharsets.UTF_8)
        );
    }
}
