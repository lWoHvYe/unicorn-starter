package com.lwohvye.reactive.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

public class Reactor3ErrorHandlingTest {

    // catch
    @Test
    void test2() {
        Flux.just(10)
                .map(this::doSomethingDangerous)
                .onErrorReturn("RECOVERED").subscribe(System.out::println);
        Flux.just(10)
                .map(this::doSomethingDangerous)
                .onErrorReturn(e -> e.getMessage().equals("boom10"), "recovered10").subscribe(System.out::println);
        Flux.just(10, 20, 30)
                .map(this::doSomethingDangerous)
                .onErrorComplete().subscribe();
    }

    private String doSomethingDangerous(Integer integer) {
        throw new RuntimeException("Dangerous");
    }

    // fallback 这里针对的是 Flux.error，直接throw是无法被onErrorResume catch的
    @Test
    void test3() {
        Flux.just("key1", "key2")
                .flatMap(k -> callExternalService(k)
                        .onErrorResume(e -> getFromCache(k))
                ).subscribe(System.out::println, throwable -> System.err.println(throwable.getMessage()));
    }

    private Flux<String> getFromCache(String k) {
        return Flux.just("Caching " + k);
    }

    private Flux<String> callExternalService(String k) {
        return Flux.error(() -> new RuntimeException("Error occurred"));
    }

    // catch and rethrow，这里对直接throw和Flux.error()都生效，onErrorMap
    @Test
    void test4() {
        Flux.just("timeout1")
                .flatMap(this::callExternalService)
                .onErrorResume(original -> Flux.error(
                        new BusinessException("oops, SLA exceeded", original))
                ).subscribe();

        Flux.just("timeout1")
                .flatMap(this::callExternalService)
                .onErrorMap(original -> new BusinessException("oops, SLA exceeded", original))
                .subscribe();

        Flux.just("timeout1")
                .flatMap(k -> callExternalService(k)
                        .doOnError(throwable -> System.err.println("Error: " + throwable.getMessage())))
                .onErrorMap(original -> new BusinessException("oops, SLA exceeded", original))
                .subscribe();
    }

    // finally & try-with-resource
    @Test
    void test5() {
        Stats stats = new Stats();
        LongAdder statsCancel = new LongAdder();
        // finally
        Flux<String> flux =
                Flux.just("foo", "bar")
                        .doOnSubscribe(stats::startTimer)
                        .doFinally(type -> {
                            stats.stopTimerAndRecordTiming();
                            if (type == SignalType.CANCEL)
                                statsCancel.increment();
                        })
                        .take(1);
        flux.subscribe();

        System.out.println("FinalStats " + statsCancel);


        var stats2 = new Stats();
        // try-with-resource
        Flux<String> flux2 =
                Flux.using(
                        () -> stats2,
                        Stats::publish,
                        Stats::stopTimerAndRecordTiming); // clean up resources
        flux2.subscribe(System.out::println);

    }

    // retry
    @Test
    void test6() throws InterruptedException {
        Flux.interval(Duration.ofMillis(250))
                .<String>handle((input, sink) -> {
                    if (input < 3) {
                        sink.next("tick " + input);
                        return;
                    }
                    sink.error(new RuntimeException("boom"));
                })
                .retry(1)
                .elapsed() // 用来输出经过的时间
                .subscribe(System.out::println, System.err::println);

        Flux<String> flux = Flux
                .<String>error(new IllegalArgumentException())
                .doOnError(System.out::println)
                .retryWhen(Retry.from(companion ->
                        companion.take(3)));
        flux.subscribe();

        Thread.sleep(2100);
    }

    @Test
    void test7() {
        final AtomicInteger transientHelper = new AtomicInteger();
        Supplier<Flux<Integer>> httpRequest = () ->
                Flux.generate(sink -> {
                    int i = transientHelper.getAndIncrement();
                    if (i == 10) {
                        sink.next(i);
                        sink.complete();
                    } else if (i % 3 == 0) {
                        sink.next(i);
                    } else {
                        sink.error(new IllegalStateException("Transient error at " + i));
                    }
                });

        AtomicInteger errorCount = new AtomicInteger();
        Flux<Integer> transientFlux = httpRequest.get()
                .doOnError(e -> errorCount.incrementAndGet());

        transientFlux.retryWhen(Retry.max(2).transientErrors(true))
                .blockLast();

        System.out.println(errorCount);
    }

    @Test
    void test8() {
        var converted = Flux
                .range(1, 10)
                .<String>handle((i, sink) -> {
                    try {
                        sink.next(convert(i));
                    }
                    catch (IOException e) {
                        sink.error(Exceptions.propagate(e));
                    }
                });

        converted.subscribe(
                v -> System.out.println("RECEIVED: " + v),
                e -> {
                    if (Exceptions.unwrap(e) instanceof IOException) {
                        System.out.println("Something bad happened with I/O");
                    } else {
                        System.out.println("Something bad happened");
                    }
                }
        );
    }

    String convert(int i) throws IOException {
        if (i > 3) {
            throw new IOException("boom " + i);
        }
        return "OK " + i;
    }
}
