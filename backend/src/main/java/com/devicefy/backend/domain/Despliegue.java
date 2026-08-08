package com.devicefy.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "despliegues")
public class Despliegue extends BaseEntity {

    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(name = "provincia", length = 100)
    private String provincia;

    @Column(name = "fichero_nombre", length = 255)
    private String ficheroNombre;

    @Column(name = "fecha_importacion")
    private Instant fechaImportacion;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @OneToMany(mappedBy = "despliegue")
    private List<DespliegueEquipo> equipos = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "despliegue_tecnicos",
            joinColumns = @JoinColumn(name = "despliegue_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    private Set<Usuario> tecnicos = new HashSet<>();
}
