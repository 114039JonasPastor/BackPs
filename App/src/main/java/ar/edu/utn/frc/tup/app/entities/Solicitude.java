package ar.edu.utn.frc.tup.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "solicitudes")
public class Solicitude {
    @Id
    @ColumnDefault("nextval('solicitudes_idsolicitud_seq')")
    @Column(name = "idsolicitud", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario idusuario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idprofesional", nullable = false)
    private Profesionale idprofesional;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idoficio", nullable = false)
    private Oficio idoficio;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "fechasolicitud", nullable = false)
    private Instant fechasolicitud;

    @NotNull
    @Column(name = "fechaservicio", nullable = false)
    private Instant fechaservicio;

    @Size(max = 20)
    @NotNull
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Size(max = 500)
    @Column(name = "observacion", length = 500)
    private String observacion;

}