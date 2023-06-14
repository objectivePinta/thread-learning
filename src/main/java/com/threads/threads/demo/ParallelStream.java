package com.threads.threads.demo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParallelStream {

    public static void main(String[] args){
        List<Integer> ints = IntStream.range(0,100).boxed().toList();
        ints.parallelStream().filter(it -> {
            System.out.println("FILTERING:"+Thread.currentThread().getName()+"-----"+it);
            //don't sleep in a commonpool
            return it % 2 == 1;
        }).sorted() // sorted & distinct runs in a single thread
                .map(n -> {
            System.out.println("MAPPING:"+Thread.currentThread().getName()+"-----"+n);
            return n * n;
        }).collect(Collectors.toList());
    }
}
