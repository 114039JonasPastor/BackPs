package ar.edu.utn.frc.tup.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalTime;

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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idtipodoc", nullable = false)
    private TiposDocumento idtipodoc;

    @Size(max = 20)
    @Column(name = "documento", length = 20)
    private String documento;

    @Size(max = 20)
    @Column(name = "telefono", length = 20)
    private String telefono;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario idusuario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idoficio", nullable = false)
    private Oficio idoficio;

    @NotNull
    @Column(name = "nacimiento", nullable = false)
    private LocalTime nacimiento;

    @NotNull
    @Column(name = "fechadesde", nullable = false)
    private LocalTime fechadesde;

    @Column(name = "fechahasta")
    private LocalTime fechahasta;

}