package ar.edu.utn.frc.tup.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "profesionales")
public class Profesionale {
    @Id
    @ColumnDefault("nextval('profesionales_idprofesional_seq')")
    @Column(name = "idprofesional", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "fechadesde", nullable = false)
    private LocalDate fechadesde;

    @Column(name = "fechahasta")
    private LocalDate fechahasta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario idusuario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idoficio", nullable = false)
    private Oficio idoficio;

}