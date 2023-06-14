package com.threads.threads.demo.rest;

import com.threads.threads.demo.Dilly;
import com.threads.threads.demo.MyBarman;
import com.threads.threads.demo.MyRequestContext;
import com.threads.threads.demo.ThreadLocalHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/dilly")
@RequiredArgsConstructor
@Slf4j
public class DillyDillyController {

    private final MyBarman myBarman;

    private final MyRequestContext myRequestContext;

    @GetMapping
    public CompletableFuture<Dilly> getDilly() {
        try{
        ThreadLocalHolder.username = ThreadLocal.withInitial(this::getRandomName);
        return myBarman.mixAsync();
        } finally {
            ThreadLocalHolder.username.remove();
        }
        //this thread leaves to connection pool
    }

    @GetMapping("/req")
    public CompletableFuture<Dilly> getDillyWithSpringRequestContext() {
        myRequestContext.setUsername(getRandomName());
        myRequestContext.setRequestId(UUID.randomUUID().toString());
        return myBarman.mixAsync();
        //this thread leaves to connection pool
    }



    String getRandomName() {
        Integer random = new Random().nextInt(0,4);
        return switch (random) {
            case 0 -> "Andrei";
            case 1 -> "Michael";
            case 2 -> "Minnie";
            case 3 -> "Loompa";
            default -> "Default Joe";
        };
    }
}
