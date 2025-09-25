CREATE DATABASE ps
CREATE TABLE Roles (
                       idRol SERIAL PRIMARY KEY,
                       descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE Usuarios (
                          idUsuario SERIAL PRIMARY KEY,
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

CREATE TABLE RolXUsuario(
                            idRolXUsuario SERIAL PRIMARY KEY,
                            idRol INT NOT NULL REFERENCES Roles(idRol),
                            idUsuario INT NOT NULL REFERENCES Usuarios(idUsuario)
);


CREATE TABLE Departamentos (
                             idDepartamento SERIAL PRIMARY KEY,
                             departamento VARCHAR(255)
);

CREATE TABLE Ciudades (
                          idCiudad SERIAL PRIMARY KEY,
                          ciudad VARCHAR(100) NOT null,
                          idDepartamento INT not null references departamentos(idDepartamento)
);

CREATE TABLE Barrios (
                         idBarrio SERIAL PRIMARY KEY,
                         barrio VARCHAR(100) NOT NULL,
                         idCiudad INT NOT NULL REFERENCES Ciudades(idCiudad)
);

CREATE TABLE Clientes (
                          idCliente SERIAL PRIMARY KEY,
			  idTipoDoc INT NOT NULL REFERENCES Tipos_Documento(idTipoDoc),
			  documento VARCHAR(20),
                          telefono VARCHAR(20),
			  nacimiento TIME NOT NULL,
                          idUsuario INT NOT NULL REFERENCES Usuarios(idUsuario)
);

CREATE TABLE Oficios (
                         idOficio SERIAL PRIMARY KEY,
                         oficio VARCHAR(100) NOT NULL
);

CREATE TABLE Profesionales (
                               idProfesional SERIAL PRIMARY KEY,
			       idTipoDoc INT NOT NULL REFERENCES Tipos_Documento(idTipoDoc),
			       documento VARCHAR(20),
                               telefono VARCHAR(20),
                               idUsuario INT NOT NULL REFERENCES Usuarios(idUsuario),
                               idOficio INT NOT NULL REFERENCES Oficios(idOficio),
			       nacimiento TIME NOT NULL,
                               fechaDesde TIME NOT NULL,
                               fechaHasta TIME
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
                             idCliente INT NOT NULL REFERENCES Clientes(idCliente),
                             idProfesional INT NOT NULL REFERENCES Profesionales(idProfesional),
                             idOficio INT NOT NULL REFERENCES Oficios(idOficio),
                             fechaSolicitud TIMESTAMP NOT NULL DEFAULT NOW(),
                             fechaServicio TIMESTAMP NOT NULL,
                             estado VARCHAR(20) NOT NULL, -- pendiente, aceptada, rechazada, finalizada
                             observacion VARCHAR(500)
);

CREATE TABLE Resenias (
                         idResenia SERIAL PRIMARY KEY,
                         idCliente INT NOT NULL REFERENCES Clientes(idCliente),
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

CREATE TABLE Direcciones (
                             idDireccion SERIAL PRIMARY KEY,
                             idUsuario INT NOT NULL REFERENCES Usuarios(idUsuario),
                             idBarrio INT NOT NULL REFERENCES Barrios(idBarrio),
                             calle VARCHAR(100) NOT NULL,
                             numero VARCHAR(10) NOT NULL,
                             piso VARCHAR(10),
                             depto VARCHAR(10),
                             observaciones VARCHAR(200)
);

CREATE TABLE MediosDePago (
                              idMedioPago SERIAL PRIMARY KEY,
                              descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE Facturas (
                          NroFactura SERIAL PRIMARY KEY,
                          idCliente INT NOT NULL REFERENCES Clientes(idCliente),
                          idProfesional INT NOT NULL REFERENCES Profesionales(idProfesional),
                          idMedioPago INT NOT NULL REFERENCES MediosDePago(idMedioPago),
                          fecha TIMESTAMP DEFAULT NOW(),
                          estadoPago VARCHAR(20) NOT NULL -- pendiente, pagado, cancelado
);