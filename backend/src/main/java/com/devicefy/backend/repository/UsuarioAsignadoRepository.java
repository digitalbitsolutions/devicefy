package com.devicefy.backend.repository;

import com.devicefy.backend.domain.UsuarioAsignado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioAsignadoRepository extends JpaRepository<UsuarioAsignado, Long> {

    Optional<UsuarioAsignado> findByNombre(String nombre);
}
