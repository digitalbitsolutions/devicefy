package com.devicefy.backend.dto;

import com.devicefy.backend.domain.enums.TipoPeriferico;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerifericoResponse {

    private Long id;
    private Long equipoId;
    private TipoPeriferico tipo;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String etiquetaPatrimonial;
    private BigDecimal tamanioPulgadas;
    private Boolean activo;
}
