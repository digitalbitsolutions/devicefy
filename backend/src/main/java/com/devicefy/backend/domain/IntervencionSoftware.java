package com.devicefy.backend.domain;

import com.devicefy.backend.domain.enums.EstadoSoftware;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "intervencion_software")
public class IntervencionSoftware {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intervencion_id", nullable = false)
    private Intervencion intervencion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "software_id", nullable = false)
    private Software software;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoSoftware estado = EstadoSoftware.PENDIENTE;

    @Column(name = "version_instalada", length = 50)
    private String versionInstalada;

    @Column(name = "nota", length = 500)
    private String nota;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
