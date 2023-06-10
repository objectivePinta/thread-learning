package com.threads.threads.demo;

//private thread data
/*
if thread returns to the pool with private thread local data it can end up in memory leak
 */
public class ThreadLocalHolder {

    static public ThreadLocal<String> username = new ThreadLocal<>();

}
