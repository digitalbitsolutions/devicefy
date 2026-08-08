package com.devicefy.backend.service;

import com.devicefy.backend.dto.PerifericoRequest;
import com.devicefy.backend.dto.PerifericoResponse;

import java.util.List;

public interface PerifericoService {
    List<PerifericoResponse> listar(Long equipoId);
    PerifericoResponse obtener(Long id);
    PerifericoResponse crear(PerifericoRequest request);
    PerifericoResponse actualizar(Long id, PerifericoRequest request);
    void eliminar(Long id);
}
