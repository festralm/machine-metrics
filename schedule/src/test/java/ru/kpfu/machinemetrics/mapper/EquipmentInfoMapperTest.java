package ru.kpfu.machinemetrics.mapper;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.kpfu.machinemetrics.dto.EquipmentInfoCreateDto;
import ru.kpfu.machinemetrics.model.EquipmentInfo;

@SpringBootTest
@TestPropertySource("classpath:application.yml")
public class EquipmentInfoMapperTest {

    @Autowired
    private EquipmentInfoMapper equipmentInfoMapper;

    @Test
    public void testToEquipmentInfo() {
        // given
        EquipmentInfoCreateDto dto =
                EquipmentInfoCreateDto.builder()
                        .id(1L)
                        .dataServiceId(2L)
                        .cronId(1L)
                        .build();

        // when
        EquipmentInfo equipmentInfo = equipmentInfoMapper.toEquipmentInfo(dto);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(equipmentInfo.getId()).isEqualTo(dto.getId());
        softly.assertThat(equipmentInfo.getDataService().getId()).isEqualTo(dto.getDataServiceId());
        softly.assertThat(equipmentInfo.getCron().getId()).isEqualTo(dto.getCronId());
        softly.assertAll();
    }
}
