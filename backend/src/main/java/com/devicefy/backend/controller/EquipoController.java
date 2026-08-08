package com.devicefy.backend.controller;

import com.devicefy.backend.dto.EquipoRequest;
import com.devicefy.backend.dto.EquipoResponse;
import com.devicefy.backend.service.EquipoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/equipos")
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    @GetMapping
    public List<EquipoResponse> listar(@RequestParam(required = false) String hostname,
                                       @RequestParam(required = false) String numeroSerie,
                                       @RequestParam(required = false) String etiquetaPatrimonial,
                                       @RequestParam(required = false) String estado,
                                       @RequestParam(required = false) Long centroId,
                                       @RequestParam(required = false) Boolean activo) {
        return equipoService.listar(hostname, numeroSerie, etiquetaPatrimonial, estado, centroId, activo);
    }

    @GetMapping("/{id}")
    public EquipoResponse obtener(@PathVariable Long id) {
        return equipoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoResponse crear(@Valid @RequestBody EquipoRequest request) {
        return equipoService.crear(request);
    }

    @PutMapping("/{id}")
    public EquipoResponse actualizar(@PathVariable Long id, @Valid @RequestBody EquipoRequest request) {
        return equipoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        equipoService.eliminar(id);
    }
}
