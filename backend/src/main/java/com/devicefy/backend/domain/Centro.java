package com.devicefy.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "centros")
public class Centro extends BaseEntity {

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "tipo", length = 30)
    private String tipo;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entidad_id")
    private Entidad entidad;

    @ManyToMany(mappedBy = "centros")
    private Set<Usuario> tecnicos = new HashSet<>();

    @OneToMany(mappedBy = "centro")
    private List<Ubicacion> ubicaciones = new ArrayList<>();

    @OneToMany(mappedBy = "centro")
    private List<Equipo> equipos = new ArrayList<>();
}
