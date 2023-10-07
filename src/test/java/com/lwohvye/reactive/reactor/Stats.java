package com.lwohvye.reactive.reactor;

import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

public record Stats() implements AutoCloseable {
    public void startTimer(Subscription s) {
        System.out.println("StartTimer " + s);
    }

    public void stopTimerAndRecordTiming() {
        System.out.println("StopTimer");
        close();
    }

    public Flux<String> publish() {
        return Flux.range(2, 5).map(String::valueOf);
    }

    @Override
    public void close() {
        System.err.println("Closing");
    }
}
