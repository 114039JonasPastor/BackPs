-- Tabla de reportes de profesionales
CREATE TABLE IF NOT EXISTS reportes (
    idreporte INT AUTO_INCREMENT PRIMARY KEY,
    idprofesional INT NOT NULL,
    reportado_por INT,
    razon VARCHAR(500) NOT NULL,
    fecha_reporte TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atendido BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_atencion TIMESTAMP NULL,
    resolucion VARCHAR(1000),
    FOREIGN KEY (idprofesional) REFERENCES profesionales(idprofesional) ON DELETE CASCADE,
    FOREIGN KEY (reportado_por) REFERENCES usuarios(idusuario) ON DELETE SET NULL,
    INDEX idx_profesional (idprofesional),
    INDEX idx_atendido (atendido),
    INDEX idx_fecha_reporte (fecha_reporte)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
