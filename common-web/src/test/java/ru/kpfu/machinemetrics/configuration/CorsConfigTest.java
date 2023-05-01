package ru.kpfu.machinemetrics.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest("app.front.url=localhost")
public class CorsConfigTest {

    @Autowired
    private CorsConfig corsConfig;

    @Test
    public void contextLoads() {
        assertThatCode(() -> corsConfig.corsFilter()).doesNotThrowAnyException();
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}
