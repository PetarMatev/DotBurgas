package dotburgas.shared.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableMethodSecurity
public class WebMvcConfiguration implements WebMvcConfigurer {

    // SecurityFilterChain is a way in which Spring Security understands how to apply to our app.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        // AuthorisedHttpRequests - config for group of endpoints
        // RequestMatcher - giving us access to specific endpoint
        // PermitAll() - everyone can access this endpoint
        // .anyRequests - any requests that have not been requested.
        // .authenticated() - to gain access you need to be authenticated.
        httpSecurity
                .authorizeHttpRequests(matchers -> matchers.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // access to static resources
                        .requestMatchers("/", "/register", "/discover-burgas", "/accommodation", "/about", "/contact", "/privacy").permitAll()
                        .requestMatchers("/css/**", "/img/**", "/js/**", "/static/**", "/webjars/**").permitAll() // Public assets
                        .requestMatchers("/reservation-request").hasAuthority("ROLE_USER")
                        .requestMatchers("/user-reservations").hasAuthority("ROLE_USER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/")
                );

        return httpSecurity.build();
    }
}
