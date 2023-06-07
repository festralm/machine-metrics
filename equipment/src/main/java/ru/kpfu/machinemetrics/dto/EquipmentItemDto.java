package ru.kpfu.machinemetrics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

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

    private OffsetDateTime deliveryDate;

    private String installationLocation;

    private AddressDto address;

    private String responsiblePerson;

    private String status;

    private OffsetDateTime lastOperationDate;

    private String photoPath;

}
