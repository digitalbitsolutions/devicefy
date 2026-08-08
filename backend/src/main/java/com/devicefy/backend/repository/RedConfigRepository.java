package com.devicefy.backend.repository;

import com.devicefy.backend.domain.RedConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RedConfigRepository extends JpaRepository<RedConfig, Long> {

    Optional<RedConfig> findByEquipoId(Long equipoId);
}
