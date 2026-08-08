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
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tareas")
public class Tarea {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intervencion_id", nullable = false)
    private Intervencion intervencion;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @Column(name = "completada", nullable = false)
    private Boolean completada = false;

    @Column(name = "nota", length = 500)
    private String nota;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
