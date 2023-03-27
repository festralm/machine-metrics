package ru.kpfu.machinemetrics.dto.equipment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentCreateDto {

    private String photoPath;

    private String inventoryNumber;

    @NotBlank(message = "{validation.equipment.name.empty}")
    private String name;

    private BigDecimal cost;

    private String source;

    private String department;

    private String responsiblePerson;

    private String status;

    private Instant receiptDate;

    private Instant lastOperationDate;
}
