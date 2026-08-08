package com.devicefy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CentroResponse {

    private Long id;
    private String codigo;
    private String nombre;
    private String tipo;
    private String direccion;
    private Boolean activo;
}
