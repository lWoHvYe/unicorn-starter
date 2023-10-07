package com.lwohvye.reactive.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

class Reactor3Test {

    // initFlux
    @Test
    void test1() {
        Flux<Integer> ints = Flux.range(1, 4);
        ints.subscribe(i -> System.out.println("Subscribe " + i),
                error -> System.err.println("Error " + error),
                () -> System.out.println("Done"));
    }

    Flux<String> initStringFlux() {
        var stringFlux1 = Flux.fromIterable(List.of("1", "2", "3"));
        return Flux.range(2, 5).map(String::valueOf);
    }

    // BaseSubscriber
    @Test
    void test2() {
        SampleSubscriber<Integer> ss = new SampleSubscriber<>();
        Flux<Integer> ints = Flux.range(1, 4)
                .doOnSubscribe(subscription -> System.out.println("Register Subscriber"))
                .doOnNext(integer -> System.out.println("Next Publish " + integer))
                .doOnComplete(() -> System.out.println("Publish Completed"));
        ints.subscribe(ss);
    }

    // generate
    @Test
    void test3() {
        Flux<String> flux = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3 * state);
                    if (state == 10) sink.complete();
                    return state + 1;
                });
        flux.subscribe();

        Flux<String> flux2 = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) sink.complete();
                    return state;
                }, (state) -> System.out.println("Final state: " + state));
        flux2.subscribe(System.out::println);
    }

    // handle
    @Test
    void test4() {
        Flux<String> alphabet = Flux.just(-1, 30, 13, 9, 20, -2, 50)
                .handle((i, sink) -> {
                    String letter = alphabet(i);
                    if (letter != null)
                        sink.next(letter);
                });

        alphabet.subscribe(System.out::println);
    }

    String alphabet(int letterNumber) {
        if (letterNumber < 1 || letterNumber > 26) {
            return null;
        }
        int letterIndexAscii = 'A' + letterNumber - 1;
        return "" + (char) letterIndexAscii;
    }
}
