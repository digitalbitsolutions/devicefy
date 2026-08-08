package com.devicefy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UbicacionRequest {

    @NotNull
    private Long centroId;

    @NotBlank
    @Size(max = 150)
    private String nombre;

    @Size(max = 50)
    private String planta;

    @Size(max = 50)
    private String zona;

    private Boolean activo;
}
