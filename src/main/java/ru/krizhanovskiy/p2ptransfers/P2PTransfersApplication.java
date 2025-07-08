package ru.krizhanovskiy.p2ptransfers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class P2PTransfersApplication {

    public static void main(String[] args) {
        SpringApplication.run(P2PTransfersApplication.class, args);
    }

}
