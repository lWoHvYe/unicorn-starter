package com.lwohvye.reactive.adapter;

import reactor.core.publisher.Flux;
import reactor.adapter.JdkFlowAdapter;

import java.util.concurrent.Flow;

public class Java9FlowToReactorExample {
    public static void main(String[] args) {
        // 创建一个 Java 9 Flow 的 Publisher
        Flow.Publisher<Integer> java9Publisher = subscriber -> subscriber.onSubscribe(new Flow.Subscription() {
            @Override
            public void request(long n) {
                // 在这里产生数据并发送给 Subscriber
                for (int i = 1; i <= 5; i++) {
                    subscriber.onNext(i);
                }
                subscriber.onComplete();
            }

            @Override
            public void cancel() {
                // 取消订阅
            }
        });

        // 使用 JdkFlowAdapter 将 Java 9 Flow 的 Publisher 适配为 Reactor 的 Flux
        Flux<Integer> reactorFlux = JdkFlowAdapter.flowPublisherToFlux(java9Publisher);

        // 订阅 Reactor 的 Flux
        reactorFlux.subscribe(
            item -> System.out.println("Received: " + item),
            error -> System.err.println("Error: " + error.getMessage()),
            () -> System.out.println("Done")
        );
    }
}
