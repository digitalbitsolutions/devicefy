package com.devicefy.backend.service.impl;

import com.devicefy.backend.domain.Despliegue;
import com.devicefy.backend.domain.Usuario;
import com.devicefy.backend.dto.DashboardResponse;
import com.devicefy.backend.repository.CentroRepository;
import com.devicefy.backend.repository.DespliegueEquipoRepository;
import com.devicefy.backend.repository.DespliegueRepository;
import com.devicefy.backend.repository.EquipoRepository;
import com.devicefy.backend.repository.UbicacionRepository;
import com.devicefy.backend.repository.UsuarioRepository;
import com.devicefy.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DespliegueRepository despliegueRepository;
    private final CentroRepository centroRepository;
    private final UbicacionRepository ubicacionRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DespliegueEquipoRepository despliegueEquipoRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse obtenerResumen() {
        List<Object[]> porEstado = equipoRepository.contarPorEstado();
        List<Object[]> porTipo = equipoRepository.contarPorTipo();
        List<Object[]> porCentro = equipoRepository.contarPorCentro();

        long totalEquipos = porEstado.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        long pendientes = valor(porEstado, "PENDIENTE");
        long enProceso = valor(porEstado, "EN_PROCESO");
        long finalizados = valor(porEstado, "FINALIZADO");

        List<Despliegue> despliegues = despliegueRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre"));
        List<DashboardResponse.ProgresoProyecto> progreso = despliegues.stream().map(d -> {
            long total = despliegueEquipoRepository.countByDespliegueId(d.getId());
            long enProc = despliegueEquipoRepository.countByDespliegueIdAndEstado(d.getId(), "EN_PROCESO");
            long hechos = despliegueEquipoRepository.countByDespliegueIdAndEstado(d.getId(), "HECHO");
            return new DashboardResponse.ProgresoProyecto(
                    d.getId(), d.getNombre(), d.getProvincia(), total, enProc, hechos);
        }).toList();

        Map<Long, Long> asignados = contador(despliegueEquipoRepository.contarAsignadosPorTecnico());
        Map<Long, Long> hechosPorTecnico = contador(despliegueEquipoRepository.contarHechosPorTecnico());
        List<DashboardResponse.CargaTecnico> carga = usuarioRepository.findAll().stream()
                .filter(u -> !u.getRoles().isEmpty() && u.getRoles().stream().noneMatch(r -> r.getNombre().name().equals("ADMIN")))
                .map(u -> new DashboardResponse.CargaTecnico(
                        u.getId(), u.getNombreCompleto(),
                        asignados.getOrDefault(u.getId(), 0L),
                        hechosPorTecnico.getOrDefault(u.getId(), 0L)))
                .filter(c -> c.asignados() > 0 || c.finalizados() > 0)
                .sorted((a, b) -> Long.compare(b.asignados(), a.asignados()))
                .toList();

        DashboardResponse.Kpis kpis = new DashboardResponse.Kpis(
                despliegues.size(),
                centroRepository.count(),
                ubicacionRepository.count(),
                totalEquipos,
                usuarioRepository.count(),
                pendientes,
                enProceso,
                finalizados);

        return new DashboardResponse(
                kpis,
                conteo(porEstado),
                conteo(porTipo),
                conteo(porCentro),
                progreso,
                carga);
    }

    private long valor(List<Object[]> filas, String clave) {
        return filas.stream()
                .filter(f -> clave.equals(String.valueOf(f[0])))
                .findFirst()
                .map(f -> ((Number) f[1]).longValue())
                .orElse(0L);
    }

    private List<DashboardResponse.Conteo> conteo(List<Object[]> filas) {
        List<DashboardResponse.Conteo> lista = new ArrayList<>();
        for (Object[] f : filas) {
            lista.add(new DashboardResponse.Conteo(String.valueOf(f[0]), ((Number) f[1]).longValue()));
        }
        return lista;
    }

    private Map<Long, Long> contador(List<Object[]> filas) {
        Map<Long, Long> mapa = new HashMap<>();
        for (Object[] f : filas) {
            mapa.put(((Number) f[0]).longValue(), ((Number) f[1]).longValue());
        }
        return mapa;
    }
}
