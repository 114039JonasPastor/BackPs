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
@Table(name = "provincias")
public class Provincia {
    @Id
    @ColumnDefault("nextval('provincias_idprovincia_seq')")
    @Column(name = "idprovincia", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "provincia", nullable = false, length = 50)
    private String provincia;

}