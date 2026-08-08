package com.devicefy.backend.repository;

import com.devicefy.backend.domain.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "centros", "despliegues"})
    @Query("select distinct u from Usuario u")
    List<Usuario> findAllConDetalle();

    @EntityGraph(attributePaths = {"roles", "centros", "despliegues"})
    @Query("select distinct u from Usuario u where u.id = :id")
    Optional<Usuario> findByIdConDetalle(@Param("id") Long id);
}
