package ar.edu.utn.frc.tup.app.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "ciudades")
public class Ciudade {
    @Id
    @ColumnDefault("nextval('ciudades_idciudad_seq')")
    @Column(name = "idciudad", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

}