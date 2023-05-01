package ru.kpfu.machinemetrics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cron {

    @Id
    @NotBlank(message = "{validation.cron.empty}")
    private String id;

    @NotNull(message = "{validation.cron.order.empty}")
    @Column(name = "show_order")
    private Integer order;

    @NotBlank(message = "{validation.cron.name.empty}")
    private String name;
}
