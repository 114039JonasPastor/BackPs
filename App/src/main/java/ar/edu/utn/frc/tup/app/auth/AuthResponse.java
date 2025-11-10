package ar.edu.utn.frc.tup.app.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    String token;
    private String nombre;
    private String apellido;
    private String email;
    private Integer idUsuario;
    private String documento;
    private String telefono;
    private String nacimiento;
    private Integer idDireccion;
    // Added to return professional id when the user is a professional
    private Integer idProfesional;
}
