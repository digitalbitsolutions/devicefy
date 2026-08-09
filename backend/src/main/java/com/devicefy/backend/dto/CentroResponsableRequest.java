package com.devicefy.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CentroResponsableRequest {

    @Size(max = 100)
    private String areaOficina;

    @Size(max = 120)
    private String nombre;

    @Size(max = 30)
    private String telefono;

    @Size(max = 150)
    private String email;
}
