package ar.edu.utn.frc.tup.app.repositories;

import ar.edu.utn.frc.tup.app.entities.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura,Integer> {
    List<Factura> findByEstadopagoOrderByFechaDesc(String estadopago);
}
