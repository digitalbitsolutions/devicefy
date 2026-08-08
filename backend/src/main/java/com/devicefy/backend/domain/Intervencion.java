package com.devicefy.backend.domain;

import com.devicefy.backend.domain.enums.EstadoIntervencion;
import com.devicefy.backend.domain.enums.TipoIntervencion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "intervenciones")
public class Intervencion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tecnico_id", nullable = false)
    private Usuario tecnico;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoIntervencion tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoIntervencion estado = EstadoIntervencion.BORRADOR;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio = Instant.now();

    @Column(name = "fecha_fin")
    private Instant fechaFin;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "incidencias", columnDefinition = "TEXT")
    private String incidencias;

    @OneToMany(mappedBy = "intervencion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tarea> tareas = new ArrayList<>();

    @OneToMany(mappedBy = "intervencion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IntervencionSoftware> softwareChecks = new ArrayList<>();

    public void addTarea(Tarea tarea) {
        tareas.add(tarea);
        tarea.setIntervencion(this);
    }

    public void addSoftwareCheck(IntervencionSoftware check) {
        softwareChecks.add(check);
        check.setIntervencion(this);
    }
}
