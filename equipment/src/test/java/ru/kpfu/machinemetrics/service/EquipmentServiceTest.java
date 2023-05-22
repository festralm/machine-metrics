package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.kpfu.machinemetrics.config.MessageSourceConfig;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.repository.EquipmentRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.EquipmentConstants.EQUIPMENT_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest
@ImportAutoConfiguration(MessageSourceConfig.class)
public class EquipmentServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private EquipmentRepository equipmentRepositoryMock;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
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
        Page<Equipment> equipmentPage = new PageImpl<>(List.of(equipment1, equipment2));

        when(equipmentRepositoryMock.findAllByDeletedFalseOrderByName(any(Pageable.class))).thenReturn(equipmentPage);

        // when
        Page<Equipment> result = equipmentService.getAllNotDeleted(Pageable.unpaged());

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getTotalElements()).isEqualTo(2);
        softly.assertThat(result.getContent().get(0).getName()).isEqualTo(equipment1.getName());
        softly.assertThat(result.getContent().get(1).getName()).isEqualTo(equipment2.getName());
        softly.assertAll();
    }

    @Test
    public void testSearch() {
        // given
        Equipment equipment1 = Equipment.builder()
                .name("Equipment 1")
                .build();
        Equipment equipment2 = Equipment.builder()
                .name("Equipment 2")
                .build();
        Page<Equipment> equipmentPage = new PageImpl<>(List.of(equipment1, equipment2));

        when(equipmentRepositoryMock.searchAllByNameContainingOrderByName(eq("test"), any(Pageable.class)))
                .thenReturn(equipmentPage);

        // when
        Page<Equipment> result = equipmentService.search("test", Pageable.unpaged());

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getTotalElements()).isEqualTo(2);
        softly.assertThat(result.getContent().get(0).getName()).isEqualTo(equipment1.getName());
        softly.assertThat(result.getContent().get(1).getName()).isEqualTo(equipment2.getName());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        Equipment equipment = Equipment.builder()
                .name("Test Equipment")
                .build();

        Equipment savedEquipment = Equipment.builder()
                .id(1L)
                .name(equipment.getName())
                .build();

        when(equipmentRepositoryMock.save(any(Equipment.class))).thenReturn(savedEquipment);

        // when
        Equipment actualEquipment = equipmentService.save(equipment);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualEquipment.getId()).isEqualTo(savedEquipment.getId());
        softly.assertThat(actualEquipment.getName()).isEqualTo(savedEquipment.getName());
        softly.assertThat(actualEquipment.isDeleted()).isFalse();
        softly.assertAll();
    }

    @Test
    public void testGetByIdFound() {
        // given
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Equipment 1");

        when(equipmentRepositoryMock.findByIdAndDeletedFalse(equipment.getId())).thenReturn(Optional.of(equipment));

        // when
        Equipment actualEquipment = equipmentService.getById(equipment.getId());

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualEquipment.getId()).isEqualTo(equipment.getId());
        softly.assertThat(actualEquipment.getName()).isEqualTo(equipment.getName());
        softly.assertThat(actualEquipment.isDeleted()).isFalse();
        softly.assertAll();
    }

    @Test
    public void testGetByIdNotFound() {
        // given
        Long givenId = 1L;

        when(equipmentRepositoryMock.findByIdAndDeletedFalse(givenId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> equipmentService.getById(givenId));

        // then
        String expectedMessage = messageSource.getMessage(
                EQUIPMENT_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{givenId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testDeleteWithExistingEquipment() {
        // given
        Long equipmentId = 1L;

        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        equipment.setName("Test Equipment");
        equipment.setDeleted(false);

        when(equipmentRepositoryMock.findByIdAndDeletedFalse(equipmentId)).thenReturn(Optional.of(equipment));

        // when
        equipmentService.delete(equipmentId);

        // then
        verify(equipmentRepositoryMock, Mockito.times(1)).findByIdAndDeletedFalse(equipmentId);
        verify(equipmentRepositoryMock, Mockito.times(1)).save(equipment);
        verify(rabbitTemplate, times(1)).convertAndSend(eq("rk-equipment"), eq(""), eq(equipmentId));
        assertThat(equipment.isDeleted()).isTrue();
    }

    @Test
    void testDeleteWithNonExistingEquipment() {
        // given
        Long equipmentId = 1L;
        when(equipmentRepositoryMock.findByIdAndDeletedFalse(equipmentId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> equipmentService.delete(equipmentId));

        // then
        verify(equipmentRepositoryMock, Mockito.times(1)).findByIdAndDeletedFalse(equipmentId);
        verify(equipmentRepositoryMock, Mockito.never()).save(Mockito.any(Equipment.class));
        verifyNoInteractions(rabbitTemplate);
        String expectedMessage = messageSource.getMessage(
                EQUIPMENT_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{equipmentId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testEdit() {
        // given
        Long equipmentId = 1L;

        Equipment existingEquipment = Equipment.builder()
                .id(equipmentId)
                .name("Existing Equipment")
                .build();

        Equipment updatedEquipment = Equipment.builder()
                .name("Updated Equipment")
                .build();

        when(equipmentRepositoryMock.findByIdAndDeletedFalse(equipmentId)).thenReturn(Optional.of(existingEquipment));
        when(equipmentRepositoryMock.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Equipment actualEquipment = equipmentService.edit(equipmentId, updatedEquipment);

        // then
        verify(equipmentRepositoryMock).findByIdAndDeletedFalse(equipmentId);
        verify(equipmentRepositoryMock).save(existingEquipment);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualEquipment.getId()).isEqualTo(existingEquipment.getId());
        softly.assertThat(actualEquipment.getName()).isEqualTo(updatedEquipment.getName());
        softly.assertAll();
    }

    @Test
    void testEditEquipmentNotFound() {
        // given
        Long equipmentId = 1L;

        when(equipmentRepositoryMock.findByIdAndDeletedFalse(equipmentId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> equipmentService.delete(equipmentId));

        // then
        String expectedMessage = messageSource.getMessage(
                EQUIPMENT_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{equipmentId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }
}
