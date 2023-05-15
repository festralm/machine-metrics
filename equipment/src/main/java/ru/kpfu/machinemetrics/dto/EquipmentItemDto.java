package ru.kpfu.machinemetrics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentItemDto {

    private Long id;

    private String name;

    private String inventoryNumber;

    private String acquisitionSource;

    private Double cost;

    private Instant deliveryDate;

    private String installationLocation;

    private String unit;

    private String responsiblePerson;

    private String status;

    private Instant lastOperationDate;

    private String photoPath;

}
