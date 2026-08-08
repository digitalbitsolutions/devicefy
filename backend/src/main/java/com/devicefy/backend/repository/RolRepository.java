package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Rol;
import com.devicefy.backend.domain.enums.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(RolNombre nombre);
}
