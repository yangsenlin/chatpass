package com.chatpass.platform.buffer;

import com.chatpass.platform.message.MessageIngressService;
import com.chatpass.platform.message.MessageProcessingResult;
import com.chatpass.platform.message.UnifiedMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class InMemoryMessageBuffer implements MessageBuffer {

    private final BlockingQueue<BufferedMessage> queue = new LinkedBlockingQueue<>();
    private final Map<String, MessageProcessingResult> results = new ConcurrentHashMap<>();
    private final MessageIngressService messageIngressService;
    private ExecutorService worker;
    private volatile boolean running;

    public InMemoryMessageBuffer(MessageIngressService messageIngressService) {
        this.messageIngressService = messageIngressService;
    }

    @PostConstruct
    public void start() {
        running = true;
        worker = Executors.newSingleThreadExecutor();
        worker.submit(this::consume);
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (worker != null) {
            worker.shutdownNow();
        }
    }

    @Override
    public BufferedMessage enqueue(UnifiedMessage message) {
        BufferedMessage bufferedMessage = new BufferedMessage(message);
        queue.offer(bufferedMessage);
        return bufferedMessage;
    }

    @Override
    public Optional<MessageProcessingResult> result(String bufferId) {
        return Optional.ofNullable(results.get(bufferId));
    }

    @Override
    public int size() {
        return queue.size();
    }

    private void consume() {
        while (running) {
            try {
                BufferedMessage bufferedMessage = queue.take();
                MessageProcessingResult result = messageIngressService.receive(bufferedMessage.getMessage());
                results.put(bufferedMessage.getBufferId(), result);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
