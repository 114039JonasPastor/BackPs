package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.trabajo.FinalizarTrabajoRequest;
import ar.edu.utn.frc.tup.app.dtos.response.trabajo.TrabajoClienteResponse;
import ar.edu.utn.frc.tup.app.dtos.response.trabajo.TrabajoResponse;
import ar.edu.utn.frc.tup.app.entities.Solicitude;
import ar.edu.utn.frc.tup.app.entities.Trabajo;
import ar.edu.utn.frc.tup.app.repositories.SolicitudeRepository;
import ar.edu.utn.frc.tup.app.repositories.TrabajoRepository;
import ar.edu.utn.frc.tup.app.services.TrabajoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrabajoServiceImpl implements TrabajoService {

    private final TrabajoRepository trabajoRepository;
    private final SolicitudeRepository solicitudRepository;

    @Override
    @Transactional
    public Trabajo crearTrabajo(Integer idSolicitud) {
        log.info("Creando trabajo para solicitud: {}", idSolicitud);

        Solicitude solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Verificar que la solicitud esté aceptada
        if (!"ACEPTADA".equals(solicitud.getEstado())) {
            throw new RuntimeException("Solo se pueden crear trabajos de solicitudes aceptadas");
        }

        // Verificar que no exista ya un trabajo para esta solicitud
        if (trabajoRepository.findBySolicitud_Id(idSolicitud).isPresent()) {
            throw new RuntimeException("Ya existe un trabajo para esta solicitud");
        }

        Trabajo trabajo = Trabajo.builder()
                .solicitud(solicitud)
                .estado("PENDIENTE")
                .build();

        Trabajo trabajoGuardado = trabajoRepository.save(trabajo);
        log.info("Trabajo creado con ID: {}", trabajoGuardado.getId());

        return trabajoGuardado;
    }

    @Override
    @Transactional
    public TrabajoResponse iniciarTrabajo(Integer idTrabajo) {
        log.info("Iniciando trabajo: {}", idTrabajo);

        Trabajo trabajo = trabajoRepository.findById(idTrabajo)
                .orElseThrow(() -> new RuntimeException("Trabajo no encontrado"));

        if (!"PENDIENTE".equals(trabajo.getEstado())) {
            throw new RuntimeException("Solo se pueden iniciar trabajos pendientes");
        }

        trabajo.setEstado("EN_CURSO");
        trabajo.setFechaInicio(Instant.now());

        Trabajo trabajoActualizado = trabajoRepository.save(trabajo);
        log.info("Trabajo {} iniciado", idTrabajo);

        return mapearATrabajoResponse(trabajoActualizado);
    }

    @Override
    @Transactional
    public TrabajoResponse pausarTrabajo(Integer idTrabajo) {
        log.info("Pausando trabajo: {}", idTrabajo);

        Trabajo trabajo = trabajoRepository.findById(idTrabajo)
                .orElseThrow(() -> new RuntimeException("Trabajo no encontrado"));

        if (!"EN_CURSO".equals(trabajo.getEstado())) {
            throw new RuntimeException("Solo se pueden pausar trabajos en curso");
        }

        trabajo.setEstado("PAUSADO");
        Trabajo trabajoActualizado = trabajoRepository.save(trabajo);
        log.info("Trabajo {} pausado", idTrabajo);

        return mapearATrabajoResponse(trabajoActualizado);
    }

    @Override
    @Transactional
    public TrabajoResponse reanudarTrabajo(Integer idTrabajo) {
        log.info("Reanudando trabajo: {}", idTrabajo);

        Trabajo trabajo = trabajoRepository.findById(idTrabajo)
                .orElseThrow(() -> new RuntimeException("Trabajo no encontrado"));

        if (!"PAUSADO".equals(trabajo.getEstado())) {
            throw new RuntimeException("Solo se pueden reanudar trabajos pausados");
        }

        trabajo.setEstado("EN_CURSO");
        Trabajo trabajoActualizado = trabajoRepository.save(trabajo);
        log.info("Trabajo {} reanudado", idTrabajo);

        return mapearATrabajoResponse(trabajoActualizado);
    }

    @Override
    @Transactional
    public TrabajoResponse finalizarTrabajo(Integer idTrabajo, FinalizarTrabajoRequest request) {
        log.info("Finalizando trabajo: {}", idTrabajo);

        Trabajo trabajo = trabajoRepository.findById(idTrabajo)
                .orElseThrow(() -> new RuntimeException("Trabajo no encontrado"));

        if (!"EN_CURSO".equals(trabajo.getEstado()) && !"PAUSADO".equals(trabajo.getEstado())) {
            throw new RuntimeException("Solo se pueden finalizar trabajos en curso o pausados");
        }

        // Validar que tenga monto
        if (request.getMontoFinal() == null || request.getMontoFinal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Debe especificar un monto válido");
        }

        trabajo.setEstado("FINALIZADO");
        trabajo.setFechaFinalizacion(Instant.now());
        trabajo.setDuracionReal(request.getDuracionReal());
        trabajo.setMontoFinal(request.getMontoFinal());
        trabajo.setMontoAdicional(request.getMontoAdicional());
        trabajo.setDescripcionAdicional(request.getDescripcionAdicional());
        trabajo.setFotoTrabajo(request.getFotoTrabajo());
        trabajo.setObservacionesTrabajo(request.getObservaciones());

        Trabajo trabajoActualizado = trabajoRepository.save(trabajo);
        log.info("Trabajo {} finalizado con monto: {}", idTrabajo, request.getMontoFinal());

        return mapearATrabajoResponse(trabajoActualizado);
    }

    @Override
    @Transactional
    public TrabajoResponse cancelarTrabajo(Integer idTrabajo, String motivoCancelacion) {
        log.info("Cancelando trabajo: {}", idTrabajo);

        Trabajo trabajo = trabajoRepository.findById(idTrabajo)
                .orElseThrow(() -> new RuntimeException("Trabajo no encontrado"));

        if ("FINALIZADO".equals(trabajo.getEstado()) || "CANCELADO".equals(trabajo.getEstado())) {
            throw new RuntimeException("No se puede cancelar un trabajo finalizado o ya cancelado");
        }

        // No se puede cancelar si ya tiene factura
        if (trabajo.getFactura() != null) {
            throw new RuntimeException("No se puede cancelar un trabajo que ya tiene factura asociada");
        }

        trabajo.setEstado("CANCELADO");
        trabajo.setFechaCancelacion(Instant.now());
        trabajo.setObservacionesCancelacion(motivoCancelacion);

        Trabajo trabajoActualizado = trabajoRepository.save(trabajo);
        log.info("Trabajo {} cancelado", idTrabajo);

        return mapearATrabajoResponse(trabajoActualizado);
    }

    @Override
    public TrabajoResponse obtenerTrabajoPorId(Integer idTrabajo) {
        log.info("Obteniendo trabajo: {}", idTrabajo);

        Trabajo trabajo = trabajoRepository.findById(idTrabajo)
                .orElseThrow(() -> new RuntimeException("Trabajo no encontrado"));

        return mapearATrabajoResponse(trabajo);
    }

    @Override
    public TrabajoResponse obtenerTrabajoPorSolicitud(Integer idSolicitud) {
        log.info("Obteniendo trabajo por solicitud: {}", idSolicitud);

        Trabajo trabajo = trabajoRepository.findBySolicitud_Id(idSolicitud)
                .orElseThrow(() -> new RuntimeException("No existe trabajo para esta solicitud"));

        return mapearATrabajoResponse(trabajo);
    }

    @Override
    public List<TrabajoResponse> obtenerTrabajosPorProfesional(Integer idProfesional) {
        List<Trabajo> trabajos = trabajoRepository.findByProfesional_Id(idProfesional);

        if(trabajos.isEmpty()){
            throw new RuntimeException("No existen trabajos para este profesional");
        } else {
            return trabajos.stream()
                    .map(this::mapearATrabajoResponse)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<TrabajoResponse> obtenerTrabajosPorProfesionalyEstado(Integer idProfesional, String estado) {
        log.info("Obteniendo trabajos del profesional {} con estado {}", idProfesional, estado);

        List<Trabajo> trabajos;

        if (estado != null && !estado.isEmpty()) {
            trabajos = trabajoRepository.findByProfesionalAndEstado(idProfesional, estado);
        } else {
            trabajos = trabajoRepository.findByProfesionalAndEstado(idProfesional, null);
        }

        return trabajos.stream()
                .map(this::mapearATrabajoResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrabajoResponse> obtenerTrabajosPorUsuario(Integer idUsuario, String estado) {
        log.info("Obteniendo trabajos del usuario {} con estado {}", idUsuario, estado);

        List<Trabajo> trabajos = trabajoRepository.findByUsuario(idUsuario);

        if (estado != null && !estado.isEmpty()) {
            trabajos = trabajos.stream()
                    .filter(t -> t.getEstado().equals(estado))
                    .collect(Collectors.toList());
        }

        return trabajos.stream()
                .map(this::mapearATrabajoResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrabajoResponse> obtenerTrabajosSinFactura() {
        log.info("Obteniendo trabajos finalizados sin factura");

        List<Trabajo> trabajos = trabajoRepository.findTrabajosFinalizadosSinFactura();

        return trabajos.stream()
                .map(this::mapearATrabajoResponse)
                .collect(Collectors.toList());
    }


    @Override
    public List<TrabajoClienteResponse> obtenerTrabajosFinalizadosPorCliente(Integer idUsuario) {
        log.info("Obteniendo trabajos finalizados para el cliente: {}", idUsuario);

        List<Trabajo> trabajos = trabajoRepository.findByUsuarioAndEstado(idUsuario, "FINALIZADO");

        if (trabajos.isEmpty()) {
            log.warn("No se encontraron trabajos finalizados para el cliente: {}", idUsuario);
            return List.of();
        }

        return trabajos.stream()
                .map(trabajo -> {
                    Solicitude solicitud = trabajo.getSolicitud();
                    String nombreProfesional = solicitud.getIdprofesional().getIdusuario().getIdauth().getName() + " " +
                            solicitud.getIdprofesional().getIdusuario().getIdauth().getLastname();

                    return TrabajoClienteResponse.builder()
                            .idTrabajo(trabajo.getId())
                            .idSolicitud(solicitud.getId())
                            .profesional(nombreProfesional)
                            .descripcion(trabajo.getObservacionesTrabajo())
                            .idpago(trabajo.getIdpago())
                            .estado(trabajo.getEstado())
                            .montoFinal(trabajo.getMontoFinal() != null ? trabajo.getMontoFinal().toString() : null)
                            .fechaFinalizacion(trabajo.getFechaFinalizacion())
                            .estadoPago(trabajo.getFactura() != null ? trabajo.getFactura().getEstadopago() : "PENDIENTE") // ⭐ AGREGAR
                            .build();
                })
                .sorted((t1, t2) -> {
                    if (t1.getFechaFinalizacion() == null && t2.getFechaFinalizacion() == null) return 0;
                    if (t1.getFechaFinalizacion() == null) return 1;
                    if (t2.getFechaFinalizacion() == null) return -1;
                    return t2.getFechaFinalizacion().compareTo(t1.getFechaFinalizacion());
                })
                .collect(Collectors.toList());
    }

    // Método helper para mapear entidad a DTO
    private TrabajoResponse mapearATrabajoResponse(Trabajo trabajo) {
        Solicitude solicitud = trabajo.getSolicitud();

        return TrabajoResponse.builder()
                .idTrabajo(trabajo.getId())
                .idSolicitud(solicitud.getId())
                .estado(trabajo.getEstado())
                .nombreCliente(solicitud.getIdusuario().getIdauth().getName() + " " +
                        solicitud.getIdusuario().getIdauth().getLastname())
                .nombreProfesional(solicitud.getIdprofesional().getIdusuario().getIdauth().getName() + " " +
                        solicitud.getIdprofesional().getIdusuario().getIdauth().getLastname())
                .oficio(solicitud.getIdoficio().getOficio())
                .fechaInicio(trabajo.getFechaInicio())
                .fechaFinalizacion(trabajo.getFechaFinalizacion())
                .duracionReal(trabajo.getDuracionReal())
                .montoFinal(trabajo.getMontoFinal())
                .montoAdicional(trabajo.getMontoAdicional())
                .descripcionAdicional(trabajo.getDescripcionAdicional())
                .fotoTrabajo(trabajo.getFotoTrabajo())
                .observacionesTrabajo(trabajo.getObservacionesTrabajo())
                .idFactura(trabajo.getFactura() != null ? trabajo.getFactura().getId() : null)
                .estadoPago(trabajo.getFactura() != null ? trabajo.getFactura().getEstadopago() : null)
                .build();
    }
}
