package com.devicefy.backend.service.impl;

import com.devicefy.backend.domain.Equipo;
import com.devicefy.backend.domain.Periferico;
import com.devicefy.backend.dto.PerifericoRequest;
import com.devicefy.backend.dto.PerifericoResponse;
import com.devicefy.backend.repository.EquipoRepository;
import com.devicefy.backend.repository.PerifericoRepository;
import com.devicefy.backend.service.PerifericoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerifericoServiceImpl implements PerifericoService {

    private final PerifericoRepository perifericoRepository;
    private final EquipoRepository equipoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PerifericoResponse> listar(Long equipoId) {
        List<Periferico> perifericos = equipoId == null
                ? perifericoRepository.findAll()
                : perifericoRepository.findByEquipoId(equipoId);
        return perifericos.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PerifericoResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Override
    @Transactional
    public PerifericoResponse crear(PerifericoRequest request) {
        Periferico periferico = new Periferico();
        periferico.setEquipo(buscarEquipoOpcional(request.getEquipoId()));
        aplicar(periferico, request);
        return toResponse(perifericoRepository.save(periferico));
    }

    @Override
    @Transactional
    public PerifericoResponse actualizar(Long id, PerifericoRequest request) {
        Periferico periferico = buscar(id);
        periferico.setEquipo(buscarEquipoOpcional(request.getEquipoId()));
        aplicar(periferico, request);
        return toResponse(perifericoRepository.save(periferico));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        perifericoRepository.delete(buscar(id));
    }

    private Periferico buscar(Long id) {
        return perifericoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Periférico no encontrado"));
    }

    private Equipo buscarEquipoOpcional(Long equipoId) {
        if (equipoId == null) {
            return null;
        }
        return equipoRepository.findById(equipoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
    }

    private void aplicar(Periferico periferico, PerifericoRequest request) {
        periferico.setTipo(request.getTipo());
        periferico.setMarca(request.getMarca());
        periferico.setModelo(request.getModelo());
        periferico.setNumeroSerie(request.getNumeroSerie());
        periferico.setEtiquetaPatrimonial(request.getEtiquetaPatrimonial());
        periferico.setTamanioPulgadas(request.getTamanioPulgadas());
        if (request.getActivo() != null) {
            periferico.setActivo(request.getActivo());
        }
    }

    private PerifericoResponse toResponse(Periferico periferico) {
        return new PerifericoResponse(periferico.getId(),
                periferico.getEquipo() == null ? null : periferico.getEquipo().getId(),
                periferico.getTipo(),
                periferico.getMarca(),
                periferico.getModelo(),
                periferico.getNumeroSerie(),
                periferico.getEtiquetaPatrimonial(),
                periferico.getTamanioPulgadas(),
                periferico.getActivo());
    }
}
