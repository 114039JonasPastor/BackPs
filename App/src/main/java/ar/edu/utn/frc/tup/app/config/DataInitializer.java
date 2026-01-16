package ar.edu.utn.frc.tup.app.config;

import ar.edu.utn.frc.tup.app.entities.TiposDocumento;
import ar.edu.utn.frc.tup.app.repositories.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeTiposDocumento();
    }

    private void initializeTiposDocumento() {
        if (tipoDocumentoRepository.count() == 0) {
            log.info("Inicializando tipos de documento...");
            
            TiposDocumento dni = new TiposDocumento();
            dni.setTipo("DNI");
            tipoDocumentoRepository.save(dni);

            TiposDocumento cuil = new TiposDocumento();
            cuil.setTipo("CUIL");
            tipoDocumentoRepository.save(cuil);

            TiposDocumento pasaporte = new TiposDocumento();
            pasaporte.setTipo("PASAPORTE");
            tipoDocumentoRepository.save(pasaporte);

            log.info("Tipos de documento inicializados correctamente");
        } else {
            log.info("Tipos de documento ya existen en la base de datos");
        }
    }
}
