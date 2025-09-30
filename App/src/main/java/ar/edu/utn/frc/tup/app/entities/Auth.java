package ar.edu.utn.frc.tup.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "auth")
public class Auth implements UserDetails {
    @Id
    @ColumnDefault("nextval('auth_idauth_seq')")
    @Column(name = "idauth", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Size(max = 150)
    @NotNull
    @Column(name = "mail", nullable = false, length = 150)
    private String mail;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = false;

    @OneToMany(mappedBy = "idauth", fetch = FetchType.LAZY)
    private List<Rolxusuario> rolxusuarioList;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return rolxusuarioList.stream()
                .map(ru -> new SimpleGrantedAuthority("ROLE_" + ru.getIdrol().getDescripcion().toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return mail; // Usando el email como username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}