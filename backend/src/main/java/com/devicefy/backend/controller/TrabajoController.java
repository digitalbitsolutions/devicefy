package com.devicefy.backend.controller;

import com.devicefy.backend.domain.Centro;
import com.devicefy.backend.domain.DespliegueEquipo;
import com.devicefy.backend.domain.Equipo;
import com.devicefy.backend.domain.RedConfig;
import com.devicefy.backend.domain.Usuario;
import com.devicefy.backend.domain.UsuarioAsignado;
import com.devicefy.backend.domain.enums.TipoAsignacionRed;
import com.devicefy.backend.dto.ProcesarEquipoRequest;
import com.devicefy.backend.dto.TrabajoResponse;
import com.devicefy.backend.repository.DespliegueEquipoRepository;
import com.devicefy.backend.repository.EquipoRepository;
import com.devicefy.backend.repository.RedConfigRepository;
import com.devicefy.backend.repository.UsuarioAsignadoRepository;
import com.devicefy.backend.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/me/trabajo")
@RequiredArgsConstructor
public class TrabajoController {

    private final UsuarioRepository usuarioRepository;
    private final DespliegueEquipoRepository despliegueEquipoRepository;
    private final RedConfigRepository redConfigRepository;
    private final UsuarioAsignadoRepository usuarioAsignadoRepository;
    private final EquipoRepository equipoRepository;

    @GetMapping
    public List<TrabajoResponse> miTrabajo(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestParam(required = false) Long centroId) {
        Usuario tecnico = tecnicoActual(userDetails);
        List<Long> centroIds = tecnico.getCentros().stream().map(Centro::getId).toList();
        if (centroIds.isEmpty()) {
            return List.of();
        }
        return despliegueEquipoRepository.findByCentroIds(centroIds).stream()
                .filter(de -> de.getEstado() == null
                        || de.getEstado().equals("PENDIENTE")
                        || de.getEstado().equals("EN_PROCESO"))
                .filter(de -> centroId == null || de.getEquipo().getCentro().getId().equals(centroId))
                .map(this::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public TrabajoResponse procesar(@PathVariable Long id,
                                    @Valid @RequestBody ProcesarEquipoRequest req,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Usuario tecnico = tecnicoActual(userDetails);
        DespliegueEquipo de = despliegueEquipoRepository.findDetalleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo de despliegue no encontrado"));
        Equipo equipo = de.getEquipo();
        Centro centro = equipo.getCentro();
        if (centro != null
                && tecnico.getCentros().stream().noneMatch(c -> c.getId().equals(centro.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes asignado este centro");
        }

        aplicarRed(equipo, req);

        if (req.getUsuarioNombre() != null && !req.getUsuarioNombre().isBlank()) {
            String nombre = req.getUsuarioNombre().trim();
            UsuarioAsignado ua = usuarioAsignadoRepository.findByNombre(nombre).orElseGet(() -> {
                UsuarioAsignado nuevo = new UsuarioAsignado();
                nuevo.setNombre(nombre);
                nuevo.setActivo(true);
                return usuarioAsignadoRepository.save(nuevo);
            });
            equipo.setUsuarioAsignado(ua);
        }

        if (req.getObservaciones() != null && !req.getObservaciones().isBlank()) {
            String actual = equipo.getObservaciones();
            equipo.setObservaciones(actual == null || actual.isBlank()
                    ? req.getObservaciones().trim()
                    : actual + "\n" + req.getObservaciones().trim());
        }

        de.setHostnameNuevo(req.getHostnameNuevo());
        de.setTecnico(tecnico);
        de.setFechaToma(Instant.now());
        String nuevoEstado = req.getEstado() == null ? "HECHO" : req.getEstado();
        de.setEstado(nuevoEstado);
        equipo.setEstado(sincronizarEstadoEquipo(nuevoEstado));
        equipoRepository.save(equipo);
        despliegueEquipoRepository.save(de);
        return toResponse(de);
    }

    private String sincronizarEstadoEquipo(String estadoDespliegue) {
        return switch (estadoDespliegue) {
            case "EN_PROCESO" -> "EN_PROCESO";
            case "HECHO" -> "FINALIZADO";
            default -> "PENDIENTE";
        };
    }

    private void aplicarRed(Equipo equipo, ProcesarEquipoRequest req) {
        if (req.getIp() == null || req.getIp().isBlank()) {
            return;
        }
        String ip = req.getIp().trim();
        RedConfig red = redConfigRepository.findByEquipoId(equipo.getId()).orElseGet(() -> {
            RedConfig r = new RedConfig();
            r.setEquipo(equipo);
            return r;
        });
        if (!ip.equals(red.getIp()) && redConfigRepository.existsByIp(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IP " + ip + " ya está asignada a otro equipo");
        }
        red.setIp(ip);
        red.setTipoAsignacion(req.getTipoAsignacion() == null ? TipoAsignacionRed.DHCP : req.getTipoAsignacion());
        redConfigRepository.save(red);
    }

    private Usuario tecnicoActual(UserDetails userDetails) {
        return usuarioRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    private TrabajoResponse toResponse(DespliegueEquipo de) {
        Equipo e = de.getEquipo();
        Centro c = e.getCentro();
        RedConfig r = e.getRedConfig();
        return new TrabajoResponse(
                de.getId(),
                de.getDespliegue() == null ? null : de.getDespliegue().getId(),
                de.getDespliegue() == null ? null : de.getDespliegue().getNombre(),
                e.getId(),
                e.getHostname(),
                e.getNumeroSerie(),
                e.getFabricante(),
                e.getModelo(),
                e.getSistemaOperativo(),
                c == null ? null : c.getId(),
                c == null ? null : c.getNombre(),
                e.getUbicacion() == null ? null : e.getUbicacion().getNombre(),
                de.getEstadoRenove(),
                de.getAnioRenove(),
                de.getPerfilImagen(),
                r == null ? null : r.getIp(),
                de.getEstado(),
                de.getTecnico() == null ? null : de.getTecnico().getNombreCompleto());
    }
}
