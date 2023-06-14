package com.threads.threads.demo.rest;

import com.threads.threads.demo.Beer;
import com.threads.threads.demo.MyBarman;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/spring")
@RequiredArgsConstructor
public class SpringExecutors {

    private final ThreadPoolTaskExecutor myThreadPoolTaskExecutor;
    private final MyBarman myBarman;

    @GetMapping
    public CompletableFuture<List<Beer>> submitTasks() throws ExecutionException, InterruptedException {
        List<CompletableFuture<Beer>> listOfFutures = IntStream.range(0, 100).mapToObj(idx ->
                CompletableFuture.supplyAsync(myBarman::pourBlondBeer, myThreadPoolTaskExecutor)
        ).toList();
        CompletableFuture<List<Beer>> futureOfList = CompletableFuture
                .allOf(listOfFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> listOfFutures.stream().map(CompletableFuture::join)
                        .collect(Collectors.toList()));
        futureOfList.get();
        return futureOfList;
    }
}
