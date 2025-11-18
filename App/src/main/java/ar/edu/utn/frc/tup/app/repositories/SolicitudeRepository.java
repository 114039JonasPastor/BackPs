package ar.edu.utn.frc.tup.app.repositories;

import ar.edu.utn.frc.tup.app.entities.Profesionale;
import ar.edu.utn.frc.tup.app.entities.Solicitude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudeRepository extends JpaRepository<Solicitude, Integer> {
    List<Solicitude> findByIdprofesionalAndEstado(Profesionale idProfesional, String estado);
    List<Solicitude> findByIdusuario_Id(Integer idUsuario);

    // Query original (solo turnos)
    @Query("SELECT s FROM Solicitude s WHERE s.idprofesional.id = :idProfesional " +
            "AND s.esTurno = true AND DATE(s.fechaservicio) = :fecha " +
            "ORDER BY s.fechaservicio")
    List<Solicitude> findTurnosByProfesionalAndFecha(
            @Param("idProfesional") Integer idProfesional,
            @Param("fecha") LocalDate fecha
    );

    // Nueva query: Busca TODAS las solicitudes ACEPTADAS (no solo turnos)
    @Query("SELECT s FROM Solicitude s WHERE s.idprofesional.id = :idProfesional " +
            "AND s.estado = 'ACEPTADA' AND DATE(s.fechaservicio) = :fecha " +
            "ORDER BY s.fechaservicio")
    List<Solicitude> findSolicitudesAceptadasByProfesionalAndFecha(
            @Param("idProfesional") Integer idProfesional,
            @Param("fecha") LocalDate fecha
    );
}
