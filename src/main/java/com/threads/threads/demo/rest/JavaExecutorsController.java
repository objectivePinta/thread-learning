package com.threads.threads.demo.rest;

import static com.threads.threads.demo.ConcurrencyUtil.sleepq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/executors")
@Slf4j
public class JavaExecutorsController {

    //infinite numbers of threads
    ExecutorService executor = Executors.newCachedThreadPool();

    @GetMapping
    public void get() {
        IntStream.range(1, 10000000).forEach(idx -> executor.submit(() -> {
            sleepq(500);
            log.info(Thread.currentThread().getName());
        }));
    }

}