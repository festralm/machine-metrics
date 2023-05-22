package ru.kpfu.machinemetrics.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String inventoryNumber;

    private String acquisitionSource;

    private Double cost;

    private Double initialCost;

    private Double residualCost;

    private String adName;

    private String ipAddress;

    @ElementCollection
    @CollectionTable(name = "kfu_development_program_application")
    @Column(name = "value")
    private List<String> kfuDevelopmentProgramApplication;

    @Column(columnDefinition = "boolean default false")
    private boolean warrantyServiceForRepresentativesOfAForeignParty;

    @ElementCollection
    @CollectionTable(name = "kfu_development_program_priority_direction")
    @Column(name = "value")
    private List<String> kfuDevelopmentProgramPriorityDirection;

    @ElementCollection
    @CollectionTable(name = "russia_development_priority_direction")
    @Column(name = "value")
    private List<String> russiaDevelopmentPriorityDirection;

    private String area;

    private String researchObjects;

    private String indicators;

    private String additionalFeatures;

    @ManyToOne
    private Purpose purpose;

    @ManyToOne
    private UsageType usageType;

    @Column(columnDefinition = "boolean default false")
    private boolean verificationRequired;

    private String type;

    private String factoryNumber;

    @ManyToOne
    private Country manufacturerCountry;

    private Integer manufactureYear;

    private String manufacturer;

    private String supplier;

    private OffsetDateTime deliveryDate;

    private OffsetDateTime commissioningDate;

    private String brand;

    @Column(columnDefinition = "boolean default false")
    private boolean providingServicesToThirdPartiesPossibility;

    @Column(columnDefinition = "boolean default false")
    private boolean collectiveFederalCenterUse;

    @Column(name = "is_unique", columnDefinition = "boolean default false")
    private boolean unique;

    @Column(columnDefinition = "boolean default false")
    private boolean collectiveInterdisciplinaryCenterUse;

    @Column(columnDefinition = "boolean default false")
    private boolean portalPublicationCardReadiness;

    private String installationLocation;

    @ManyToOne
    private Unit unit;

    private String responsiblePerson;

    @ManyToOne
    private Status status;

    private OffsetDateTime lastOperationDate;

    private String photoPath;

    @Column(columnDefinition = "boolean default false")
    private boolean deleted;
}
