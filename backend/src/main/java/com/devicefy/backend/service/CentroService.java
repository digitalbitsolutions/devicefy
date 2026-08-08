package com.devicefy.backend.service;

import com.devicefy.backend.dto.CentroRequest;
import com.devicefy.backend.dto.CentroResponse;

import java.util.List;

public interface CentroService {
    List<CentroResponse> listar(List<Long> centrosPermitidos);
    CentroResponse obtener(Long id);
    CentroResponse crear(CentroRequest request);
    CentroResponse actualizar(Long id, CentroRequest request);
    void eliminar(Long id);
}
