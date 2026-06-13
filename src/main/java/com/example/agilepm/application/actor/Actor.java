package com.example.agilepm.application.actor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

public abstract class Actor implements AutoCloseable {
    private final LinkedBlockingQueue<Runnable> mailbox;
    private final ExecutorService executor;
    private volatile boolean running = true;

    protected Actor(int mailboxCapacity, String actorName) {
        this.mailbox = new LinkedBlockingQueue<>(mailboxCapacity);
        this.executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name(actorName + "-", 0).factory());
        this.executor.submit(this::drainMailbox);
    }

    protected <T> CompletableFuture<T> ask(Supplier<T> action) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        boolean accepted = mailbox.offer(() -> {
            try { promise.complete(action.get()); }
            catch (Throwable t) { promise.completeExceptionally(t); }
        });
        if (!accepted) promise.completeExceptionally(new IllegalStateException("Actor mailbox is full"));
        return promise;
    }

    protected CompletableFuture<Void> tell(ThrowingRunnable action) {
        return ask(() -> { action.run(); return null; });
    }

    private void drainMailbox() {
        while (running) {
            try { mailbox.take().run(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); running = false; }
        }
    }

    @Override public void close() {
        running = false;
        executor.shutdownNow();
    }

    @FunctionalInterface
    protected interface ThrowingRunnable { void run() throws Exception; }
}
