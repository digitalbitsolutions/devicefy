package com.devicefy.backend.dto;

import com.devicefy.backend.domain.enums.TipoAsignacionRed;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProcesarEquipoRequest {

    private String ip;
    private TipoAsignacionRed tipoAsignacion;
    private String hostnameNuevo;
    private String usuarioNombre;
    private String observaciones;
    private String estado;
}
