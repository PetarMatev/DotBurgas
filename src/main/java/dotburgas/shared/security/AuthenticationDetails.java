package dotburgas.shared.security;

import dotburgas.user.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
// this class preserves the details of the logged in user.
public class AuthenticationDetails implements UserDetails {

    private UUID userId;
    private String username;
    private String password;
    private UserRole role;

    // This method is used by Spring Security to understand which Roles/authorities our USER has.
    // Authority - Permission or Role

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // hasRole("ADMIN") - "ROLE_ADMIN"
        // hasAuthority("ADMIN") -> "ADMIN" - check if we have authority that is called admin.

        // if we are to use .requestMatchers("/admin/users").hasRole("ADMIN") ---> then we need "ROLE_ADMIN"
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
