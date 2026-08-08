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
public class UsuarioResponse {

    private Long id;
    private String username;
    private String nombreCompleto;
    private String email;
    private Boolean activo;
    private List<String> roles;
    private List<Long> centroIds;
    private List<String> centroNombres;
}
