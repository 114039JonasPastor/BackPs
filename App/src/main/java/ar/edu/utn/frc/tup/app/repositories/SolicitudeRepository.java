package ar.edu.utn.frc.tup.app.repositories;

import ar.edu.utn.frc.tup.app.entities.Profesionale;
import ar.edu.utn.frc.tup.app.entities.Solicitude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    // Verificar si existe una solicitud pendiente entre usuario y profesional
    boolean existsByIdusuario_IdAndIdprofesional_IdAndEstado(
            Integer idUsuario,
            Integer idProfesional,
            String estado
    );
    
    //Mapa para solicitudes
    @Query("""
        SELECT new map(
            s.id as idSolicitud,
            s.fechasolicitud as fechaSolicitud,
            s.fechaservicio as fechaServicio,
            s.estado as estado,
            s.observacion as descripcion,
            u.id as idUsuario,
            auth.name as nombreCliente,
            auth.lastname as apellidoCliente,
            auth.mail as emailCliente,
            u.telefono as telefonoCliente,
            d.calle as calle,
            d.numero as numero,
            d.piso as piso,
            d.depto as depto,
            b.barrio as barrio,
            c.ciudad as ciudad,
            p.id as idProfesional,
            o.oficio as oficio
        )
        FROM Solicitude s
        JOIN s.idusuario u
        JOIN u.idauth auth
        JOIN u.iddireccion d
        JOIN d.idbarrio b
        JOIN b.idciudad c
        JOIN s.idprofesional p
        JOIN p.idoficio o
        WHERE s.id = :idSolicitud
        """)
    Map<String, Object> findSolicitudConDireccion(@Param("idSolicitud") Integer idSolicitud);

    /**
     * Obtiene todas las solicitudes de un profesional con información de dirección
     */
    @Query("""
        SELECT new map(
            s.id as idSolicitud,
            s.fechasolicitud as fechaSolicitud,
            s.fechaservicio as fechaServicio,
            s.estado as estado,
            s.observacion as descripcion,
            u.id as idUsuario,
            auth.name as nombreCliente,
            auth.lastname as apellidoCliente,
            auth.mail as emailCliente,
            u.telefono as telefonoCliente,
            d.calle as calle,
            d.numero as numero,
            d.piso as piso,
            d.depto as depto,
            b.barrio as barrio,
            c.ciudad as ciudad,
            p.id as idProfesional,
            o.oficio as oficio
        )
        FROM Solicitude s
        JOIN s.idusuario u
        JOIN u.idauth auth
        JOIN u.iddireccion d
        JOIN d.idbarrio b
        JOIN b.idciudad c
        JOIN s.idprofesional p
        JOIN p.idoficio o
        WHERE p.id = :idProfesional
        ORDER BY s.fechasolicitud DESC
        """)
    List<Map<String, Object>> findSolicitudesByProfesionalConDireccion(@Param("idProfesional") Integer idProfesional);

    /**
     * Obtiene los oficios más solicitados con opción de filtrar por fecha
     * Si fechaInicio y fechaFin son null, retorna todas las solicitudes
     */
    @Query("""
        SELECT o.oficio, COUNT(s.id)
        FROM Solicitude s
        JOIN s.idoficio o
        WHERE s.fechasolicitud >= COALESCE(:fechaInicio, s.fechasolicitud)
        AND s.fechasolicitud <= COALESCE(:fechaFin, s.fechasolicitud)
        GROUP BY o.oficio
        ORDER BY COUNT(s.id) DESC
        """)
    List<Object[]> findOficiosMasSolicitados(
            @Param("fechaInicio") Instant fechaInicio,
            @Param("fechaFin") Instant fechaFin
    );
}
