package ar.edu.utn.frc.tup.app.repositories;

import ar.edu.utn.frc.tup.app.entities.Profesionale;
import ar.edu.utn.frc.tup.app.entities.Solicitude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolicitudeRepository extends JpaRepository<Solicitude, Integer> {
    Optional<Solicitude> findByIdprofesionalAndEstado(Profesionale idProfesional, String estado);
}
