package ru.kpfu.machinemetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.kpfu.machinemetrics.properties.AppApiProperties;
import ru.kpfu.machinemetrics.properties.JwtAuthConverterProperties;
import ru.kpfu.machinemetrics.properties.KeycloakProperties;

@SpringBootApplication
@EnableConfigurationProperties({KeycloakProperties.class, AppApiProperties.class, JwtAuthConverterProperties.class})
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

}
