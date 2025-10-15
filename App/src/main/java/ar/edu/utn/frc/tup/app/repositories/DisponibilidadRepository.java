package ar.edu.utn.frc.tup.app.repositories;

import ar.edu.utn.frc.tup.app.entities.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Integer> {
    Optional<Disponibilidad> findByIdprofesional_Id(Integer idProfesional);
}
