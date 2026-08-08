package com.devicefy.backend.repository;

import com.devicefy.backend.domain.UsuarioAsignado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioAsignadoRepository extends JpaRepository<UsuarioAsignado, Long> {
}
