package ru.kpfu.machinemetrics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.kpfu.machinemetrics.model.Country;
import ru.kpfu.machinemetrics.model.Purpose;
import ru.kpfu.machinemetrics.model.Status;
import ru.kpfu.machinemetrics.model.UsageType;

import java.time.OffsetDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDetailsDto {

    private Long id;

    private String name;

    private String inventoryNumber;

    private String acquisitionSource;

    private Double cost;

    private Double initialCost;

    private Double residualCost;

    private String adName;

    private String ipAddress;

    private List<String> kfuDevelopmentProgramApplication;

    private Boolean warrantyServiceForRepresentativesOfAForeignParty;

    private List<String> kfuDevelopmentProgramPriorityDirection;

    private List<String> russiaDevelopmentPriorityDirection;

    private String area;

    private String researchObjects;

    private String indicators;

    private String additionalFeatures;

    private Purpose purpose;

    private UsageType usageType;

    private Boolean verificationRequired;

    private String type;

    private String factoryNumber;

    private Country manufacturerCountry;

    private Integer manufactureYear;

    private String manufacturer;

    private OffsetDateTime deliveryDate;

    private String supplier;

    private OffsetDateTime commissioningDate;

    private String brand;

    private Boolean providingServicesToThirdPartiesPossibility;

    private Boolean collectiveFederalCenterUse;

    private Boolean unique;

    private Boolean collectiveInterdisciplinaryCenterUse;

    private Boolean portalPublicationCardReadiness;

    private String installationLocation;

    private AddressDto address;

    private String responsiblePerson;

    private Status status;

    private OffsetDateTime lastOperationDate;

    private String photoPath;
}
