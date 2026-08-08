package com.devicefy.backend.controller;

import com.devicefy.backend.domain.Centro;
import com.devicefy.backend.domain.Rol;
import com.devicefy.backend.domain.Usuario;
import com.devicefy.backend.domain.enums.RolNombre;
import com.devicefy.backend.dto.AsignarCentrosRequest;
import com.devicefy.backend.dto.CentroResponse;
import com.devicefy.backend.dto.CrearUsuarioRequest;
import com.devicefy.backend.dto.UsuarioResponse;
import com.devicefy.backend.repository.CentroRepository;
import com.devicefy.backend.repository.RolRepository;
import com.devicefy.backend.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getNombreCompleto))
                .map(this::toResponse)
                .toList();
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
        return toResponse(usuarioRepository.save(u));
    }

    @PutMapping("/{id}/centros")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse asignarCentros(@PathVariable Long id, @RequestBody AsignarCentrosRequest req) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        u.getCentros().clear();
        if (req.getCentroIds() != null) {
            for (Long cid : req.getCentroIds()) {
                centroRepository.findById(cid).ifPresent(u.getCentros()::add);
            }
        }
        return toResponse(usuarioRepository.save(u));
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

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getUsername(),
                u.getNombreCompleto(),
                u.getEmail(),
                u.getActivo(),
                u.getRoles().stream().map(r -> r.getNombre().name()).sorted().toList(),
                u.getCentros().stream().map(Centro::getId).sorted().toList(),
                u.getCentros().stream().map(Centro::getNombre).sorted().toList());
    }
}
