-- Tabla de reportes de profesionales
CREATE TABLE IF NOT EXISTS reportes (
    idreporte SERIAL PRIMARY KEY,
    idprofesional INT NOT NULL,
    reportado_por INT,
    razon VARCHAR(500) NOT NULL,
    fecha_reporte TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atendido BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_atencion TIMESTAMP NULL,
    resolucion VARCHAR(1000),
    FOREIGN KEY (idprofesional) REFERENCES profesionales(idprofesional) ON DELETE CASCADE,
    FOREIGN KEY (reportado_por) REFERENCES usuarios(idusuario) ON DELETE SET NULL
);

CREATE INDEX idx_profesional ON reportes(idprofesional);
CREATE INDEX idx_atendido ON reportes(atendido);
CREATE INDEX idx_fecha_reporte ON reportes(fecha_reporte);
