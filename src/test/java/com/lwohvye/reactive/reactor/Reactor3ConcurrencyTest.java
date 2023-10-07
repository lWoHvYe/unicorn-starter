package com.lwohvye.reactive.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ThreadFactory;

public class Reactor3ConcurrencyTest {


    ThreadFactory virtualFactory = Thread.ofVirtual().name("Virtual-Thread-", 1).factory();

    @Test
    void test1() throws InterruptedException {
        final Mono<String> mono = Mono.just("hello ");

        Thread t = virtualFactory.newThread(() -> mono
                .map(msg -> msg + "thread ")
                .subscribe(v ->
                        System.out.println(v + Thread.currentThread().getName())
                )
        );
        t.start();
        t.join();

        Flux.interval(Duration.ofMillis(300), Schedulers.newSingle("test")).subscribe(System.out::println);

        Thread.sleep(2000);
    }

    // publishOn
    @Test
    void test2() throws InterruptedException {
        Scheduler scheduler = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> { // Thread t
                    System.out.println(i + Thread.currentThread().getName() + " map1");
                    return 10 + i;
                })
                .publishOn(scheduler)
                .map(i -> { // parallel-scheduler
                    System.out.println(i + Thread.currentThread().getName() + " map2");
                    return "value " + i;
                });

        var t = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " newThread");
            flux.subscribe(s1 -> { // parallel-scheduler
                System.out.println(s1 + Thread.currentThread().getName() + " subscribe");
            });
        });
        t.start();
        t.join();
    }

    // subscribeOn
    @Test
    void test3() throws InterruptedException {
        Scheduler scheduler = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> { // parallel-scheduler ***
                    System.out.println(i + Thread.currentThread().getName() + " map1");
                    return 10 + i;
                })
                .subscribeOn(scheduler)
                .map(i -> { // parallel-scheduler
                    System.out.println(i + Thread.currentThread().getName() + " map2");
                    return "value " + i;
                });

        var t = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " newThread");
            flux.subscribe(s1 -> { // parallel-scheduler
                System.out.println(s1 + Thread.currentThread().getName() + " subscribe");
            });
        });
        t.start();
        t.join();
    }
}
