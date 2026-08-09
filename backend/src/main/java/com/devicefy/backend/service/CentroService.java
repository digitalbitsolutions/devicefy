package com.devicefy.backend.service;

import com.devicefy.backend.dto.CentroRequest;
import com.devicefy.backend.dto.CentroResponse;
import com.devicefy.backend.dto.CentroResponsableRequest;
import com.devicefy.backend.dto.ResponsableResponse;

import java.util.List;

public interface CentroService {
    List<CentroResponse> listar(List<Long> centrosPermitidos, String comunidadAutonoma);
    CentroResponse obtener(Long id);
    CentroResponse crear(CentroRequest request);
    CentroResponse actualizar(Long id, CentroRequest request);
    void eliminar(Long id);
    ResponsableResponse crearResponsable(Long centroId, CentroResponsableRequest request);
    ResponsableResponse actualizarResponsable(Long centroId, Long responsableId, CentroResponsableRequest request);
    void eliminarResponsable(Long centroId, Long responsableId);
}
