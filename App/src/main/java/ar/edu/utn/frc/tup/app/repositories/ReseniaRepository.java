package ar.edu.utn.frc.tup.app.repositories;

import ar.edu.utn.frc.tup.app.entities.Resenia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReseniaRepository extends JpaRepository<Resenia,Integer> {
//    @Query("SELECT r FROM Resenia r WHERE r.idprofesional.id = :idProfesional")
//    List<Resenia> findByProfesionalId(@Param("idProfesional") Integer idProfesional);
    List<Resenia> findByIdprofesional_Id(Integer id);
}
