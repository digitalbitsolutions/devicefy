package com.devicefy.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "despliegue_equipos")
public class DespliegueEquipo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despliegue_id", nullable = false)
    private Despliegue despliegue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Column(name = "hostname_actual", length = 100)
    private String hostnameActual;

    @Column(name = "hostname_nuevo", length = 100)
    private String hostnameNuevo;

    @Column(name = "estado_renove", length = 10)
    private String estadoRenove;

    @Column(name = "anio_renove")
    private Integer anioRenove;

    @Column(name = "perfil_imagen", length = 50)
    private String perfilImagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @Column(name = "fecha_toma")
    private Instant fechaToma;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "PENDIENTE";
}
