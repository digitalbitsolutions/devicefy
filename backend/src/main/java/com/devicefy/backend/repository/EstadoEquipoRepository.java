package com.devicefy.backend.repository;

import com.devicefy.backend.domain.EstadoEquipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoEquipoRepository extends JpaRepository<EstadoEquipo, Long> {

    Optional<EstadoEquipo> findByCodigo(String codigo);
}
