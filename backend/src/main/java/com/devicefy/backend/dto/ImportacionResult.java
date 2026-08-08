package com.devicefy.backend.dto;

import java.util.List;

public record ImportacionResult(
        Long despliegueId,
        String nombreDespliegue,
        String formato,
        int filasLeidas,
        int equiposCreados,
        int centrosCreados,
        int ubicacionesCreadas,
        int errores,
        List<ErrorImportacion> erroresDetalle) {
}
