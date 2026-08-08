package com.devicefy.backend.dto;

import com.devicefy.backend.domain.enums.TipoEquipo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EquipoRequest {

    @Size(max = 100)
    private String hostname;

    @Size(max = 100)
    private String numeroSerie;

    @Size(max = 100)
    private String etiquetaPatrimonial;

    @Size(max = 100)
    private String fabricante;

    @Size(max = 100)
    private String modelo;

    @Size(max = 100)
    private String sistemaOperativo;

    @Size(max = 150)
    private String procesador;

    @NotNull
    private TipoEquipo tipoEquipo;

    @Size(max = 30)
    private String estado;

    private Long centroId;

    private Long ubicacionId;

    private Long usuarioAsignadoId;

    private String observaciones;

    private Boolean activo;

    private RedConfigRequest red;
}
