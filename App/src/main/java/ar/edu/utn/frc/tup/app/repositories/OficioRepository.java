package ar.edu.utn.frc.tup.app.repositories;

import ar.edu.utn.frc.tup.app.entities.Oficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OficioRepository extends JpaRepository<Oficio,Integer> {
    @Query("SELECT DISTINCT o FROM Oficio o " +
            "JOIN Profesionale p ON p.idoficio.id = o.id")
    List<Oficio> findAllWithProfesionales();
    List<Oficio> findByActivoTrue();
    @Query("SELECT o FROM Oficio o WHERE o.activo = false")
    List<Oficio> findByActivoFalse();
    Optional<Oficio> findByOficioAndActivoTrue(String oficio);
}
