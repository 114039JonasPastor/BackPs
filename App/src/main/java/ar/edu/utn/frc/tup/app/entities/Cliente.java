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
@Table(name = "clientes")
public class Cliente {
    @Id
    @ColumnDefault("nextval('clientes_idcliente_seq')")
    @Column(name = "idcliente", nullable = false)
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
    @Column(name = "nacimiento", nullable = false)
    private LocalTime nacimiento;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario idusuario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iddireccion", nullable = false)
    private Usuario iddireccion;

}