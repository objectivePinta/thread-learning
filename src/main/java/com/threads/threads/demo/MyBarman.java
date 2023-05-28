package com.threads.threads.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.threads.threads.demo.ConcurrencyUtil.sleepq;

@Slf4j
@Service
public class MyBarman {
    public Beer pourBlondBeer() {

        sleepq(1000);
        log.info("End pouring");
        return new Beer();
    }

    public Beer pourDarkBeer() {
        log.info("Torn bere to ");// + requestContext.getCurrentUser()+"...");
        sleepq(1000); // imagine network call HTTP
        log.info("sfarsit pouring");
        return new Beer();
    }

    public Vodka pourVodka() {
        log.info("Torn Vodka...");
        sleepq(900); // JDBC call/ cassard
        return new Vodka();
    }

    public CompletableFuture<Dilly> mixAsync() {
        return CompletableFuture.supplyAsync(this::pourVodka)
                .thenCombine(CompletableFuture.supplyAsync(this::pourBlondBeer), Dilly::new);
    }
}
