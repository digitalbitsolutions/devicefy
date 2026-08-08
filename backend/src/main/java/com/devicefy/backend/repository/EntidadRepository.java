package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Entidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntidadRepository extends JpaRepository<Entidad, Long> {

    Optional<Entidad> findByNombre(String nombre);
}
