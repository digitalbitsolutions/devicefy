package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Periferico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerifericoRepository extends JpaRepository<Periferico, Long> {

    List<Periferico> findByEquipoId(Long equipoId);

    List<Periferico> findByEquipoIsNull();
}
