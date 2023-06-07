package ru.kpfu.machinemetrics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateDto {

    @NotBlank(message = "{validation.address.address.empty}")
    private String address;

    @NotNull(message = "{validation.address.unit.empty}")
    private Long unit;
}
