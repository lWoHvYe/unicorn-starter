package com.lwohvye.reactive.adapter;

import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

import java.util.concurrent.Flow;

public class ReactorToJava9FlowExample {
    public static void main(String[] args) {
        // 创建一个 Flux
        Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5)
                .doOnSubscribe(System.out::println);

        // 使用 JdkFlowAdapter 将 Flux 适配为 Java 9 Flow 的 Publisher
        Flow.Publisher<Integer> publisher = JdkFlowAdapter.publisherToFlowPublisher(flux);

        // 创建一个 Java 9 Flow 的 Subscriber
        Flow.Subscriber<Integer> subscriber = new Flow.Subscriber<>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                System.out.println("Received: " + item);
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Done");
            }
        };

        // 订阅 Java 9 Flow 的 Publisher
        publisher.subscribe(subscriber);
    }
}
