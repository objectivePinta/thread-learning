package com.threads.threads.demo.rest;

import com.threads.threads.demo.Dilly;
import com.threads.threads.demo.MyBarman;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/dilly")
@RequiredArgsConstructor
public class DillyDillyController {

    private final MyBarman myBarman;

    @GetMapping
    public CompletableFuture<Dilly> getDilly() {
        return myBarman.mixAsync();
    }
}
