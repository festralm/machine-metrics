package ru.kpfu.machinemetrics.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
public class WebMvcConfigTest {

    @Autowired
    private WebMvcConfig webMvcConfig;

    @Test
    public void contextLoads() {
        assertThatCode(() -> webMvcConfig.configureMessageConverters(new ArrayList<>())).doesNotThrowAnyException();
    }
}
