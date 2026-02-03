-- Agregar columnas para manejo de turnos en solicitudes
ALTER TABLE solicitudes
ADD COLUMN IF NOT EXISTS es_turno BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS duracion_estimada INTEGER,
ADD COLUMN IF NOT EXISTS hora_reserva VARCHAR(10);

-- Comentarios para documentación
COMMENT ON COLUMN solicitudes.es_turno IS 'Indica si la solicitud es un turno agendado con horario específico';
COMMENT ON COLUMN solicitudes.duracion_estimada IS 'Duración estimada del servicio en minutos';
COMMENT ON COLUMN solicitudes.hora_reserva IS 'Hora de reserva del turno en formato HH:mm:ss';
