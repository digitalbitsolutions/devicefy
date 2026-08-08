package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    List<Ubicacion> findByCentroId(Long centroId);
}
