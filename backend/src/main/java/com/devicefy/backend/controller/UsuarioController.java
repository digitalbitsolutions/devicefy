package com.devicefy.backend.controller;

import com.devicefy.backend.domain.Centro;
import com.devicefy.backend.domain.Despliegue;
import com.devicefy.backend.domain.Rol;
import com.devicefy.backend.domain.Usuario;
import com.devicefy.backend.domain.enums.RolNombre;
import com.devicefy.backend.dto.ActualizarUsuarioRequest;
import com.devicefy.backend.dto.AsignarCentrosRequest;
import com.devicefy.backend.dto.AsignarDesplieguesRequest;
import com.devicefy.backend.dto.CentroResponse;
import com.devicefy.backend.dto.CrearUsuarioRequest;
import com.devicefy.backend.dto.UsuarioResponse;
import com.devicefy.backend.repository.CentroRepository;
import com.devicefy.backend.repository.DespliegueRepository;
import com.devicefy.backend.repository.RolRepository;
import com.devicefy.backend.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CentroRepository centroRepository;
    private final DespliegueRepository despliegueRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllConDetalle().stream()
                .sorted(Comparator.comparing(Usuario::getNombreCompleto))
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return toResponse(buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest req) {
        if (usuarioRepository.existsByUsername(req.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario ya existe");
        }
        RolNombre rolNombre = req.getRol() == null ? RolNombre.TECNICO : req.getRol();
        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Rol no configurado: " + rolNombre));
        Usuario u = new Usuario();
        u.setUsername(req.getUsername().trim());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setNombreCompleto(req.getNombreCompleto());
        u.setEmail(req.getEmail());
        u.setActivo(true);
        u.getRoles().add(rol);
        Usuario guardado = usuarioRepository.save(u);
        return toResponse(usuarioRepository.findByIdConDetalle(guardado.getId()).orElse(guardado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest req) {
        Usuario u = buscar(id);
        u.setNombreCompleto(req.getNombreCompleto());
        u.setEmail(req.getEmail());
        if (req.getActivo() != null) {
            u.setActivo(req.getActivo());
        }
        if (req.getRol() != null) {
            Rol rol = rolRepository.findByNombre(req.getRol())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Rol no configurado: " + req.getRol()));
            u.getRoles().clear();
            u.getRoles().add(rol);
        }
        usuarioRepository.save(u);
        return toResponse(usuarioRepository.findByIdConDetalle(id).orElseThrow());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        Usuario u = buscar(id);
        u.getRoles().clear();
        u.getCentros().clear();
        u.getDespliegues().clear();
        usuarioRepository.save(u);
        usuarioRepository.deleteById(id);
    }

    @PutMapping("/{id}/centros")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse asignarCentros(@PathVariable Long id, @RequestBody AsignarCentrosRequest req) {
        Usuario u = buscar(id);
        u.getCentros().clear();
        if (req.getCentroIds() != null) {
            for (Long cid : req.getCentroIds()) {
                centroRepository.findById(cid).ifPresent(u.getCentros()::add);
            }
        }
        usuarioRepository.save(u);
        return toResponse(usuarioRepository.findByIdConDetalle(id).orElseThrow());
    }

    @PutMapping("/{id}/despliegues")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse asignarDespliegues(@PathVariable Long id, @RequestBody AsignarDesplieguesRequest req) {
        Usuario u = buscar(id);
        u.getDespliegues().clear();
        if (req.getDespliegueIds() != null) {
            for (Long did : req.getDespliegueIds()) {
                despliegueRepository.findById(did).ifPresent(u.getDespliegues()::add);
            }
        }
        usuarioRepository.save(u);
        return toResponse(usuarioRepository.findByIdConDetalle(id).orElseThrow());
    }

    @GetMapping("/me/centros")
    public List<CentroResponse> misCentros(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario u = usuarioRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return u.getCentros().stream()
                .sorted(Comparator.comparing(Centro::getNombre))
                .map(c -> new CentroResponse(c.getId(), c.getCodigo(), c.getNombre(), c.getTipo(), c.getDireccion(), c.getActivo()))
                .toList();
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findByIdConDetalle(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getUsername(),
                u.getNombreCompleto(),
                u.getEmail(),
                u.getActivo(),
                u.getRoles().stream().map(r -> r.getNombre().name()).sorted().toList(),
                u.getCentros().stream().map(Centro::getId).sorted().toList(),
                u.getCentros().stream().map(Centro::getNombre).sorted().toList(),
                u.getDespliegues().stream().map(Despliegue::getId).sorted().toList(),
                u.getDespliegues().stream().map(Despliegue::getNombre).sorted().toList());
    }
}
