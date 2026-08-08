package com.devicefy.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActualizarDespliegueRequest {

    @Size(max = 150)
    private String nombre;

    @Size(max = 100)
    private String provincia;

    private String estado;
}
