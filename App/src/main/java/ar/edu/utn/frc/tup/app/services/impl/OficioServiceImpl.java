package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.entities.Oficio;
import ar.edu.utn.frc.tup.app.repositories.OficioRepository;
import ar.edu.utn.frc.tup.app.services.OficioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OficioServiceImpl implements OficioService {

    private final OficioRepository oficioRepository;

    @Override
    public List<Oficio> getAllOficios() {
        return oficioRepository.findByActivoTrue();
    }

    @Override
    public List<Oficio> getAllOficiosIncludingInactive() {
        return oficioRepository.findByActivoFalse();
    }

    @Override
    public void desactivarOficio(Integer idOficio) {
        Oficio oficio = oficioRepository.findById(idOficio)
                .orElseThrow(() -> new RuntimeException("Oficio no encontrado"));

        oficio.setActivo(false);
        oficioRepository.save(oficio);
    }

    @Override
    public void activarOficio(Integer idOficio) {
        Oficio oficio = oficioRepository.findById(idOficio)
                .orElseThrow(() -> new RuntimeException("Oficio no encontrado"));

        oficio.setActivo(true);
        oficioRepository.save(oficio);
    }
}
