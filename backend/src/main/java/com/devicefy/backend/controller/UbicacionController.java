package com.devicefy.backend.controller;

import com.devicefy.backend.dto.UbicacionRequest;
import com.devicefy.backend.dto.UbicacionResponse;
import com.devicefy.backend.service.UbicacionService;
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
@RequestMapping("/api/ubicaciones")
@RequiredArgsConstructor
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping
    public List<UbicacionResponse> listar(@RequestParam(required = false) Long centroId) {
        return ubicacionService.listar(centroId);
    }

    @GetMapping("/{id}")
    public UbicacionResponse obtener(@PathVariable Long id) {
        return ubicacionService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UbicacionResponse crear(@Valid @RequestBody UbicacionRequest request) {
        return ubicacionService.crear(request);
    }

    @PutMapping("/{id}")
    public UbicacionResponse actualizar(@PathVariable Long id, @Valid @RequestBody UbicacionRequest request) {
        return ubicacionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        ubicacionService.eliminar(id);
    }
}
