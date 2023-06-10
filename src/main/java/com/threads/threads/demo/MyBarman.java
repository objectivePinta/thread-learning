package com.threads.threads.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.threads.threads.demo.ConcurrencyUtil.sleepq;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyBarman {

    private final MyRequestContext myRequestContext;
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
        sleepq(900); // JDBC call/ cassard
        return new Vodka();
    }

    public CompletableFuture<Dilly> mixAsync() {
        String username = ThreadLocalHolder.username.get();
        log.info("Preparing a dilly-dilly to:{}", getUsernameFromRunningThread());
        return CompletableFuture.supplyAsync(this::pourVodka)
                .thenCombine(CompletableFuture.supplyAsync(this::pourBlondBeer), Dilly::new);
    }

    private String getUsernameFromRunningThread() {
        if (ThreadLocalHolder.username.get() == null) {
            return myRequestContext.getUsername()+" "+myRequestContext.getRequestId();
        } else {
            return ThreadLocalHolder.username.get();
        }
    }
}
