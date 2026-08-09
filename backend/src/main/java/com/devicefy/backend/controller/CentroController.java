package com.devicefy.backend.controller;

import com.devicefy.backend.domain.Usuario;
import com.devicefy.backend.domain.enums.RolNombre;
import com.devicefy.backend.dto.CentroRequest;
import com.devicefy.backend.dto.CentroResponse;
import com.devicefy.backend.dto.CentroResponsableRequest;
import com.devicefy.backend.dto.ResponsableResponse;
import com.devicefy.backend.repository.UsuarioRepository;
import com.devicefy.backend.service.CentroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/centros")
@RequiredArgsConstructor
public class CentroController {

    private final CentroService centroService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public List<CentroResponse> listar(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam(required = false) String comunidadAutonoma) {
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre() == RolNombre.ADMIN);
        List<Long> centrosPermitidos = null;
        if (!esAdmin) {
            centrosPermitidos = usuario.getCentros().stream().map(c -> c.getId()).toList();
        }
        return centroService.listar(centrosPermitidos, comunidadAutonoma);
    }

    @GetMapping("/{id}")
    public CentroResponse obtener(@PathVariable Long id) {
        return centroService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CentroResponse crear(@Valid @RequestBody CentroRequest request) {
        return centroService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CentroResponse actualizar(@PathVariable Long id, @Valid @RequestBody CentroRequest request) {
        return centroService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        centroService.eliminar(id);
    }

    @PostMapping("/{id}/responsables")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponsableResponse crearResponsable(@PathVariable Long id,
                                                @Valid @RequestBody CentroResponsableRequest request) {
        return centroService.crearResponsable(id, request);
    }

    @PutMapping("/{id}/responsables/{responsableId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponsableResponse actualizarResponsable(@PathVariable Long id,
                                                     @PathVariable Long responsableId,
                                                     @Valid @RequestBody CentroResponsableRequest request) {
        return centroService.actualizarResponsable(id, responsableId, request);
    }

    @DeleteMapping("/{id}/responsables/{responsableId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminarResponsable(@PathVariable Long id, @PathVariable Long responsableId) {
        centroService.eliminarResponsable(id, responsableId);
    }
}
