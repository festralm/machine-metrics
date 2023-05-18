package ru.kpfu.machinemetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.kpfu.machinemetrics.properties.AppProperties;

@EnableConfigurationProperties(AppProperties.class)
@SpringBootApplication
public class StatisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatisticsApplication.class, args);
    }

}
