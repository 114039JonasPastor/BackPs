package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.registro.NuevoCliente;
import ar.edu.utn.frc.tup.app.dtos.request.registro.NuevoProfesional;
import ar.edu.utn.frc.tup.app.entities.*;
import ar.edu.utn.frc.tup.app.repositories.*;
import ar.edu.utn.frc.tup.app.services.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistroServiceImpl implements RegistroService {

    @Autowired
    UsuarioRepository usuarioRepository;


    @Autowired
    ProfesionalRepository profesionalRepository;

    @Autowired
    TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    DireccionRepository direccionRepository;

    @Autowired
    OficioRepository oficioRepository;

//    @Override
//    public Profesionale registrarProfesional(NuevoProfesional nuevo) {
//        Direccione direccion = direccionRepository.findById(nuevo.getIdDireccion()).orElse(null);
//        Usuario usuario = usuarioRepository.findById(nuevo.getIdUsuario()).orElse(null);
//        TiposDocumento tipo = tipoDocumentoRepository.findById(nuevo.getIdTipoDoc()).orElse(null);
//        Oficio oficio = oficioRepository.findById(nuevo.getIdoficio()).orElse(null);
//
//        Profesionale profesionale = new Profesionale();
//
//        profesionale.setIdtipodoc(tipo);
//        profesionale.setIdusuario(usuario);
//        profesionale.setIddireccion(direccion);
//        profesionale.setNacimiento(nuevo.getFechaNacimiento());
//        profesionale.setFechadesde(nuevo.getFechaDesde());
//        profesionale.setFechahasta(nuevo.getFechaHasta());
//        profesionale.setDocumento(nuevo.getDocumento());
//        profesionale.setTelefono(nuevo.getTelefono());
//        profesionale.setIdoficio(oficio);
//
//        return profesionalRepository.save(profesionale);
//    }
//
//    @Override
//    public Cliente registrarCliente(NuevoCliente nuevo) {
//        Direccione direccion = direccionRepository.findById(nuevo.getIdDireccion()).orElse(null);
//        Usuario usuario = usuarioRepository.findById(nuevo.getIdUsuario()).orElse(null);
//        TiposDocumento tipo = tipoDocumentoRepository.findById(nuevo.getIdTipoDoc()).orElse(null);
//
//        Cliente cliente = new Cliente();
//        cliente.setIdtipodoc(tipo);
//        cliente.setIdusuario(usuario);
//        cliente.setIddireccion(direccion);
//        cliente.setNacimiento(nuevo.getFechaNacimiento());
//        cliente.setDocumento(nuevo.getDocumento());
//        cliente.setTelefono(nuevo.getTelefono());
//
//        return clienteRepository.save(cliente);
//    }
}
