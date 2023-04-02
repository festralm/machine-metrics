package ru.kpfu.machinemetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RandomDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(RandomDataApplication.class, args);
    }

}
