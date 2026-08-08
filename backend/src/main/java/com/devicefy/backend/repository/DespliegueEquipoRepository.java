package com.devicefy.backend.repository;

import com.devicefy.backend.domain.DespliegueEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DespliegueEquipoRepository extends JpaRepository<DespliegueEquipo, Long> {

    List<DespliegueEquipo> findByDespliegueIdOrderByHostnameActualAsc(Long despliegueId);

    @Query("""
            select de from DespliegueEquipo de
            left join fetch de.tecnico t
            where de.equipo.id = :equipoId
            order by de.id asc
            """)
    List<DespliegueEquipo> findByEquipoId(@Param("equipoId") Long equipoId);

    long countByDespliegueId(Long despliegueId);

    long countByDespliegueIdAndEstado(Long despliegueId, String estado);

    @Query("""
            select de from DespliegueEquipo de
            join fetch de.equipo e
            left join fetch e.centro c
            left join fetch e.ubicacion u
            left join fetch e.redConfig r
            left join fetch de.despliegue d
            where c.id in :centroIds
            order by c.nombre asc, e.hostname asc
            """)
    List<DespliegueEquipo> findByCentroIds(@Param("centroIds") Collection<Long> centroIds);

    @Query("""
            select de from DespliegueEquipo de
            join fetch de.equipo e
            left join fetch e.centro c
            left join fetch e.ubicacion u
            left join fetch e.redConfig r
            left join fetch de.despliegue d
            left join fetch de.tecnico t
            where de.id = :id
            """)
    Optional<DespliegueEquipo> findDetalleById(@Param("id") Long id);

    @Query("""
            select de.tecnico.id, count(de)
            from DespliegueEquipo de
            where de.tecnico.id is not null
            group by de.tecnico.id
            """)
    List<Object[]> contarAsignadosPorTecnico();

    @Query("""
            select de.tecnico.id, count(de)
            from DespliegueEquipo de
            where de.tecnico.id is not null and de.estado = 'HECHO'
            group by de.tecnico.id
            """)
    List<Object[]> contarHechosPorTecnico();

    long countByDespliegueIdInAndTecnicoIdIsNull(Collection<Long> despliegueIds);
}
