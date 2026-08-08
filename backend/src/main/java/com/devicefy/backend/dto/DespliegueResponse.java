package com.devicefy.backend.dto;

import java.time.Instant;

public record DespliegueResponse(
        Long id,
        String nombre,
        String ficheroNombre,
        Instant fechaImportacion,
        String estado,
        long totalEquipos,
        long enProceso,
        long hechos) {
}
