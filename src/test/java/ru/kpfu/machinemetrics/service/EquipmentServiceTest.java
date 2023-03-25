package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.repository.EquipmentRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    @Test
    public void testGetAllNotDeleted() {
        // given
        Equipment equipment1 = Equipment.builder()
                .name("Equipment 1")
                .build();
        Equipment equipment2 = Equipment.builder()
                .name("Equipment 2")
                .build();
        List<Equipment> equipmentList = List.of(equipment1, equipment2);

        when(equipmentRepository.findByDeletedFalse()).thenReturn(equipmentList);

        // when
        List<Equipment> result = equipmentService.getAllNotDeleted();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getName()).isEqualTo(equipment1.getName());
        softly.assertThat(result.get(1).getName()).isEqualTo(equipment2.getName());
        softly.assertAll();
    }

    @Test
    void testSave() {
        Equipment equipment = Equipment.builder()
                .name("Test Equipment")
                .build();

        Equipment savedEquipment = Equipment.builder()
                .id(1L)
                .name(equipment.getName())
                .build();

        when(equipmentRepository.save(any(Equipment.class))).thenReturn(savedEquipment);

        Equipment actualEquipment = equipmentService.save(equipment);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualEquipment.getId()).isEqualTo(savedEquipment.getId());
        softly.assertThat(actualEquipment.getName()).isEqualTo(savedEquipment.getName());
        softly.assertThat(actualEquipment.isDeleted()).isFalse();
        softly.assertAll();
    }
}
