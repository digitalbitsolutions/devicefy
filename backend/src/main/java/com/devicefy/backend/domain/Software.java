package com.devicefy.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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
@Table(name = "software")
public class Software extends BaseEntity {

    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(name = "fabricante", length = 150)
    private String fabricante;

    @Column(name = "version_referencia", length = 50)
    private String versionReferencia;

    @Column(name = "categoria", length = 80)
    private String categoria;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "software")
    private List<IntervencionSoftware> intervenciones = new ArrayList<>();
}
