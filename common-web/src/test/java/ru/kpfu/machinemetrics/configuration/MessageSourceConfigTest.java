package ru.kpfu.machinemetrics.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
public class MessageSourceConfigTest {

    @Autowired
    private MessageSourceConfig messageSourceConfig;

    @Test
    public void contextLoads() {
        assertThatCode(() -> messageSourceConfig.messageSource()).doesNotThrowAnyException();
    }
}
