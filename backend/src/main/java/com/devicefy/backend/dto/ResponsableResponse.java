package com.devicefy.backend.dto;

public record ResponsableResponse(
        Long id,
        String areaOficina,
        String nombre,
        String telefono,
        String email) {
}
