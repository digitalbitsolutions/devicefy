package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Intervencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntervencionRepository extends JpaRepository<Intervencion, Long> {

    List<Intervencion> findByEquipoIdOrderByFechaInicioDesc(Long equipoId);

    List<Intervencion> findByTecnicoId(Long tecnicoId);
}
