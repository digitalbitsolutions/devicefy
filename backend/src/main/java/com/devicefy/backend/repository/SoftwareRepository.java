package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Software;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SoftwareRepository extends JpaRepository<Software, Long> {

    Optional<Software> findByNombre(String nombre);
}
