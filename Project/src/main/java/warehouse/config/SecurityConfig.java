package warehouse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService projectUserDetailsService;

@Autowired
    public SecurityConfig(PasswordEncoder passwordEncoder, UserDetailsService projectUserDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.projectUserDetailsService = projectUserDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                authorizeRequests().
                requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll().
                antMatchers("/login**", "/login-error**", "/img/**").permitAll().
                antMatchers("/**").authenticated().
                and().
                formLogin().
                loginPage("/login").
                loginProcessingUrl("/login/authenticate").
                failureForwardUrl("/login-error").
                successForwardUrl("/home").
                and().
                logout().
                logoutUrl("/logout").
                logoutSuccessUrl("/login").
                invalidateHttpSession(true).
                deleteCookies("JSESSIONID");
    }

    @Override
    public void configure(AuthenticationManagerBuilder authManager) throws Exception {
        authManager.
                userDetailsService(projectUserDetailsService).
                passwordEncoder(passwordEncoder);
    }
}
