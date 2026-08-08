package com.devicefy.backend.dto;

import com.devicefy.backend.domain.enums.TipoAsignacionRed;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RedConfigRequest {

    private TipoAsignacionRed tipoAsignacion;

    @Size(max = 45)
    private String ip;

    @Size(max = 45)
    private String mascara;

    @Size(max = 45)
    private String puertaEnlace;

    @Size(max = 45)
    private String dns1;

    @Size(max = 45)
    private String dns2;

    @Size(max = 150)
    private String dominio;
}
