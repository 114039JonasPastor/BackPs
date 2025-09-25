package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.repositories.BarrioRepository;
import ar.edu.utn.frc.tup.app.repositories.ClienteRepository;
import ar.edu.utn.frc.tup.app.repositories.DepartamentoRepository;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistroServiceImpl implements RegistroService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    DepartamentoRepository departamentoRepository;

    @Autowired
    BarrioRepository barrioRepository;

    @Autowired
    ClienteRepository clienteRepository;

}
