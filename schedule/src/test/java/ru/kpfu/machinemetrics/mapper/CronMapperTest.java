package ru.kpfu.machinemetrics.mapper;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.kpfu.machinemetrics.dto.CronCreateDto;
import ru.kpfu.machinemetrics.model.Cron;

@SpringBootTest
@TestPropertySource("classpath:application.yml")
public class CronMapperTest {

    @Autowired
    private CronMapper cronMapper;

    @Test
    public void testToCron() {
        // given
        CronCreateDto dto =
                CronCreateDto.builder()
                        .expression("expression 1")
                        .order(1)
                        .name("name 1")
                        .build();

        // when
        Cron cron = cronMapper.toCron(dto);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cron.getExpression()).isEqualTo(dto.getExpression());
        softly.assertThat(cron.getOrder()).isEqualTo(dto.getOrder());
        softly.assertThat(cron.getName()).isEqualTo(dto.getName());
        softly.assertAll();

    }
}
