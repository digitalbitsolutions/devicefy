package com.devicefy.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuarios_asignados")
public class UsuarioAsignado extends BaseEntity {

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "puesto", length = 150)
    private String puesto;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
