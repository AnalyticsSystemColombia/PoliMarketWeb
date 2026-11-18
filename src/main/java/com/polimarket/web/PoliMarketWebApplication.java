package com.polimarket.web; // (o deja el package que ya tenía arriba)

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.polimarket.web")
public class PoliMarketWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoliMarketWebApplication.class, args);
    }
}
