package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long>, JpaSpecificationExecutor<Equipo> {

    Optional<Equipo> findByHostname(String hostname);

    Optional<Equipo> findByNumeroSerie(String numeroSerie);

    Optional<Equipo> findByEtiquetaPatrimonial(String etiquetaPatrimonial);

    boolean existsByHostname(String hostname);

    boolean existsByNumeroSerie(String numeroSerie);

    boolean existsByEtiquetaPatrimonial(String etiquetaPatrimonial);
}
