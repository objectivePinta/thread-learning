package com.threads.threads.demo;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Data
@Component
@Scope(scopeName = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
/*
For unit tests replace this with Singleton
 */
public class MyRequestContext {
    private String username;
    private String requestId;
}
