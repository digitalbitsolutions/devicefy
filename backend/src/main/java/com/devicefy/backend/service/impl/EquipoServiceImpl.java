package com.devicefy.backend.service.impl;

import com.devicefy.backend.domain.Centro;
import com.devicefy.backend.domain.DespliegueEquipo;
import com.devicefy.backend.domain.Equipo;
import com.devicefy.backend.domain.RedConfig;
import com.devicefy.backend.domain.Ubicacion;
import com.devicefy.backend.domain.Usuario;
import com.devicefy.backend.domain.UsuarioAsignado;
import com.devicefy.backend.domain.enums.TipoAsignacionRed;
import com.devicefy.backend.dto.EquipoRequest;
import com.devicefy.backend.dto.EquipoResponse;
import com.devicefy.backend.dto.PerifericoResponse;
import com.devicefy.backend.dto.RedConfigRequest;
import com.devicefy.backend.dto.RedConfigResponse;
import com.devicefy.backend.repository.CentroRepository;
import com.devicefy.backend.repository.DespliegueEquipoRepository;
import com.devicefy.backend.repository.EquipoRepository;
import com.devicefy.backend.repository.EstadoEquipoRepository;
import com.devicefy.backend.repository.PerifericoRepository;
import com.devicefy.backend.repository.RedConfigRepository;
import com.devicefy.backend.repository.UbicacionRepository;
import com.devicefy.backend.repository.UsuarioAsignadoRepository;
import com.devicefy.backend.service.EquipoService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipoServiceImpl implements EquipoService {

    private final EquipoRepository equipoRepository;
    private final RedConfigRepository redConfigRepository;
    private final PerifericoRepository perifericoRepository;
    private final CentroRepository centroRepository;
    private final UbicacionRepository ubicacionRepository;
    private final UsuarioAsignadoRepository usuarioAsignadoRepository;
    private final EstadoEquipoRepository estadoEquipoRepository;
    private final DespliegueEquipoRepository despliegueEquipoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EquipoResponse> listar(String hostname, String numeroSerie, String etiquetaPatrimonial,
                                       String estado, Long centroId, Boolean activo, Long tecnicoId,
                                       Long despliegueId, String provincia, List<Long> centrosPermitidos) {
        if (centrosPermitidos != null && centrosPermitidos.isEmpty()) {
            return List.of();
        }
        Specification<Equipo> spec = (root, query, cb) -> cb.conjunction();
        if (hostname != null && !hostname.isBlank()) {
            spec = spec.and(like("hostname", hostname));
        }
        if (numeroSerie != null && !numeroSerie.isBlank()) {
            spec = spec.and(like("numeroSerie", numeroSerie));
        }
        if (etiquetaPatrimonial != null && !etiquetaPatrimonial.isBlank()) {
            spec = spec.and(like("etiquetaPatrimonial", etiquetaPatrimonial));
        }
        if (estado != null && !estado.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        if (centroId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("centro").get("id"), centroId));
        }
        if (activo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), activo));
        }
        if (tecnicoId != null || despliegueId != null || (provincia != null && !provincia.isBlank())) {
            spec = spec.and((root, query, cb) -> {
                jakarta.persistence.criteria.Subquery<Long> sub = subqueryEquiposProcesados(
                        cb, query, tecnicoId, despliegueId, provincia);
                return root.get("id").in(sub);
            });
        }
        if (centrosPermitidos != null) {
            spec = spec.and((root, query, cb) -> root.get("centro").get("id").in(centrosPermitidos));
        }
        return equipoRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    private jakarta.persistence.criteria.Subquery<Long> subqueryEquiposProcesados(
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.CriteriaQuery<?> query, Long tecnicoId, Long despliegueId,
            String provincia) {
        jakarta.persistence.criteria.Subquery<Long> sub = query.subquery(Long.class);
        jakarta.persistence.criteria.Root<DespliegueEquipo> de = sub.from(DespliegueEquipo.class);
        sub.select(de.get("equipo").get("id"));
        java.util.List<jakarta.persistence.criteria.Predicate> condiciones = new java.util.ArrayList<>();
        if (tecnicoId != null) {
            condiciones.add(cb.equal(de.get("tecnico").get("id"), tecnicoId));
        }
        if (despliegueId != null) {
            condiciones.add(cb.equal(de.get("despliegue").get("id"), despliegueId));
        }
        if (provincia != null && !provincia.isBlank()) {
            condiciones.add(cb.equal(
                    cb.lower(de.get("despliegue").get("provincia")),
                    provincia.trim().toLowerCase(java.util.Locale.ROOT)));
        }
        sub.where(condiciones.toArray(jakarta.persistence.criteria.Predicate[]::new));
        return sub;
    }

    @Override
    @Transactional(readOnly = true)
    public EquipoResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Override
    @Transactional
    public EquipoResponse crear(EquipoRequest request) {
        verificarDuplicados(null, request);
        validarEstado(request.getEstado());
        Equipo equipo = new Equipo();
        aplicar(equipo, request);
        equipo = equipoRepository.save(equipo);
        aplicarRed(equipo, request.getRed());
        return toResponse(equipo);
    }

    @Override
    @Transactional
    public EquipoResponse actualizar(Long id, EquipoRequest request) {
        Equipo equipo = buscar(id);
        verificarDuplicados(id, request);
        validarEstado(request.getEstado());
        aplicar(equipo, request);
        equipo = equipoRepository.save(equipo);
        if (request.getRed() != null) {
            aplicarRed(equipo, request.getRed());
        }
        return toResponse(equipo);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = buscar(id);
        try {
            redConfigRepository.findByEquipoId(id).ifPresent(redConfigRepository::delete);
            equipoRepository.delete(equipo);
            equipoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede eliminar el equipo: tiene intervenciones o registros asociados");
        }
    }

    private Specification<Equipo> like(String campo, String valor) {
        return (root, query, cb) -> cb.like(cb.lower(root.get(campo)), "%" + valor.toLowerCase() + "%");
    }

    private Equipo buscar(Long id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
    }

    private void verificarDuplicados(Long selfId, EquipoRequest request) {
        if (request.getHostname() != null && !request.getHostname().isBlank()) {
            equipoRepository.findByHostname(request.getHostname())
                    .filter(e -> !e.getId().equals(selfId))
                    .ifPresent(e -> {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Ya existe un equipo con el hostname " + request.getHostname());
                    });
        }
        if (request.getNumeroSerie() != null && !request.getNumeroSerie().isBlank()) {
            equipoRepository.findByNumeroSerie(request.getNumeroSerie())
                    .filter(e -> !e.getId().equals(selfId))
                    .ifPresent(e -> {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Ya existe un equipo con el número de serie " + request.getNumeroSerie());
                    });
        }
        if (request.getEtiquetaPatrimonial() != null && !request.getEtiquetaPatrimonial().isBlank()) {
            equipoRepository.findByEtiquetaPatrimonial(request.getEtiquetaPatrimonial())
                    .filter(e -> !e.getId().equals(selfId))
                    .ifPresent(e -> {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Ya existe un equipo con la etiqueta patrimonial " + request.getEtiquetaPatrimonial());
                    });
        }
    }

    private void validarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return;
        }
        estadoEquipoRepository.findByCodigo(estado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Estado de equipo no válido: " + estado));
    }

    private void aplicar(Equipo equipo, EquipoRequest request) {
        equipo.setHostname(request.getHostname());
        equipo.setNumeroSerie(request.getNumeroSerie());
        equipo.setEtiquetaPatrimonial(request.getEtiquetaPatrimonial());
        equipo.setFabricante(request.getFabricante());
        equipo.setModelo(request.getModelo());
        equipo.setSistemaOperativo(request.getSistemaOperativo());
        equipo.setProcesador(request.getProcesador());
        equipo.setTipoEquipo(request.getTipoEquipo());
        equipo.setEstado(request.getEstado());
        equipo.setObservaciones(request.getObservaciones());
        equipo.setCentro(buscarCentro(request.getCentroId()));
        equipo.setUbicacion(buscarUbicacion(request.getUbicacionId()));
        equipo.setUsuarioAsignado(buscarUsuarioAsignado(request.getUsuarioAsignadoId()));
        if (request.getActivo() != null) {
            equipo.setActivo(request.getActivo());
        }
    }

    private Centro buscarCentro(Long id) {
        if (id == null) {
            return null;
        }
        return centroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro no encontrado"));
    }

    private Ubicacion buscarUbicacion(Long id) {
        if (id == null) {
            return null;
        }
        return ubicacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ubicación no encontrada"));
    }

    private UsuarioAsignado buscarUsuarioAsignado(Long id) {
        if (id == null) {
            return null;
        }
        return usuarioAsignadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario asignado no encontrado"));
    }

    private void aplicarRed(Equipo equipo, RedConfigRequest request) {
        if (request == null) {
            return;
        }
        RedConfig red = redConfigRepository.findByEquipoId(equipo.getId())
                .orElseGet(RedConfig::new);
        red.setEquipo(equipo);
        red.setTipoAsignacion(request.getTipoAsignacion() == null
                ? TipoAsignacionRed.DHCP
                : request.getTipoAsignacion());
        red.setIp(request.getIp());
        red.setMascara(request.getMascara());
        red.setPuertaEnlace(request.getPuertaEnlace());
        red.setDns1(request.getDns1());
        red.setDns2(request.getDns2());
        red.setDominio(request.getDominio());
        redConfigRepository.save(red);
    }

    private EquipoResponse toResponse(Equipo equipo) {
        RedConfigResponse red = redConfigRepository.findByEquipoId(equipo.getId())
                .map(r -> new RedConfigResponse(r.getId(), r.getTipoAsignacion(), r.getIp(), r.getMascara(),
                        r.getPuertaEnlace(), r.getDns1(), r.getDns2(), r.getDominio(), r.getActualizadaAt()))
                .orElse(null);
        List<PerifericoResponse> perifericos = perifericoRepository.findByEquipoId(equipo.getId()).stream()
                .map(p -> new PerifericoResponse(p.getId(), p.getEquipo() == null ? null : p.getEquipo().getId(),
                        p.getTipo(), p.getMarca(), p.getModelo(), p.getNumeroSerie(), p.getEtiquetaPatrimonial(),
                        p.getTamanioPulgadas(), p.getActivo()))
                .toList();
        Usuario tecnicoProceso = despliegueEquipoRepository.findByEquipoId(equipo.getId()).stream()
                .map(DespliegueEquipo::getTecnico)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
        return new EquipoResponse(equipo.getId(), equipo.getHostname(), equipo.getNumeroSerie(),
                equipo.getEtiquetaPatrimonial(), equipo.getFabricante(), equipo.getModelo(),
                equipo.getSistemaOperativo(), equipo.getProcesador(), equipo.getTipoEquipo(),
                equipo.getEstado(),
                equipo.getCentro() == null ? null : equipo.getCentro().getId(),
                equipo.getCentro() == null ? null : equipo.getCentro().getNombre(),
                equipo.getUbicacion() == null ? null : equipo.getUbicacion().getId(),
                equipo.getUbicacion() == null ? null : equipo.getUbicacion().getNombre(),
                equipo.getUsuarioAsignado() == null ? null : equipo.getUsuarioAsignado().getId(),
                equipo.getUsuarioAsignado() == null ? null : equipo.getUsuarioAsignado().getNombre(),
                tecnicoProceso == null ? null : tecnicoProceso.getId(),
                tecnicoProceso == null ? null : tecnicoProceso.getNombreCompleto(),
                equipo.getObservaciones(), equipo.getActivo(), red, perifericos);
    }
}
