package com.devicefy.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "despliegues")
public class Despliegue extends BaseEntity {

    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(name = "fichero_nombre", length = 255)
    private String ficheroNombre;

    @Column(name = "fecha_importacion")
    private Instant fechaImportacion;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @OneToMany(mappedBy = "despliegue")
    private List<DespliegueEquipo> equipos = new ArrayList<>();
}
