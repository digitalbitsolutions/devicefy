package com.devicefy.backend.dto;

import java.time.Instant;
import java.util.List;

public record DespliegueResponse(
        Long id,
        String nombre,
        String provincia,
        String ficheroNombre,
        Instant fechaImportacion,
        String estado,
        long totalEquipos,
        long enProceso,
        long hechos,
        List<Long> tecnicoIds,
        List<String> tecnicoNombres) {
}
