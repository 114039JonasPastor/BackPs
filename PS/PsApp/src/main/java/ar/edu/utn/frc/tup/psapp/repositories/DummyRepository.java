package ar.edu.utn.frc.tup.psapp.repositories;

import ar.edu.utn.frc.tup.psapp.entities.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DummyRepository extends JpaRepository<DummyEntity, Long> {
}
