package ar.edu.utn.frc.tup.app.repositories;

import ar.edu.utn.frc.tup.app.entities.Profesionale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesionale, Integer> {
    @Query("SELECT p FROM Profesionale p " +
            "JOIN p.idoficio o " +
            "JOIN p.idusuario u " +
            "JOIN u.iddireccion d " +
            "JOIN d.idbarrio b " +
            "JOIN b.idciudad c " +
            "WHERE (:oficio IS NULL OR LOWER(o.oficio) LIKE LOWER(CONCAT('%', :oficio, '%'))) " +
            "AND (:zona IS NULL OR LOWER(b.barrio) LIKE LOWER(CONCAT('%', :zona, '%')) " +
            "    OR LOWER(c.ciudad) LIKE LOWER(CONCAT('%', :zona, '%')))")
    List<Profesionale> findByOficioAndZona(@Param("oficio") String oficio, @Param("zona") String zona);

    @Query("SELECT p FROM Profesionale p " +
            "JOIN p.idoficio o " +
            "WHERE LOWER(o.oficio) LIKE LOWER(CONCAT('%', :oficio, '%'))")
    List<Profesionale> findByOficio(@Param("oficio") String oficio);

    @Query("SELECT p FROM Profesionale p " +
            "JOIN p.idusuario u " +
            "JOIN u.iddireccion d " +
            "JOIN d.idbarrio b " +
            "JOIN b.idciudad c " +
            "WHERE LOWER(b.barrio) LIKE LOWER(CONCAT('%', :zona, '%')) " +
            "   OR LOWER(c.ciudad) LIKE LOWER(CONCAT('%', :zona, '%'))")
    List<Profesionale> findByZona(@Param("zona") String zona);

    @Query("SELECT p FROM Profesionale p " +
            "JOIN p.idusuario u " +
            "JOIN u.iddireccion d " +
            "JOIN d.idbarrio b " +
            "JOIN b.idciudad c " +
            "WHERE p.fechahasta IS NULL OR p.fechahasta >= CURRENT_DATE")
    List<Profesionale> findProfesionalesActivos();

    Optional<Profesionale> findByIdusuario(Integer idUsuario);
}
