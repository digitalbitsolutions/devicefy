package com.devicefy.backend.dto;

import com.devicefy.backend.domain.enums.TipoPeriferico;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PerifericoRequest {

    private Long equipoId;

    @NotNull
    private TipoPeriferico tipo;

    @Size(max = 100)
    private String marca;

    @Size(max = 100)
    private String modelo;

    @Size(max = 100)
    private String numeroSerie;

    @Size(max = 100)
    private String etiquetaPatrimonial;

    private BigDecimal tamanioPulgadas;

    private Boolean activo;
}
