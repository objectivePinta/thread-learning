package com.threads.threads.demo.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;

@RestController
@RequestMapping("/math")
@Slf4j
public class MathController {

    ForkJoinPool numbersPool = new ForkJoinPool();
    @GetMapping("/{number}")
    public CompletableFuture<String> getSum(@PathVariable double number) {

        CompletableFuture<Double> numberToPower = CompletableFuture.supplyAsync(() -> numberToPower(number), numbersPool)
                .exceptionally(throwable -> {
                    log.error("Error when calculating numberToPower {}", throwable.getMessage());
                    return number * number;
                });
        CompletableFuture<Double> sqrt = CompletableFuture.supplyAsync(() -> getSqrt(number));

        return sqrt.thenCombine(numberToPower, getResultAsString());

    }

    private static BiFunction<Double, Double, String> getResultAsString() {
        return (aDouble, aDouble2) -> String.format("Sqrt:%f, Power:%f", aDouble, aDouble2);
    }

    private double numberToPower(double number) {
        log.info("calculating number to power 100 on {}", Thread.currentThread().getName());
        return Math.pow(number, 100);
    }

    static double getSqrt(double number) {
        log.info("calculating sqrt on {}", Thread.currentThread().getName());
        double cloned = number;
        for(int i = 0; i < 5; i++) {
            cloned = Math.sqrt(cloned);
        }
        return cloned;
    }
}
