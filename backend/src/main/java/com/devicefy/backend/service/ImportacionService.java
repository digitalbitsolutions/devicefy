package com.devicefy.backend.service;

import com.devicefy.backend.dto.DespliegueEquipoResponse;
import com.devicefy.backend.dto.DespliegueResponse;
import com.devicefy.backend.dto.ImportacionResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImportacionService {

    ImportacionResult importar(String nombreDespliegue, MultipartFile archivo);

    List<DespliegueResponse> listarDespliegues();

    List<DespliegueEquipoResponse> listarEquipos(Long despliegueId);
}
