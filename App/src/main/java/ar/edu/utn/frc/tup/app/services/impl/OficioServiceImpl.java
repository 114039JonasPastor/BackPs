package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.entities.Oficio;
import ar.edu.utn.frc.tup.app.repositories.OficioRepository;
import ar.edu.utn.frc.tup.app.services.OficioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OficioServiceImpl implements OficioService {

    @Autowired
    OficioRepository oficioRepository;

    @Override
    public List<Oficio> getAllOficios() {
        return oficioRepository.findAll();
    }
}
