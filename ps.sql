CREATE DATABASE Ps

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
                            idAuth INT NOT NULL REFERENCES Usuarios(idUsuario)
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
                             idUsuario INT NOT NULL REFERENCES Usuario(idUsuario),
                             idProfesional INT NOT NULL REFERENCES Profesionales(idProfesional),
                             idOficio INT NOT NULL REFERENCES Oficios(idOficio),
                             fechaSolicitud TIMESTAMP NOT NULL DEFAULT NOW(),
                             fechaServicio TIMESTAMP NOT NULL,
                             estado VARCHAR(20) NOT NULL, -- pendiente, aceptada, rechazada, finalizada
                             observacion VARCHAR(500)
);

CREATE TABLE Resenias (
                         idResenia SERIAL PRIMARY KEY,
                         idUsuario INT NOT NULL REFERENCES Usuario(idUsuario),
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
                          idUsuario INT NOT NULL REFERENCES Usuario(idUsuario),
                          idProfesional INT NOT NULL REFERENCES Profesionales(idProfesional),
                          idMedioPago INT NOT NULL REFERENCES MediosDePago(idMedioPago),
                          fecha TIMESTAMP DEFAULT NOW(),
                          estadoPago VARCHAR(20) NOT NULL -- pendiente, pagado, cancelado
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

-- Insert 
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

-- Índices para performance
CREATE INDEX idx_reset_token_email ON password_reset_tokens(email);
CREATE INDEX idx_reset_token_expiry ON password_reset_tokens(expiry_date);