package com.devicefy.backend.service;

import com.devicefy.backend.dto.UbicacionRequest;
import com.devicefy.backend.dto.UbicacionResponse;

import java.util.List;

public interface UbicacionService {
    List<UbicacionResponse> listar(Long centroId);
    UbicacionResponse obtener(Long id);
    UbicacionResponse crear(UbicacionRequest request);
    UbicacionResponse actualizar(Long id, UbicacionRequest request);
    void eliminar(Long id);
}
