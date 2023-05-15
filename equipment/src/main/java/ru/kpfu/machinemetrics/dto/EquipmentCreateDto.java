package ru.kpfu.machinemetrics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentCreateDto {

    @NotBlank(message = "{validation.equipment.name.empty}")
    private String name;

    @NotBlank(message = "{validation.equipment.inventory-number.empty}")
    private String inventoryNumber;

    @NotBlank(message = "{validation.equipment.acquisition-source.empty}")
    private String acquisitionSource;

    @NotNull(message = "{validation.equipment.cost.empty}")
    private Double cost;

    @NotNull(message = "{validation.equipment.initial-cost.empty}")
    private Double initialCost;

    @NotNull(message = "{validation.equipment.residual-cost.empty}")
    private Double residualCost;

    @NotBlank(message = "{validation.equipment.ad-name.empty}")
    private String adName;

    @NotBlank(message = "{validation.equipment.ip-address.empty}")
    private String ipAddress;

    private List<String> kfuDevelopmentProgramApplication;

    private Boolean warrantyServiceForRepresentativesOfAForeignParty;

    private List<String> kfuDevelopmentProgramPriorityDirection;

    private List<String> russiaDevelopmentPriorityDirection;

    private String area;

    private String researchObjects;

    private String indicators;

    private String additionalFeatures;

    private Long purpose;

    private Long usageType;

    private Boolean verificationRequired;

    private String type;

    private String factoryNumber;

    private Long manufacturerCountry;

    private Integer manufactureYear;

    private String manufacturer;

    private Instant deliveryDate;

    private String supplier;

    private Instant commissioningDate;

    private String brand;

    private Boolean providingServicesToThirdPartiesPossibility;

    private Boolean collectiveFederalCenterUse;

    private Boolean unique;

    private Boolean collectiveInterdisciplinaryCenterUse;

    private Boolean portalPublicationCardReadiness;

    private String installationLocation;

    private Long unit;

    private String responsiblePerson;

    private Long status;

    private String photoPath;
}
