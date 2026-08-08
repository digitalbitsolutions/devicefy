package com.devicefy.backend.domain;

import com.devicefy.backend.domain.enums.TipoAsignacionRed;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "redes")
public class RedConfig {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_asignacion", nullable = false, length = 20)
    private TipoAsignacionRed tipoAsignacion = TipoAsignacionRed.DHCP;

    @Column(name = "ip", unique = true, length = 45)
    private String ip;

    @Column(name = "mascara", length = 45)
    private String mascara;

    @Column(name = "puerta_enlace", length = 45)
    private String puertaEnlace;

    @Column(name = "dns1", length = 45)
    private String dns1;

    @Column(name = "dns2", length = 45)
    private String dns2;

    @Column(name = "dominio", length = 150)
    private String dominio;

    @UpdateTimestamp
    @Column(name = "actualizada_at", nullable = false)
    private Instant actualizadaAt;
}
