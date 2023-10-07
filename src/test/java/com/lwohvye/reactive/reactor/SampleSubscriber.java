package com.lwohvye.reactive.reactor;

import org.reactivestreams.Subscription;

import reactor.core.publisher.BaseSubscriber;

public class SampleSubscriber<T> extends BaseSubscriber<T> {

    public void hookOnSubscribe(Subscription subscription) {
        System.out.println("Subscribed");
        request(1);
    }

    public void hookOnNext(T value) {
        System.out.println("Next Subscribe " + value);
        request(1);
    }

    @Override
    protected void hookOnComplete() {
        System.out.println("Subscribe Done");
    }
}
