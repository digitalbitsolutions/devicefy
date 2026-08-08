package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long>, JpaSpecificationExecutor<Equipo> {

    Optional<Equipo> findByHostname(String hostname);

    Optional<Equipo> findByNumeroSerie(String numeroSerie);

    Optional<Equipo> findByEtiquetaPatrimonial(String etiquetaPatrimonial);

    boolean existsByHostname(String hostname);

    boolean existsByNumeroSerie(String numeroSerie);

    boolean existsByEtiquetaPatrimonial(String etiquetaPatrimonial);

    @Query("""
            select e.estado as clave, count(e) as total
            from Equipo e
            where e.estado is not null
            group by e.estado
            order by e.estado
            """)
    List<Object[]> contarPorEstado();

    @Query("""
            select e.tipoEquipo as clave, count(e) as total
            from Equipo e
            group by e.tipoEquipo
            order by e.tipoEquipo
            """)
    List<Object[]> contarPorTipo();

    @Query("""
            select c.nombre as clave, count(e) as total
            from Equipo e
            join e.centro c
            group by c.nombre
            order by count(e) desc
            """)
    List<Object[]> contarPorCentro();
}
