package com.lwohvye.reactive.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Reactor3SinkTest {

    ThreadFactory virtualFactory = Thread.ofVirtual().name("Virtual-Thread-", 1).factory();

    @Test
    void test1() throws InterruptedException {
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();
        var t1 = new Thread(() -> {
            //thread1
            replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        });

        var t2 = new Thread(() -> {
            //thread2, later
            replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        });

        var t3 = new Thread(() -> {
            //thread3, concurrently with thread 2
            //would retry emitting for 2 seconds and fail with EmissionException if unsuccessful
            replaySink.emitNext(3, Sinks.EmitFailureHandler.busyLooping(Duration.ofSeconds(2)));

            //thread3, concurrently with thread 2
            //would return FAIL_NON_SERIALIZED
            Sinks.EmitResult result = replaySink.tryEmitNext(4);
            System.out.println(result);
        });
        var t4 = new Thread(() -> {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5L));
            replaySink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        });
        t1.start();
        t2.start();
        t3.start();
        t4.start();

        Flux<Integer> fluxView = replaySink.asFlux();
        fluxView
                .takeWhile(i -> i < 10)
                .log()
                .blockLast();
    }

    void test2() {
        Sinks.Many<String> multicastSink = Sinks.many().multicast().onBackpressureBuffer();
        Sinks.Many<String> multicastSink2 = Sinks.many().multicast().directBestEffort();
    }
}
