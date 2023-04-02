package ru.kpfu.machinemetrics.mapper;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.kpfu.machinemetrics.dto.EquipmentCreateDto;
import ru.kpfu.machinemetrics.dto.EquipmentDetailsDto;
import ru.kpfu.machinemetrics.dto.EquipmentItemDto;
import ru.kpfu.machinemetrics.model.Equipment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@SpringBootTest
public class EquipmentMapperTest {

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Test
    public void testToEquipment() {
        // given
        EquipmentCreateDto dto =
                EquipmentCreateDto.builder().photoPath("path/to/photo").inventoryNumber("12345").name("Equipment 1").cost(new BigDecimal("1000")).source("Source 1").department("Department 1").responsiblePerson("John Doe").status("Operational").receiptDate(Instant.now()).lastOperationDate(Instant.now()).build();

        // when
        Equipment equipment = equipmentMapper.toEquipment(dto);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(equipment.getPhotoPath()).isEqualTo(dto.getPhotoPath());
        softly.assertThat(equipment.getInventoryNumber()).isEqualTo(dto.getInventoryNumber());
        softly.assertThat(equipment.getName()).isEqualTo(dto.getName());
        softly.assertThat(equipment.getCost()).isEqualTo(dto.getCost());
        softly.assertThat(equipment.getSource()).isEqualTo(dto.getSource());
        softly.assertThat(equipment.getDepartment()).isEqualTo(dto.getDepartment());
        softly.assertThat(equipment.getResponsiblePerson()).isEqualTo(dto.getResponsiblePerson());
        softly.assertThat(equipment.getStatus()).isEqualTo(dto.getStatus());
        softly.assertThat(equipment.getReceiptDate()).isEqualTo(dto.getReceiptDate());
        softly.assertThat(equipment.getLastOperationDate()).isEqualTo(dto.getLastOperationDate());
        softly.assertThat(equipment.isDeleted()).isFalse();
        softly.assertAll();
    }

    @Test
    public void testToEquipmentCreateDto() {
        // given
        Equipment equipment = Equipment.builder().id(1L).photoPath("path/to/photo").inventoryNumber("12345").name(
                "Equipment 1").cost(new BigDecimal("1000")).source("Source 1").department("Department 1").responsiblePerson("John Doe").status("Operational").receiptDate(Instant.now()).lastOperationDate(Instant.now()).build();

        // when
        EquipmentCreateDto dto = equipmentMapper.toEquipmentCreateDto(equipment);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dto.getPhotoPath()).isEqualTo(equipment.getPhotoPath());
        softly.assertThat(dto.getInventoryNumber()).isEqualTo(equipment.getInventoryNumber());
        softly.assertThat(dto.getName()).isEqualTo(equipment.getName());
        softly.assertThat(dto.getCost()).isEqualTo(equipment.getCost());
        softly.assertThat(dto.getSource()).isEqualTo(equipment.getSource());
        softly.assertThat(dto.getDepartment()).isEqualTo(equipment.getDepartment());
        softly.assertThat(dto.getResponsiblePerson()).isEqualTo(equipment.getResponsiblePerson());
        softly.assertThat(dto.getStatus()).isEqualTo(equipment.getStatus());
        softly.assertThat(dto.getReceiptDate()).isEqualTo(equipment.getReceiptDate());
        softly.assertThat(dto.getLastOperationDate()).isEqualTo(equipment.getLastOperationDate());
        softly.assertAll();
    }

    @Test
    public void testToEquipmentItemDtos() {
        // given
        Equipment equipment1 =
                Equipment.builder().id(1L).photoPath("path/to/photo1.jpg").inventoryNumber("INV-12345").name("Test " +
                        "Equipment 1").cost(new BigDecimal("10000.00")).source("Vendor A").department("Department 1").responsiblePerson("John Doe").status("Active").receiptDate(Instant.now().minusSeconds(3600)).lastOperationDate(Instant.now()).deleted(false).build();
        Equipment equipment2 =
                Equipment.builder().id(2L).photoPath("path/to/photo2.jpg").inventoryNumber("INV-67890").name("Test " +
                        "Equipment 2").cost(new BigDecimal("20000.00")).source("Vendor B").department("Department 2").responsiblePerson("Jane Doe").status("Inactive").receiptDate(Instant.now().minusSeconds(7200)).lastOperationDate(Instant.now().minusSeconds(3600)).deleted(false).build();
        List<Equipment> equipments = List.of(equipment1, equipment2);

        // when
        List<EquipmentItemDto> dtos = equipmentMapper.toEquipmentItemDtos(equipments);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(dtos).hasSize(equipments.size());

        for (int i = 0; i < dtos.size(); i++) {
            EquipmentItemDto dto = dtos.get(i);
            Equipment equipment = equipments.get(i);

            softAssertions.assertThat(dto.getId()).isEqualTo(equipment.getId());
            softAssertions.assertThat(dto.getName()).isEqualTo(equipment.getName());
        }

        softAssertions.assertAll();
    }

    @Test
    public void testToEquipmentDetailsDto() {
        // given
        Equipment equipment =
                Equipment.builder().id(1L).photoPath("path/to/photo1.jpg").inventoryNumber("INV-12345").name("Test " +
                        "Equipment 1").cost(new BigDecimal("10000.00")).source("Vendor A").department("Department 1").responsiblePerson("John Doe").status("Active").receiptDate(Instant.now().minusSeconds(3600)).lastOperationDate(Instant.now()).deleted(false).build();

        // when
        EquipmentDetailsDto dto = equipmentMapper.toEquipmentDetailsDto(equipment);

        // then
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(dto.getId()).isEqualTo(equipment.getId());
        softAssertions.assertThat(dto.getPhotoPath()).isEqualTo(equipment.getPhotoPath());
        softAssertions.assertThat(dto.getInventoryNumber()).isEqualTo(equipment.getInventoryNumber());
        softAssertions.assertThat(dto.getName()).isEqualTo(equipment.getName());
        softAssertions.assertThat(dto.getCost()).isEqualTo(equipment.getCost());
        softAssertions.assertThat(dto.getSource()).isEqualTo(equipment.getSource());
        softAssertions.assertThat(dto.getDepartment()).isEqualTo(equipment.getDepartment());
        softAssertions.assertThat(dto.getResponsiblePerson()).isEqualTo(equipment.getResponsiblePerson());
        softAssertions.assertThat(dto.getStatus()).isEqualTo(equipment.getStatus());
        softAssertions.assertThat(dto.getReceiptDate()).isEqualTo(equipment.getReceiptDate());
        softAssertions.assertThat(dto.getLastOperationDate()).isEqualTo(equipment.getLastOperationDate());


        softAssertions.assertAll();
    }
}
