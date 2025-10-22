CREATE TABLE Roles (
                       idRol SERIAL PRIMARY KEY,
                       descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE Auth (
                          idAuth SERIAL PRIMARY KEY,
                          password VARCHAR(255) NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          lastName VARCHAR(255) NOT NULL,
                          mail VARCHAR(150) NOT NULL UNIQUE,
			              active BOOLEAN NOT NULL
);

CREATE TABLE Tipos_Documento (
                          idTipoDoc SERIAL PRIMARY KEY,
                          tipo VARCHAR(255) NOT NULL
);

CREATE TABLE Departamentos (
                             idDepartamento SERIAL PRIMARY KEY,
                             departamento VARCHAR(255)
);

CREATE TABLE Ciudades (
                          idCiudad SERIAL PRIMARY KEY,
                          ciudad VARCHAR(100) NOT null,
                          idDepartamento INT not null references Departamentos(idDepartamento)
);

CREATE TABLE Barrios (
                         idBarrio SERIAL PRIMARY KEY,
                         barrio VARCHAR(100) NOT NULL,
                         idCiudad INT NOT NULL REFERENCES Ciudades(idCiudad)
);

CREATE TABLE Direcciones (
                             idDireccion SERIAL PRIMARY KEY,
                             idBarrio INT NOT NULL REFERENCES Barrios(idBarrio),
                             calle VARCHAR(100) NOT NULL,
                             numero VARCHAR(10) NOT NULL,
                             piso VARCHAR(10),
                             depto VARCHAR(10),
                             observaciones VARCHAR(200)
);

create table Usuarios (
				idUsuario SERIAL primary key,			    
			    documento VARCHAR(20),
                telefono VARCHAR(20),
			    nacimiento date NOT NULL,
			    
                idDireccion INT NOT NULL REFERENCES Direcciones(idDireccion),  
                idTipoDoc INT NOT NULL REFERENCES Tipos_Documento(idTipoDoc),
				idAuth INT NOT NULL REFERENCES Auth(idAuth)
);



CREATE TABLE RolXUsuario(
                            idRolXUsuario SERIAL PRIMARY KEY,
                            idRol INT NOT NULL REFERENCES Roles(idRol),
                            idAuth INT NOT NULL REFERENCES Auth(idAuth)  -- Cambiar Usuarios(idUsuario) → Auth(idAuth)
);


CREATE TABLE Oficios (
                         idOficio SERIAL PRIMARY KEY,
                         oficio VARCHAR(100) NOT NULL
);

CREATE TABLE Profesionales (
                        idProfesional SERIAL PRIMARY KEY,                      
                        fechaDesde date NOT NULL,
                        fechaHasta date,
                        idUsuario INT NOT NULL REFERENCES Usuarios(idUsuario),
                        idOficio INT NOT NULL REFERENCES Oficios(idOficio)
);

CREATE TABLE Disponibilidad (
                                idDisponibilidad SERIAL PRIMARY KEY,
                                idProfesional INT NOT NULL REFERENCES Profesionales(idProfesional),
                                diaSemana VARCHAR(20) NOT NULL,
                                horaInicio TIME NOT NULL,
                                horaFin TIME NOT NULL
);

CREATE TABLE Solicitudes (
                             idSolicitud SERIAL PRIMARY KEY,
                             idUsuario INT NOT NULL REFERENCES Usuarios(idUsuario),  -- Cambiar Usuario → Usuarios
                             idProfesional INT NOT NULL REFERENCES Profesionales(idProfesional),
                             idOficio INT NOT NULL REFERENCES Oficios(idOficio),
                             fechaSolicitud TIMESTAMP NOT NULL DEFAULT NOW(),
                             fechaServicio TIMESTAMP NOT NULL,
                             estado VARCHAR(20) NOT NULL,
                             observacion VARCHAR(500)
);

CREATE TABLE Resenias (
                          idResenia SERIAL PRIMARY KEY,
                          idUsuario INT NOT NULL REFERENCES Usuarios(idUsuario),  -- Cambiar Usuario → Usuarios
                          idProfesional INT NOT NULL REFERENCES Profesionales(idProfesional),
                          puntuacion INT CHECK (puntuacion BETWEEN 1 AND 5),
                          comentario VARCHAR(500),
                          fecha TIMESTAMP DEFAULT NOW()
);

CREATE TABLE Mensajes (
                          idMensaje SERIAL PRIMARY KEY,
                          idSolicitud INT NOT NULL REFERENCES Solicitudes(idSolicitud),
                          idRemitente INT NOT NULL REFERENCES Usuarios(idUsuario),
                          idDestinatario INT NOT NULL REFERENCES Usuarios(idUsuario),
                          mensaje VARCHAR(500) NOT NULL,
                          fechaHora TIMESTAMP DEFAULT NOW()
);

CREATE TABLE Montos (
                        idMonto SERIAL PRIMARY KEY,
                        idProfesional INT NOT NULL REFERENCES Profesionales(idProfesional),
                        idOficio INT NOT NULL REFERENCES Oficios(idOficio),
                        precioMin NUMERIC(10,2) NOT NULL,
                        precioMax NUMERIC(10,2) NOT NULL
);



CREATE TABLE MediosDePago (
                              idMedioPago SERIAL PRIMARY KEY,
                              descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE Facturas (
                          NroFactura SERIAL PRIMARY KEY,
                          idUsuario INT NOT NULL REFERENCES Usuarios(idUsuario),  -- Cambiar Usuario → Usuarios
                          idProfesional INT NOT NULL REFERENCES Profesionales(idProfesional),
                          idMedioPago INT NOT NULL REFERENCES MediosDePago(idMedioPago),
                          fecha TIMESTAMP DEFAULT NOW(),
                          estadoPago VARCHAR(20) NOT NULL,
                          importe NUMERIC(10,2) NOT NULL
);

CREATE TABLE password_reset_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(10) NOT NULL, -- Para el código de 6 dígitos
    email VARCHAR(150) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    id_auth INT REFERENCES Usuarios(idUsuario) -- Opcional pero útil
);

-- INSERTS
INSERT INTO Departamentos (departamento) VALUES 
('CAPITAL'),
('CALAMUCHITA'),
('COLON'),
('CRUZ DEL EJE'),
('GENERAL ROCA'),
('GENERAL SAN MARTIN'),
('ISCHILIN'),
('JUAREZ CELMAN'),
('MARCOS JUAREZ'),
('MINAS'),
('POCHO'),
('PTE. ROQUE SAENZ PEÑA'),
('PUNILLA'),
('RIO CUARTO'),
('RIO PRIMERO'),
('RIO SECO'),
('RIO SEGUNDO'),
('SAN ALBERTO'),
('SAN JAVIER'),
('SAN JUSTO'),
('SANTA MARIA'),
('SOBREMONTE'),
('TERCERO ARRIBA'),
('TOTORAL'),
('TULUMBA'),
('UNION');

INSERT INTO Oficios (oficio) VALUES 
('GASISTA'),
('ELECTRICISTA'),
('PLOMERO'),
('CARPINTERO'),
('PINTOR'),
('EMPLEADA DOMESTICA'),
('INSTALADOR DE AIRES ACONDICIONADOS');


INSERT INTO Ciudades (ciudad, idDepartamento) VALUES
('Córdoba', 1),          -- CAPITAL
('Villa María', 6),      -- GENERAL SAN MARTIN
('Villa Carlos Paz', 13),-- PUNILLA
('San Francisco', 20),   -- SAN JUSTO
('Alta Gracia', 21),     -- SANTA MARIA
('Río Tercero', 23),     -- TERCERO ARRIBA
('Río Cuarto', 14),      -- RIO CUARTO
('La Calera', 3),        -- COLON
('Villa Allende', 3),    -- COLON
('Jesús María', 3),      -- COLON
('Bell Ville', 26),      -- UNION
('Cruz del Eje', 4),     -- CRUZ DEL EJE
('Villa Dolores', 19);   -- SAN JAVIER

INSERT INTO Barrios (barrio, idCiudad) VALUES
                                           ('Nueva Córdoba', 1),
                                           ('Alta Córdoba', 1),
                                           ('General Paz', 1),
                                           ('Cerro de las Rosas', 1),
                                           ('Alberdi', 1),
                                           ('San Vicente', 1),
                                           ('Villa El Libertador', 1),
                                           ('Jardín', 1),
                                           ('Centro', 2),
                                           ('San Martín', 2),
                                           ('Malvinas Argentinas', 2),
                                           ('Bello Horizonte', 2),
                                           ('Las Acacias', 2),
                                           ('Centro', 3),
                                           ('La Cuesta', 3),
                                           ('Santa Rita', 3),
                                           ('Villa del Lago', 3),
                                           ('José Muñoz', 3),
                                           ('Centro', 4),
                                           ('Parque', 4),
                                           ('La Florida', 4),
                                           ('San Cayetano', 4),
                                           ('Hospital', 4),
                                           ('Centro', 5),
                                           ('Residencial El Golf', 5),
                                           ('Paravachasca', 5),
                                           ('Sabattini', 5),
                                           ('Pellegrini', 5),
                                           ('Centro', 6),
                                           ('Cabero', 6),
                                           ('Monte Grande', 6),
                                           ('Magnasco', 6),
                                           ('Media Luna', 6),
                                           ('Centro', 7),
                                           ('Banda Norte', 7),
                                           ('Alberdi', 7),
                                           ('Fénix', 7),
                                           ('Las Ferias', 7),
                                           ('Centro', 8),
                                           ('Stoecklin', 8),
                                           ('Industrial', 8),
                                           ('Dumandzic', 8),
                                           ('La Campana', 8),
                                           ('Centro', 9),
                                           ('Chacras de la Villa', 9),
                                           ('Las Polonias', 9),
                                           ('Cerro', 9),
                                           ('El Golf', 9),
                                           ('Centro', 10),
                                           ('Malabrigo', 10),
                                           ('Barrio Norte', 10),
                                           ('La Florida', 10),
                                           ('Sierras y Parques', 10),
                                           ('Centro', 11),
                                           ('Los Lirios', 11),
                                           ('Progreso', 11),
                                           ('Martín Fierro', 11),
                                           ('Tiro Federal', 11),
                                           ('Centro', 12),
                                           ('San Martín', 12),
                                           ('Las Playas', 12),
                                           ('Los Altos', 12),
                                           ('Villa Elaine', 12),
                                           ('Centro', 13),
                                           ('Aeroclub', 13),
                                           ('Parque', 13),
                                           ('San Pablo', 13),
                                           ('Los Olivos', 13);

insert into tipos_documento (tipo) values
                                       ('DNI'),
                                       ('CUIL'),
                                       ('PASAPORTE');

-- Índices para performance
CREATE INDEX idx_reset_token_email ON password_reset_tokens(email);
CREATE INDEX idx_reset_token_expiry ON password_reset_tokens(expiry_date);

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS avatar VARCHAR(255);