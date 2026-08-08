package com.devicefy.backend.dto;

import java.time.Instant;

public record DespliegueEquipoResponse(
        Long id,
        Long despliegueId,
        Long equipoId,
        String hostnameActual,
        String hostnameNuevo,
        String estadoRenove,
        Integer anioRenove,
        String perfilImagen,
        String estado,
        Long tecnicoId,
        String tecnicoNombre,
        Instant fechaToma,
        String numeroSerie,
        String fabricante,
        String modelo,
        String sistemaOperativo,
        String procesador,
        String centroNombre,
        String ubicacionNombre,
        String ip) {
}
