package com.devicefy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CentroRequest {

    @NotBlank
    @Size(max = 20)
    private String codigo;

    @NotBlank
    @Size(max = 150)
    private String nombre;

    @Size(max = 30)
    private String tipo;

    @Size(max = 255)
    private String direccion;

    private Boolean activo;
}
