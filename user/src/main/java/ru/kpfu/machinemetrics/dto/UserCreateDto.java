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
public class UserCreateDto {

    @NotBlank(message = "{validation.user.first-name.empty}")
    private String firstName;

    @NotBlank(message = "{validation.user.last-name.empty}")
    private String lastName;

    private String email;

    private String password;

    @NotBlank(message = "{validation.user.role.empty}")
    private  String roleName;
}
