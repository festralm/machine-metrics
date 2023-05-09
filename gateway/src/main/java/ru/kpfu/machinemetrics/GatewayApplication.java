package ru.kpfu.machinemetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.config.properties.FrontProperties;
import ru.kpfu.machinemetrics.config.properties.JwtAuthConverterProperties;

@EnableConfigurationProperties({JwtAuthConverterProperties.class, FrontProperties.class})
@SpringBootApplication
@RestController
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
