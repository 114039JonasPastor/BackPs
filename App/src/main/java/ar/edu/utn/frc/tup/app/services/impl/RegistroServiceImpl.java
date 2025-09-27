package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.registro.NuevoCliente;
import ar.edu.utn.frc.tup.app.dtos.request.registro.NuevoProfesional;
import ar.edu.utn.frc.tup.app.entities.*;
import ar.edu.utn.frc.tup.app.repositories.ClienteRepository;
import ar.edu.utn.frc.tup.app.repositories.DireccionRepository;
import ar.edu.utn.frc.tup.app.repositories.TipoDocumentoRepository;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistroServiceImpl implements RegistroService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    DireccionRepository direccionRepository;

    @Override
    public Profesionale registrarProfesional(NuevoProfesional nuevo) {
        return null;
    }

    @Override
    public Cliente registrarCliente(NuevoCliente nuevo) {
        Direccione direccion = direccionRepository.findById(nuevo.getIdDireccion()).orElse(null);
        Usuario usuario = usuarioRepository.findById(nuevo.getIdUsuario()).orElse(null);
        TiposDocumento tipo = tipoDocumentoRepository.findById(nuevo.getIdTipoDoc()).orElse(null);

        Cliente cliente = new Cliente();
        cliente.setIdtipodoc(tipo);
        cliente.setIdusuario(usuario);
        cliente.setIddireccion(direccion);
        cliente.setNacimiento(nuevo.getFechaNacimiento());
        cliente.setDocumento(nuevo.getDocumento());
        cliente.setTelefono(nuevo.getTelefono());

        return clienteRepository.save(cliente);
    }
}
