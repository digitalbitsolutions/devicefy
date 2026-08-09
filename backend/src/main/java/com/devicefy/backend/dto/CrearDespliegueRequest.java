package com.devicefy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CrearDespliegueRequest {

    @NotBlank
    @Size(max = 150)
    private String nombre;

    @Size(max = 100)
    @NotBlank
    private String provincia;

    @Size(max = 100)
    @NotBlank
    private String comunidadAutonoma;

    private List<Long> centroIds = new ArrayList<>();

    private List<Long> tecnicoIds = new ArrayList<>();

    @Size(max = 30)
    private String estado = "PENDIENTE";
}
