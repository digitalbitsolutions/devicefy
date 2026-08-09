package com.devicefy.backend.service;

import com.devicefy.backend.dto.ActualizarDespliegueRequest;
import com.devicefy.backend.dto.AsignarCentrosRequest;
import com.devicefy.backend.dto.AsignarDesplieguesRequest;
import com.devicefy.backend.dto.CrearDespliegueRequest;
import com.devicefy.backend.dto.DespliegueEquipoResponse;
import com.devicefy.backend.dto.DespliegueResponse;
import com.devicefy.backend.dto.ImportacionResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImportacionService {

    ImportacionResult importar(String nombreDespliegue, MultipartFile archivo);

    List<DespliegueResponse> listarDespliegues();

    List<DespliegueEquipoResponse> listarEquipos(Long despliegueId);

    DespliegueResponse crear(CrearDespliegueRequest request);

    DespliegueResponse actualizar(Long despliegueId, ActualizarDespliegueRequest request);

    void eliminar(Long despliegueId);

    DespliegueResponse asignarTecnicos(Long despliegueId, AsignarDesplieguesRequest request);

    DespliegueResponse asignarCentros(Long despliegueId, AsignarCentrosRequest request);
}
