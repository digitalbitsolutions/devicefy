package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Despliegue;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DespliegueRepository extends JpaRepository<Despliegue, Long> {

    Optional<Despliegue> findByNombre(String nombre);

    @EntityGraph(attributePaths = {"tecnicos", "centros"})
    List<Despliegue> findAllByOrderByCreatedAtDesc();
}
