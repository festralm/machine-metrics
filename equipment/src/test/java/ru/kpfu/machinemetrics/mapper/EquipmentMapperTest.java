package ru.kpfu.machinemetrics.mapper;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.kpfu.machinemetrics.dto.EquipmentCreateDto;
import ru.kpfu.machinemetrics.dto.EquipmentDetailsDto;
import ru.kpfu.machinemetrics.dto.EquipmentItemDto;
import ru.kpfu.machinemetrics.model.Country;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.model.Purpose;
import ru.kpfu.machinemetrics.model.Status;
import ru.kpfu.machinemetrics.model.Unit;
import ru.kpfu.machinemetrics.model.UsageType;

import java.time.Instant;
import java.util.List;

@SpringBootTest
public class EquipmentMapperTest {

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Test
    public void testToEquipment() {
        // given
        EquipmentCreateDto dto = EquipmentCreateDto.builder()
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .initialCost(200.0)
                .residualCost(300.0)
                .adName("adName 1")
                .ipAddress("ipAddress 1")
                .kfuDevelopmentProgramApplication(
                        List.of("kfuDevelopmentProgramApplication 1", "kfuDevelopmentProgramApplication 2")
                )
                .warrantyServiceForRepresentativesOfAForeignParty(true)
                .kfuDevelopmentProgramPriorityDirection(
                        List.of("kfuDevelopmentProgramPriorityDirection 1", "kfuDevelopmentProgramPriorityDirection 2")
                )
                .russiaDevelopmentPriorityDirection(
                        List.of("russiaDevelopmentPriorityDirection 1", "russiaDevelopmentPriorityDirection 2")
                )
                .area("area 1")
                .researchObjects("researchObjects 1")
                .indicators("indicators 1")
                .additionalFeatures("additionalFeatures 1")
                .purpose(1L)
                .usageType(1L)
                .verificationRequired(false)
                .type("type 1")
                .factoryNumber("factoryNumber 1")
                .manufacturerCountry(1L)
                .manufactureYear(2000)
                .manufacturer("manufacturer 1")
                .deliveryDate(Instant.now())
                .supplier("supplier 1")
                .commissioningDate(Instant.now())
                .brand("brand 1")
                .providingServicesToThirdPartiesPossibility(true)
                .collectiveFederalCenterUse(false)
                .unique(true)
                .collectiveInterdisciplinaryCenterUse(false)
                .portalPublicationCardReadiness(true)
                .installationLocation("installationLocation 1")
                .unit(1L)
                .responsiblePerson("responsiblePerson 1")
                .status(1L)
                .photoPath("photoPath 1")
                .build();

        // when
        Equipment equipment = equipmentMapper.toEquipment(dto);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(equipment.getName()).isEqualTo(dto.getName());
        softly.assertThat(equipment.getInventoryNumber()).isEqualTo(dto.getInventoryNumber());
        softly.assertThat(equipment.getAcquisitionSource()).isEqualTo(dto.getAcquisitionSource());
        softly.assertThat(equipment.getCost()).isEqualTo(dto.getCost());
        softly.assertThat(equipment.getInitialCost()).isEqualTo(dto.getInitialCost());
        softly.assertThat(equipment.getResidualCost()).isEqualTo(dto.getResidualCost());
        softly.assertThat(equipment.getAdName()).isEqualTo(dto.getAdName());
        softly.assertThat(equipment.getIpAddress()).isEqualTo(dto.getIpAddress());
        softly.assertThat(equipment.getKfuDevelopmentProgramApplication()).containsExactlyInAnyOrderElementsOf(
                dto.getKfuDevelopmentProgramApplication()
        );
        softly.assertThat(equipment.isWarrantyServiceForRepresentativesOfAForeignParty())
                .isEqualTo(dto.getWarrantyServiceForRepresentativesOfAForeignParty());
        softly.assertThat(equipment.getKfuDevelopmentProgramPriorityDirection()).containsExactlyInAnyOrderElementsOf(
                dto.getKfuDevelopmentProgramPriorityDirection()
        );
        softly.assertThat(equipment.getRussiaDevelopmentPriorityDirection()).containsExactlyInAnyOrderElementsOf(
                dto.getRussiaDevelopmentPriorityDirection()
        );
        softly.assertThat(equipment.getArea()).isEqualTo(dto.getArea());
        softly.assertThat(equipment.getResearchObjects()).isEqualTo(dto.getResearchObjects());
        softly.assertThat(equipment.getIndicators()).isEqualTo(dto.getIndicators());
        softly.assertThat(equipment.getAdditionalFeatures()).isEqualTo(dto.getAdditionalFeatures());
        softly.assertThat(equipment.getPurpose().getId()).isEqualTo(dto.getPurpose());
        softly.assertThat(equipment.getUsageType().getId()).isEqualTo(dto.getUsageType());
        softly.assertThat(equipment.isVerificationRequired()).isEqualTo(dto.getVerificationRequired());
        softly.assertThat(equipment.getType()).isEqualTo(dto.getType());
        softly.assertThat(equipment.getFactoryNumber()).isEqualTo(dto.getFactoryNumber());
        softly.assertThat(equipment.getManufacturerCountry().getId()).isEqualTo(dto.getManufacturerCountry());
        softly.assertThat(equipment.getManufactureYear()).isEqualTo(dto.getManufactureYear());
        softly.assertThat(equipment.getManufacturer()).isEqualTo(dto.getManufacturer());
        softly.assertThat(equipment.getDeliveryDate()).isEqualTo(dto.getDeliveryDate());
        softly.assertThat(equipment.getSupplier()).isEqualTo(dto.getSupplier());
        softly.assertThat(equipment.getCommissioningDate()).isEqualTo(dto.getCommissioningDate());
        softly.assertThat(equipment.getBrand()).isEqualTo(dto.getBrand());
        softly.assertThat(equipment.isProvidingServicesToThirdPartiesPossibility())
                .isEqualTo(dto.getProvidingServicesToThirdPartiesPossibility());
        softly.assertThat(equipment.isCollectiveFederalCenterUse()).isEqualTo(dto.getCollectiveFederalCenterUse());
        softly.assertThat(equipment.isUnique()).isEqualTo(dto.getUnique());
        softly.assertThat(equipment.isCollectiveInterdisciplinaryCenterUse())
                .isEqualTo(dto.getCollectiveInterdisciplinaryCenterUse());
        softly.assertThat(equipment.isPortalPublicationCardReadiness())
                .isEqualTo(dto.getPortalPublicationCardReadiness());
        softly.assertThat(equipment.getInstallationLocation()).isEqualTo(dto.getInstallationLocation());
        softly.assertThat(equipment.getUnit().getId()).isEqualTo(dto.getUnit());
        softly.assertThat(equipment.getResponsiblePerson()).isEqualTo(dto.getResponsiblePerson());
        softly.assertThat(equipment.getStatus().getId()).isEqualTo(dto.getStatus());
        softly.assertThat(equipment.getLastOperationDate()).isNull();
        softly.assertThat(equipment.getPhotoPath()).isEqualTo(dto.getPhotoPath());
        softly.assertThat(equipment.isDeleted()).isFalse();
        softly.assertAll();
    }

    @Test
    public void testToEquipmentDetailsDto() {
        // given
        Equipment equipment = Equipment.builder()
                .id(1L)
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .initialCost(200.0)
                .residualCost(300.0)
                .adName("adName 1")
                .ipAddress("ipAddress 1")
                .kfuDevelopmentProgramApplication(
                        List.of("kfuDevelopmentProgramApplication 1", "kfuDevelopmentProgramApplication 2")
                )
                .warrantyServiceForRepresentativesOfAForeignParty(true)
                .kfuDevelopmentProgramPriorityDirection(
                        List.of("kfuDevelopmentProgramPriorityDirection 1", "kfuDevelopmentProgramPriorityDirection 2")
                )
                .russiaDevelopmentPriorityDirection(
                        List.of("russiaDevelopmentPriorityDirection 1", "russiaDevelopmentPriorityDirection 2")
                )
                .area("area 1")
                .researchObjects("researchObjects 1")
                .indicators("indicators 1")
                .additionalFeatures("additionalFeatures 1")
                .purpose(Purpose.builder().id(1L).name("purpose 1").build())
                .usageType(UsageType.builder().id(1L).name("usageType 1").build())
                .verificationRequired(false)
                .type("type 1")
                .factoryNumber("factoryNumber 1")
                .manufacturerCountry(Country.builder().id(1L).name("manufacturerCountry 1").build())
                .manufactureYear(2000)
                .manufacturer("manufacturer 1")
                .deliveryDate(Instant.now())
                .supplier("supplier 1")
                .commissioningDate(Instant.now())
                .brand("brand 1")
                .providingServicesToThirdPartiesPossibility(true)
                .collectiveFederalCenterUse(false)
                .unique(true)
                .collectiveInterdisciplinaryCenterUse(false)
                .portalPublicationCardReadiness(true)
                .installationLocation("installationLocation 1")
                .unit(Unit.builder().id(1L).name("unit 1").build())
                .responsiblePerson("responsiblePerson 1")
                .status(Status.builder().id(1L).name("status 1").build())
                .photoPath("photoPath 1")
                .deleted(false)
                .build();

        // when
        EquipmentDetailsDto dto = equipmentMapper.toEquipmentDetailsDto(equipment);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dto.getId()).isEqualTo(equipment.getId());
        softly.assertThat(dto.getName()).isEqualTo(equipment.getName());
        softly.assertThat(dto.getInventoryNumber()).isEqualTo(equipment.getInventoryNumber());
        softly.assertThat(dto.getAcquisitionSource()).isEqualTo(equipment.getAcquisitionSource());
        softly.assertThat(dto.getCost()).isEqualTo(equipment.getCost());
        softly.assertThat(dto.getInitialCost()).isEqualTo(equipment.getInitialCost());
        softly.assertThat(dto.getResidualCost()).isEqualTo(equipment.getResidualCost());
        softly.assertThat(dto.getAdName()).isEqualTo(equipment.getAdName());
        softly.assertThat(dto.getIpAddress()).isEqualTo(equipment.getIpAddress());
        softly.assertThat(dto.getKfuDevelopmentProgramApplication()).containsExactlyInAnyOrderElementsOf(
                equipment.getKfuDevelopmentProgramApplication()
        );
        softly.assertThat(dto.getWarrantyServiceForRepresentativesOfAForeignParty())
                .isEqualTo(equipment.isWarrantyServiceForRepresentativesOfAForeignParty());
        softly.assertThat(dto.getKfuDevelopmentProgramPriorityDirection()).containsExactlyInAnyOrderElementsOf(
                equipment.getKfuDevelopmentProgramPriorityDirection()
        );
        softly.assertThat(dto.getRussiaDevelopmentPriorityDirection()).containsExactlyInAnyOrderElementsOf(
                equipment.getRussiaDevelopmentPriorityDirection()
        );
        softly.assertThat(dto.getArea()).isEqualTo(equipment.getArea());
        softly.assertThat(dto.getResearchObjects()).isEqualTo(equipment.getResearchObjects());
        softly.assertThat(dto.getIndicators()).isEqualTo(equipment.getIndicators());
        softly.assertThat(dto.getAdditionalFeatures()).isEqualTo(equipment.getAdditionalFeatures());
        softly.assertThat(dto.getPurpose()).isEqualTo(equipment.getPurpose());
        softly.assertThat(dto.getUsageType()).isEqualTo(equipment.getUsageType());
        softly.assertThat(dto.getVerificationRequired()).isEqualTo(equipment.isVerificationRequired());
        softly.assertThat(dto.getType()).isEqualTo(equipment.getType());
        softly.assertThat(dto.getFactoryNumber()).isEqualTo(equipment.getFactoryNumber());
        softly.assertThat(dto.getManufacturerCountry()).isEqualTo(equipment.getManufacturerCountry());
        softly.assertThat(dto.getManufactureYear()).isEqualTo(equipment.getManufactureYear());
        softly.assertThat(dto.getManufacturer()).isEqualTo(equipment.getManufacturer());
        softly.assertThat(dto.getDeliveryDate()).isEqualTo(equipment.getDeliveryDate());
        softly.assertThat(dto.getSupplier()).isEqualTo(equipment.getSupplier());
        softly.assertThat(dto.getCommissioningDate()).isEqualTo(equipment.getCommissioningDate());
        softly.assertThat(dto.getBrand()).isEqualTo(equipment.getBrand());
        softly.assertThat(dto.getProvidingServicesToThirdPartiesPossibility())
                .isEqualTo(equipment.isProvidingServicesToThirdPartiesPossibility());
        softly.assertThat(dto.getCollectiveFederalCenterUse()).isEqualTo(equipment.isCollectiveFederalCenterUse());
        softly.assertThat(dto.getUnique()).isEqualTo(equipment.isUnique());
        softly.assertThat(dto.getCollectiveInterdisciplinaryCenterUse())
                .isEqualTo(equipment.isCollectiveInterdisciplinaryCenterUse());
        softly.assertThat(dto.getPortalPublicationCardReadiness())
                .isEqualTo(equipment.isPortalPublicationCardReadiness());
        softly.assertThat(dto.getInstallationLocation()).isEqualTo(equipment.getInstallationLocation());
        softly.assertThat(dto.getUnit().getName()).isEqualTo(equipment.getUnit().getName());
        softly.assertThat(dto.getUnit().getId()).isEqualTo(equipment.getUnit().getId());
        softly.assertThat(dto.getResponsiblePerson()).isEqualTo(equipment.getResponsiblePerson());
        softly.assertThat(dto.getStatus()).isEqualTo(equipment.getStatus());
        softly.assertThat(dto.getLastOperationDate()).isNull();
        softly.assertThat(dto.getPhotoPath()).isEqualTo(equipment.getPhotoPath());
        softly.assertAll();
    }

    @Test
    public void testToEquipmentItemDtos() {
        // given
        Equipment equipment1 = Equipment.builder()
                .id(1L)
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .deliveryDate(Instant.now())
                .installationLocation("installationLocation 1")
                .unit(Unit.builder().id(1L).name("unit 1").build())
                .responsiblePerson("responsiblePerson 1")
                .status(Status.builder().id(1L).name("status 1").build())
                .lastOperationDate(Instant.now())
                .photoPath("photoPath 1")
                .deleted(false)
                .build();
        Equipment equipment2 = Equipment.builder()
                .id(2L)
                .name("name 2")
                .inventoryNumber("inventoryNumber 2")
                .acquisitionSource("acquisitionSource 2")
                .cost(200.0)
                .deliveryDate(Instant.now())
                .installationLocation("installationLocation 2")
                .unit(Unit.builder().id(1L).name("unit 2").build())
                .responsiblePerson("responsiblePerson 2")
                .status(Status.builder().id(1L).name("status 2").build())
                .photoPath("photoPath 2")
                .lastOperationDate(Instant.now())
                .deleted(false)
                .build();
        Page<Equipment> equipments = new PageImpl<>(List.of(equipment1, equipment2));

        // when
        Page<EquipmentItemDto> dtos = equipmentMapper.toEquipmentItemDtos(equipments);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dtos.getTotalElements()).isEqualTo(equipments.getTotalElements());

        for (int i = 0; i < dtos.getTotalElements(); i++) {
            EquipmentItemDto dto = dtos.getContent().get(i);
            Equipment equipment = equipments.getContent().get(i);

            softly.assertThat(dto.getId()).isEqualTo(equipment.getId());
            softly.assertThat(dto.getName()).isEqualTo(equipment.getName());
            softly.assertThat(dto.getInventoryNumber()).isEqualTo(equipment.getInventoryNumber());
            softly.assertThat(dto.getAcquisitionSource()).isEqualTo(equipment.getAcquisitionSource());
            softly.assertThat(dto.getCost()).isEqualTo(equipment.getCost());
            softly.assertThat(dto.getDeliveryDate()).isEqualTo(equipment.getDeliveryDate());
            softly.assertThat(dto.getInstallationLocation()).isEqualTo(equipment.getInstallationLocation());
            softly.assertThat(dto.getUnit().getId()).isEqualTo(equipment.getUnit().getId());
            softly.assertThat(dto.getUnit().getName()).isEqualTo(equipment.getUnit().getName());
            softly.assertThat(dto.getResponsiblePerson()).isEqualTo(equipment.getResponsiblePerson());
            softly.assertThat(dto.getStatus()).isEqualTo(equipment.getStatus().getName());
            softly.assertThat(dto.getLastOperationDate()).isEqualTo(equipment.getLastOperationDate());
            softly.assertThat(dto.getPhotoPath()).isEqualTo(equipment.getPhotoPath());
        }

        softly.assertAll();
    }
}
