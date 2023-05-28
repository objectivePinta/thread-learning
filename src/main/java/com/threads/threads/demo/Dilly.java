package com.threads.threads.demo;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import static com.threads.threads.demo.ConcurrencyUtil.sleepq;

@Value
@Slf4j
public
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
