package com.devicefy.backend.domain;

import com.devicefy.backend.domain.enums.TipoEquipo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "equipos")
public class Equipo extends BaseEntity {

    @Column(name = "hostname", unique = true, length = 100)
    private String hostname;

    @Column(name = "numero_serie", unique = true, length = 100)
    private String numeroSerie;

    @Column(name = "etiqueta_patrimonial", unique = true, length = 100)
    private String etiquetaPatrimonial;

    @Column(name = "fabricante", length = 100)
    private String fabricante;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "sistema_operativo", length = 100)
    private String sistemaOperativo;

    @Column(name = "procesador", length = 150)
    private String procesador;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_equipo", nullable = false, length = 30)
    private TipoEquipo tipoEquipo = TipoEquipo.CPU;

    @Column(name = "estado", length = 30)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_id")
    private Centro centro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_asignado_id")
    private UsuarioAsignado usuarioAsignado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @OneToOne(mappedBy = "equipo")
    private RedConfig redConfig;

    @OneToMany(mappedBy = "equipo")
    private List<Periferico> perifericos = new ArrayList<>();

    @OneToMany(mappedBy = "equipo")
    private List<Intervencion> intervenciones = new ArrayList<>();
}
