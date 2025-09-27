package ar.edu.utn.frc.tup.app.repositories;

import ar.edu.utn.frc.tup.app.entities.Oficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OficioRepository extends JpaRepository<Oficio,Integer> {
}
