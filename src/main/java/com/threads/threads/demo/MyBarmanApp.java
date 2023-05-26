package com.threads.threads.demo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

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
        pool.awaitTermination(2000, TimeUnit.SECONDS);
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

