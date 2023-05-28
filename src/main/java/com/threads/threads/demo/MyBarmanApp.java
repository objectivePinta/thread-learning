package com.threads.threads.demo;

import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static com.threads.threads.demo.ConcurrencyUtil.sleepq;


@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@Slf4j
public class MyBarmanApp implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(MyBarmanApp.class, args)
        ; // Note: .close to stop executors after CLRunner finishes
    }

    @Override
    public void run(String... args) throws Exception {
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
        combineNumbers();
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

    public void combineNumbers() {
        ForkJoinPool numbersPool = new ForkJoinPool();

        SomeRandomNumber someRandomNumber = new SomeRandomNumber();
        CompletableFuture.supplyAsync(someRandomNumber::call).exceptionally(MyBarmanApp::recover)
                .thenCombine(CompletableFuture.supplyAsync(someRandomNumber::call,numbersPool)
                        .exceptionally(MyBarmanApp::recover),(x, y) -> {
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
    public Integer call() throws Exception {
        log.info("Thread #"+Thread.currentThread().getName()+"...Task number #"+atomicLong.incrementAndGet()+"...Current number of tasks on the queue...."+threadPoolExecutor.getQueue().size());
        sleepq(500);
        log.info(Thread.currentThread().getName()+"...doing the task");
        return 2;
    }
}

@Slf4j
class MyBarman {
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
}

@Data
class Beer {
    private final String type = "beer";
}

@Data
class Vodka {
    private final String type = "stalinskaya";
}

@Value
@Slf4j
class Dilly {
    Vodka vodka;
    Beer beer;

    public Dilly(Vodka vodka, Beer beer) {
        log.info("Mixing Dilly Dilly");
        sleepq(1000);
        this.vodka = vodka;
        this.beer = beer;
    }
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