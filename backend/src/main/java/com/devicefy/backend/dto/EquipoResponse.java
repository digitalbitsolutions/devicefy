package com.devicefy.backend.dto;

import com.devicefy.backend.domain.enums.TipoEquipo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipoResponse {

    private Long id;
    private String hostname;
    private String numeroSerie;
    private String etiquetaPatrimonial;
    private String fabricante;
    private String modelo;
    private String sistemaOperativo;
    private String procesador;
    private TipoEquipo tipoEquipo;
    private String estado;
    private Long centroId;
    private String centroNombre;
    private Long ubicacionId;
    private String ubicacionNombre;
    private Long usuarioAsignadoId;
    private String usuarioAsignadoNombre;
    private String observaciones;
    private Boolean activo;
    private RedConfigResponse red;
    private List<PerifericoResponse> perifericos;
}
