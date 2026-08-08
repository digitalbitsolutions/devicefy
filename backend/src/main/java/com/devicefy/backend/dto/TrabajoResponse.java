package com.devicefy.backend.dto;

public record TrabajoResponse(
        Long despliegueEquipoId,
        Long despliegueId,
        String despliegueNombre,
        Long equipoId,
        String hostname,
        String numeroSerie,
        String fabricante,
        String modelo,
        String sistemaOperativo,
        Long centroId,
        String centroNombre,
        String ubicacionNombre,
        String estadoRenove,
        Integer anioRenove,
        String perfilImagen,
        String ip,
        String estado,
        String tecnicoNombre) {
}
