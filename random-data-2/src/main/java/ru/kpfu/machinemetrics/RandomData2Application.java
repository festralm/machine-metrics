package ru.kpfu.machinemetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RandomData2Application {

    public static void main(String[] args) {
        SpringApplication.run(RandomData2Application.class, args);
    }

}
