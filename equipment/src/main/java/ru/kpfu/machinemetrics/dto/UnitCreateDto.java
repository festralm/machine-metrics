package ru.kpfu.machinemetrics.dto;

import jakarta.validation.constraints.NotBlank;
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
public class UnitCreateDto {

    @NotBlank(message = "{validation.unit.name.empty}")
    private String name;

    private Long parent;
}
