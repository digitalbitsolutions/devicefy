package com.devicefy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionResponse {

    private Long id;
    private Long centroId;
    private String centroNombre;
    private String nombre;
    private String planta;
    private String zona;
    private Boolean activo;
}
