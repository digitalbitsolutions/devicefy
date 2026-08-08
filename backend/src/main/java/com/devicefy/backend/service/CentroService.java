package com.devicefy.backend.service;

import com.devicefy.backend.domain.Centro;

import java.util.List;
import java.util.Optional;

public interface CentroService {
    List<Centro> listarTodos();
    Optional<Centro> obtenerPorId(Long id);
    Centro crear(Centro centro);
    Centro actualizar(Long id, Centro centro);
    void eliminar(Long id);
}
