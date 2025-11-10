package ar.edu.utn.frc.tup.app.repositories;

import ar.edu.utn.frc.tup.app.entities.Solicitude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudeRepository extends JpaRepository<Solicitude, Integer> {
}
