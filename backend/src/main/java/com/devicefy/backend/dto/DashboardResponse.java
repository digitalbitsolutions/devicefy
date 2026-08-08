package com.devicefy.backend.dto;

import java.util.List;

public record DashboardResponse(
        Kpis kpis,
        List<Conteo> equiposPorEstado,
        List<Conteo> equiposPorTipo,
        List<Conteo> equiposPorCentro,
        List<ProgresoProyecto> progresoProyectos,
        List<CargaTecnico> cargaTecnicos) {

    public record Kpis(
            long totalProyectos,
            long totalCentros,
            long totalUbicaciones,
            long totalEquipos,
            long totalUsuarios,
            long equiposPendientes,
            long equiposEnProceso,
            long equiposFinalizados) {
    }

    public record Conteo(String nombre, long valor) {
    }

    public record ProgresoProyecto(
            long id,
            String nombre,
            String provincia,
            long total,
            long enProceso,
            long hechos) {
    }

    public record CargaTecnico(
            long id,
            String nombre,
            long asignados,
            long finalizados) {
    }
}
