package com.threads.threads.demo;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static com.threads.threads.demo.ConcurrencyUtil.sleepq;


@SpringBootApplication
@Slf4j
@EnableAsync
@RequiredArgsConstructor
public class MyBarmanApp implements CommandLineRunner {

    private final NumberGenerator numberGenerator;

    public static void main(String[] args) {
        SpringApplication.run(MyBarmanApp.class, args); // Note: .close to stop executors after CLRunner finishes
    }

    @Override
    public void run(String... args) throws Exception {

        //Check @Async is not running on main
        //c.threads.threads.demo.NumberGenerator   : @Async is running on thread:task-1
        numberGenerator.combineNumbers();

        /* The optimal number of running threads is equal with the number of logical CPU cores
        *  if there are more threads than cores,
        *  the threads will fight over the cpu core (thread contention, lots of context switching).
        *  The analogy with having 4 shovels and asking how many workers should you hire if you have 4 shovels?
        *
        * Formula for number of threads = no of cpus / (1 - blocking factor) - Goetz & Subramian
        * eg. bf = 0.8 => 8 / 1 - 0.8 = 8 * 2 = 16
        *  */
        log.info("available processors:"+Runtime.getRuntime().availableProcessors());
        MyBarman myBarman = new MyBarman();
        ExecutorService pool = Executors.newFixedThreadPool(2);

        /* max 4 active threads, and 2 tasks waiting in the queue. Should crash when 7 tasks are submitted */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                4,
                5,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(4);
        threadPoolTaskExecutor.setTaskDecorator(new TaskDecorator() {
            @Override
            public Runnable decorate(Runnable original) {
                //this is still on main thread
                long t0 = System.currentTimeMillis();
                return () -> {
                    long t1 = System.currentTimeMillis();
                    // time waiting in queue
                    log.info("XXXX {} seconds on thread {}", (t1-t0)/1000, Thread.currentThread().getName());
                    original.run();
                };
            }
        });
        threadPoolTaskExecutor.initialize();

        IntStream.range(1, 5).forEach(idx -> {
            threadPoolTaskExecutor.submit(() -> {
                sleepq(500);
                log.info("#### I'm here:"+Thread.currentThread().getName());
            });
        });

        long start = System.currentTimeMillis();
        IntStream.range(1, 17).forEach(idx ->
                threadPoolExecutor.submit(new MyTask(threadPoolExecutor))
        );
        Future<Beer> futureBeer = pool.submit(myBarman::pourBlondBeer);
        Future<Vodka> futureVodka = pool.submit(myBarman::pourVodka);

        Beer beer = futureBeer.get();
        Vodka vodka = futureVodka.get();

//        Beer beer = myBarman.pourBlondBeer();
//        Vodka vodka = myBarman.pourVodka();
        long total = System.currentTimeMillis() - start;
        log.info("Am terminat:" + total);
        pool.shutdown();
        completableFuture(pool, myBarman);
        pool.awaitTermination(2000, TimeUnit.SECONDS);

    }


    public void completableFuture(ExecutorService pool, MyBarman myBarman) throws ExecutionException, InterruptedException {
        /*
        CF allows to create asyncronous flows that we program and leave to run by themselves
         */
        log.info("Entered completable future world");
        CompletableFuture<Void> payForDrinks = CompletableFuture.runAsync(() -> pay("Drinks"));
        CompletableFuture<Vodka> vodkaFuture = CompletableFuture.supplyAsync(myBarman::pourVodka)
                .thenApply(vodka -> {System.out.println("Add ice # # #"); return vodka;});
        CompletableFuture<Beer> beerCompletableFuture = CompletableFuture.supplyAsync(myBarman::pourDarkBeer);

        CompletableFuture<Beer> paidBeer = payForDrinks.thenApply(v -> myBarman.pourBlondBeer());
        CompletableFuture<Vodka> paidVodka = payForDrinks.thenApply(v -> myBarman.pourVodka());

        CompletableFuture<Dilly> futureDilly = vodkaFuture.thenCombine(beerCompletableFuture, (vodka,beer) -> {
            return new Dilly(vodka, beer);
        });
        futureDilly.thenAccept(dilly -> log.info(dilly.toString()));
        log.info("Exiting completable future world");

        /* it uses an internal common pool*/
        Vodka vodka = vodkaFuture.get(); // don't get on CompletableFuture
        // execute a c
    }


    void pay(String what) {
    log.info("You're paying for "+ what);
    }
}
@Slf4j
class MyTask implements Callable<Integer> {
    ThreadPoolExecutor threadPoolExecutor;

    static AtomicLong atomicLong = new AtomicLong(0);

    public MyTask(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public Integer call() {
        log.info("Thread #"+Thread.currentThread().getName()+"...Task number #"+atomicLong.incrementAndGet()+"...Current number of tasks on the queue...."+threadPoolExecutor.getQueue().size());
        sleepq(500);
        log.info(Thread.currentThread().getName()+"...doing the task");
        return 2;
    }
}

@Data
class Beer {
    private final String type = "beer";
}

@Data
class Vodka {
    private final String type = "stalinskaya";
}

@Slf4j
class SomeRandomNumber implements Callable<Long> {
    public static long start = 0;
    @Override
    public Long call()  {


        Random random = new Random();
        long nextLong = random.nextLong(1, 100);
        log.info("### thread:"+Thread.currentThread().getName()+" will return:"+nextLong);
        sleepq(1500);
        if (start != 0) {
            long next = start;
            start = System.currentTimeMillis() - next;
            log.info("Took:"+start);
        } else {
            start = System.currentTimeMillis();
            throw new IllegalStateException("No more numbers");
        }
        return nextLong;
    }
}

@Slf4j
@Service
@RequiredArgsConstructor
class NumberGenerator{


        @Async
        //@Async methods are run by spring on a different thread
        public CompletableFuture<Long> combineNumbers() {

            log.info("@Async is running on thread:"+Thread.currentThread().getName());
        /*
        FJP = a thread pool optimized for divide et impera tasks
        - resubmitting new tasks back
         */
            ForkJoinPool numbersPool = new ForkJoinPool();
            SomeRandomNumber someRandomNumber = new SomeRandomNumber();
            return CompletableFuture.supplyAsync(someRandomNumber::call).exceptionally(NumberGenerator::recover)
                    .thenCombine(CompletableFuture.supplyAsync(someRandomNumber::call,numbersPool)
                            .exceptionally(NumberGenerator::recover),(x, y) -> {
                        long suma = x + y;
                        log.info("suma="+ suma);
                        return suma;
                    }).exceptionally(throwable -> {
                        log.info(throwable.getMessage());
                        //recover
                        if (throwable instanceof IllegalStateException) {
                            Random random = new Random();
                            return random.nextLong(1, 100);
                        } else {
                            return null;
                        }
                    });
        }


    private static long recover(Throwable t) {
        if (t.getCause() instanceof IllegalStateException) {
            Random random = new Random();
            return random.nextLong(1, 100);
        } else {
            throw new RuntimeException(t);
        }
    }
}
