package com.devicefy.backend.service.impl;

import com.devicefy.backend.domain.Centro;
import com.devicefy.backend.domain.CentroResponsable;
import com.devicefy.backend.dto.CentroRequest;
import com.devicefy.backend.dto.CentroResponse;
import com.devicefy.backend.dto.CentroResponsableRequest;
import com.devicefy.backend.dto.ResponsableResponse;
import com.devicefy.backend.repository.CentroRepository;
import com.devicefy.backend.repository.CentroResponsableRepository;
import com.devicefy.backend.service.CentroService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CentroServiceImpl implements CentroService {

    private final CentroRepository centroRepository;
    private final CentroResponsableRepository responsableRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CentroResponse> listar(List<Long> centrosPermitidos, String comunidadAutonoma) {
        List<Centro> centros;
        if (comunidadAutonoma != null && !comunidadAutonoma.isBlank()) {
            centros = centroRepository.findByComunidadAutonomaIgnoreCaseOrderByNombreAsc(comunidadAutonoma.trim());
        } else {
            centros = centroRepository.findAll();
        }
        return centros.stream()
                .filter(c -> centrosPermitidos == null || centrosPermitidos.contains(c.getId()))
                .sorted(Comparator.comparing(Centro::getNombre))
                .map(this::toResponse)
                .toList();
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

    @Override
    @Transactional
    public ResponsableResponse crearResponsable(Long centroId, CentroResponsableRequest request) {
        Centro centro = buscar(centroId);
        CentroResponsable responsable = new CentroResponsable();
        responsable.setCentro(centro);
        aplicar(request, responsable);
        return toResponse(responsableRepository.save(responsable));
    }

    @Override
    @Transactional
    public ResponsableResponse actualizarResponsable(Long centroId, Long responsableId,
                                                     CentroResponsableRequest request) {
        buscar(centroId);
        CentroResponsable responsable = buscarResponsable(responsableId);
        if (!responsable.getCentro().getId().equals(centroId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El responsable no pertenece a ese centro");
        }
        aplicar(request, responsable);
        return toResponse(responsableRepository.save(responsable));
    }

    @Override
    @Transactional
    public void eliminarResponsable(Long centroId, Long responsableId) {
        buscar(centroId);
        CentroResponsable responsable = buscarResponsable(responsableId);
        if (!responsable.getCentro().getId().equals(centroId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El responsable no pertenece a ese centro");
        }
        responsableRepository.delete(responsable);
    }

    private Centro buscar(Long id) {
        return centroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro no encontrado"));
    }

    private CentroResponsable buscarResponsable(Long id) {
        return responsableRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Responsable no encontrado"));
    }

    private void aplicar(Centro centro, CentroRequest request) {
        centro.setCodigo(request.getCodigo());
        centro.setNombre(request.getNombre());
        centro.setTipo(request.getTipo());
        centro.setDireccion(request.getDireccion());
        centro.setComunidadAutonoma(request.getComunidadAutonoma());
        centro.setProvincia(request.getProvincia());
        centro.setTelefono(request.getTelefono());
        centro.setEmail(request.getEmail());
        if (request.getActivo() != null) {
            centro.setActivo(request.getActivo());
        }
    }

    private void aplicar(CentroResponsableRequest request, CentroResponsable responsable) {
        responsable.setAreaOficina(request.getAreaOficina());
        responsable.setNombre(request.getNombre());
        responsable.setTelefono(request.getTelefono());
        responsable.setEmail(request.getEmail());
    }

    private CentroResponse toResponse(Centro centro) {
        return new CentroResponse(centro.getId(), centro.getCodigo(), centro.getNombre(),
                centro.getTipo(), centro.getDireccion(), centro.getComunidadAutonoma(),
                centro.getProvincia(), centro.getTelefono(), centro.getEmail(), centro.getActivo(),
                centro.getResponsables().stream()
                        .map(this::toResponse)
                        .sorted(Comparator.comparing(ResponsableResponse::nombre,
                                Comparator.nullsLast(String::compareTo)))
                        .toList());
    }

    private ResponsableResponse toResponse(CentroResponsable r) {
        return new ResponsableResponse(r.getId(), r.getAreaOficina(), r.getNombre(),
                r.getTelefono(), r.getEmail());
    }
}
