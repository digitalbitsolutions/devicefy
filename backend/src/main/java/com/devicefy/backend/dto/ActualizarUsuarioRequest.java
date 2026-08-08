package com.devicefy.backend.dto;

import com.devicefy.backend.domain.enums.RolNombre;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActualizarUsuarioRequest {

    @NotBlank
    @Size(max = 120)
    private String nombreCompleto;

    @Email
    @Size(max = 150)
    private String email;

    private Boolean activo;

    private RolNombre rol;
}
