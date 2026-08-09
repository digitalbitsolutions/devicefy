package com.devicefy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
    private String comunidadAutonoma;
    private String provincia;
    private String telefono;
    private String email;
    private Boolean activo;
    private List<ResponsableResponse> responsables;
}
