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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ubicaciones")
public class Ubicacion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_id", nullable = false)
    private Centro centro;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "planta", length = 50)
    private String planta;

    @Column(name = "zona", length = 50)
    private String zona;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
