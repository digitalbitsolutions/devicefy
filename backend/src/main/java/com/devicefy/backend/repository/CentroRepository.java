package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Centro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CentroRepository extends JpaRepository<Centro, Long> {

    Optional<Centro> findByCodigo(String codigo);

    List<Centro> findByComunidadAutonomaIgnoreCaseOrderByNombreAsc(String comunidadAutonoma);
}
