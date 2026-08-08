package com.devicefy.backend.service;

import com.devicefy.backend.dto.EquipoRequest;
import com.devicefy.backend.dto.EquipoResponse;

import java.util.List;

public interface EquipoService {
    List<EquipoResponse> listar(String hostname, String numeroSerie, String etiquetaPatrimonial,
                                String estado, Long centroId, Boolean activo, Long tecnicoId,
                                Long despliegueId, List<Long> centrosPermitidos);
    EquipoResponse obtener(Long id);
    EquipoResponse crear(EquipoRequest request);
    EquipoResponse actualizar(Long id, EquipoRequest request);
    void eliminar(Long id);
}
