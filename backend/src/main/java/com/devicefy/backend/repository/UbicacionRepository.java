package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    List<Ubicacion> findByCentroId(Long centroId);

    Optional<Ubicacion> findByCentroIdAndNombre(Long centroId, String nombre);

    boolean existsByCentroIdAndNombre(Long centroId, String nombre);

    boolean existsByCentroIdAndNombreAndIdNot(Long centroId, String nombre, Long id);
}
