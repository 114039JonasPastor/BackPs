-- Insertar tipos de documento si no existen
INSERT INTO tipos_documento (tipo) 
SELECT 'DNI' WHERE NOT EXISTS (SELECT 1 FROM tipos_documento WHERE tipo = 'DNI');

INSERT INTO tipos_documento (tipo) 
SELECT 'CUIL' WHERE NOT EXISTS (SELECT 1 FROM tipos_documento WHERE tipo = 'CUIL');

INSERT INTO tipos_documento (tipo) 
SELECT 'PASAPORTE' WHERE NOT EXISTS (SELECT 1 FROM tipos_documento WHERE tipo = 'PASAPORTE');
