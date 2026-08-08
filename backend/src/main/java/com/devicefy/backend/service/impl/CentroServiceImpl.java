package com.devicefy.backend.service.impl;

import com.devicefy.backend.domain.Centro;
import com.devicefy.backend.dto.CentroRequest;
import com.devicefy.backend.dto.CentroResponse;
import com.devicefy.backend.repository.CentroRepository;
import com.devicefy.backend.service.CentroService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CentroServiceImpl implements CentroService {

    private final CentroRepository centroRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CentroResponse> listar() {
        return centroRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CentroResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Override
    @Transactional
    public CentroResponse crear(CentroRequest request) {
        if (centroRepository.findByCodigo(request.getCodigo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ya existe un centro con el código " + request.getCodigo());
        }
        Centro centro = new Centro();
        aplicar(centro, request);
        return toResponse(centroRepository.save(centro));
    }

    @Override
    @Transactional
    public CentroResponse actualizar(Long id, CentroRequest request) {
        Centro centro = buscar(id);
        centroRepository.findByCodigo(request.getCodigo())
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Ya existe un centro con el código " + request.getCodigo());
                });
        aplicar(centro, request);
        return toResponse(centroRepository.save(centro));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Centro centro = buscar(id);
        try {
            centroRepository.delete(centro);
            centroRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede eliminar el centro: tiene ubicaciones o equipos asociados");
        }
    }

    private Centro buscar(Long id) {
        return centroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro no encontrado"));
    }

    private void aplicar(Centro centro, CentroRequest request) {
        centro.setCodigo(request.getCodigo());
        centro.setNombre(request.getNombre());
        centro.setTipo(request.getTipo());
        centro.setDireccion(request.getDireccion());
        if (request.getActivo() != null) {
            centro.setActivo(request.getActivo());
        }
    }

    private CentroResponse toResponse(Centro centro) {
        return new CentroResponse(centro.getId(), centro.getCodigo(), centro.getNombre(),
                centro.getTipo(), centro.getDireccion(), centro.getActivo());
    }
}
