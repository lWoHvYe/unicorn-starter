package com.lwohvye.reactive.adapter;

import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Java9FlowToReactorExample {
    public static void main(String[] args) {
        // 创建一个 Java 9 Flow 的 Publisher
        Flow.Publisher<Integer> java9Publisher = subscriber ->
                // 针对传入的subscriber，设置其subscription属性
                subscriber.onSubscribe(new Flow.Subscription() {
                    @Override
                    public void request(long n) {
                        // 在这里产生数据并发送给 Subscriber
                        // publishes items to the subscriber asynchronously,
                        for (int i = 1; i <= 5; i++) {
                            subscriber.onNext(i);
                            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1L));
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

    // 使用SubmissionPublisher更直观
    public static void main2(String[] args) throws InterruptedException {

        // 创建一个 SubmissionPublisher，它实现了 Flow.Publisher 接口
        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();

        // 使用 JdkFlowAdapter 将 Java 9 Flow 的 Publisher 适配为 Reactor 的 Flux
        Flux<Integer> reactorFlux = JdkFlowAdapter.flowPublisherToFlux(publisher);


        var publishThread = new Thread(() -> {
            for (var i = 1; i <= 5; i++) {
                // submit 1 to count
                publisher.submit(i);
            }
            // Close the publisher
            publisher.close();
        });

        // 订阅 Reactor 的 Flux
        reactorFlux.subscribe(
                item -> System.out.println("Received: " + item),
                error -> System.err.println("Error: " + error.getMessage()),
                () -> System.out.println("Done")
        );

        publishThread.start();
        publishThread.join();
    }
}
