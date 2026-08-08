package com.devicefy.backend.service.impl;

import com.devicefy.backend.domain.Centro;
import com.devicefy.backend.domain.Ubicacion;
import com.devicefy.backend.dto.UbicacionRequest;
import com.devicefy.backend.dto.UbicacionResponse;
import com.devicefy.backend.repository.CentroRepository;
import com.devicefy.backend.repository.UbicacionRepository;
import com.devicefy.backend.service.UbicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicacionServiceImpl implements UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final CentroRepository centroRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionResponse> listar(Long centroId) {
        List<Ubicacion> ubicaciones = centroId == null
                ? ubicacionRepository.findAll()
                : ubicacionRepository.findByCentroId(centroId);
        return ubicaciones.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UbicacionResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Override
    @Transactional
    public UbicacionResponse crear(UbicacionRequest request) {
        Centro centro = buscarCentro(request.getCentroId());
        if (ubicacionRepository.existsByCentroIdAndNombre(request.getCentroId(), request.getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ya existe una ubicación con ese nombre en el centro");
        }
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setCentro(centro);
        aplicar(ubicacion, request);
        return toResponse(ubicacionRepository.save(ubicacion));
    }

    @Override
    @Transactional
    public UbicacionResponse actualizar(Long id, UbicacionRequest request) {
        Ubicacion ubicacion = buscar(id);
        Centro centro = buscarCentro(request.getCentroId());
        if (ubicacionRepository.existsByCentroIdAndNombreAndIdNot(
                request.getCentroId(), request.getNombre(), id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ya existe una ubicación con ese nombre en el centro");
        }
        ubicacion.setCentro(centro);
        aplicar(ubicacion, request);
        return toResponse(ubicacionRepository.save(ubicacion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Ubicacion ubicacion = buscar(id);
        try {
            ubicacionRepository.delete(ubicacion);
            ubicacionRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede eliminar la ubicación: tiene equipos asociados");
        }
    }

    private Ubicacion buscar(Long id) {
        return ubicacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ubicación no encontrada"));
    }

    private Centro buscarCentro(Long centroId) {
        return centroRepository.findById(centroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro no encontrado"));
    }

    private void aplicar(Ubicacion ubicacion, UbicacionRequest request) {
        ubicacion.setNombre(request.getNombre());
        ubicacion.setPlanta(request.getPlanta());
        ubicacion.setZona(request.getZona());
        if (request.getActivo() != null) {
            ubicacion.setActivo(request.getActivo());
        }
    }

    private UbicacionResponse toResponse(Ubicacion ubicacion) {
        return new UbicacionResponse(ubicacion.getId(),
                ubicacion.getCentro().getId(),
                ubicacion.getCentro().getNombre(),
                ubicacion.getNombre(),
                ubicacion.getPlanta(),
                ubicacion.getZona(),
                ubicacion.getActivo());
    }
}
