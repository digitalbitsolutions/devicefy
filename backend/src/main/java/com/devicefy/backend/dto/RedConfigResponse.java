package com.devicefy.backend.dto;

import com.devicefy.backend.domain.enums.TipoAsignacionRed;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RedConfigResponse {

    private Long id;
    private TipoAsignacionRed tipoAsignacion;
    private String ip;
    private String mascara;
    private String puertaEnlace;
    private String dns1;
    private String dns2;
    private String dominio;
    private Instant actualizadaAt;
}
