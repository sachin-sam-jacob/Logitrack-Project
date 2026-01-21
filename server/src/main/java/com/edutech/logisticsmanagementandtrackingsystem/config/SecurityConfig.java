package com.edutech.logisticsmanagementandtrackingsystem.config;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
// import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.edutech.logisticsmanagementandtrackingsystem.jwt.JwtRequestFilter;
 


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final PasswordEncoder passwordEncoder;
 
    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService,
                          JwtRequestFilter jwtRequestFilter,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
        this.passwordEncoder = passwordEncoder;
    }
 
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }
 
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .authorizeRequests()
                //forgot-password endpoints
                .antMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/forgot-password/check").permitAll()
                .antMatchers(HttpMethod.POST, "/api/forgot-password/request").permitAll()
                .antMatchers(HttpMethod.POST, "/api/forgot-password/reset").permitAll()
                // Public endpoints
                .antMatchers(HttpMethod.POST, "/api/register").permitAll()
                .antMatchers(HttpMethod.POST, "/api/login").permitAll()
                .antMatchers(HttpMethod.POST, "/api/resend-otp").permitAll()
                .antMatchers(HttpMethod.POST, "/api/verify-otp").permitAll()
                .antMatchers(HttpMethod.POST, "/api/create-admin").permitAll()
                .antMatchers(HttpMethod.POST, "/api/google-signin").permitAll()

                // User details endpoints
                .antMatchers(HttpMethod.POST, "/api/user-details/business").permitAll()
                .antMatchers(HttpMethod.POST, "/api/user-details/driver").permitAll()
                .antMatchers(HttpMethod.POST, "/api/user-details/customer").permitAll()
                .antMatchers(HttpMethod.GET, "/api/user-details/check-completion").permitAll()
                .antMatchers(HttpMethod.GET, "/api/user-details/business").permitAll()
                .antMatchers(HttpMethod.GET, "/api/user-details/driver").permitAll()
                .antMatchers(HttpMethod.GET, "/api/user-details/customer").permitAll()
                .antMatchers(HttpMethod.POST, "/api/user-details/driver/toggle-availability").hasAuthority("DRIVER")
                
                // Admin endpoints
                .antMatchers("/api/admin/**").hasAuthority("ADMIN")
                
                // Business endpoints
                .antMatchers(HttpMethod.POST, "/api/business/cargo").hasAuthority("BUSINESS")
                .antMatchers(HttpMethod.GET, "/api/business/drivers").hasAuthority("BUSINESS")
                .antMatchers(HttpMethod.GET, "/api/business/cargo").hasAuthority("BUSINESS")
                .antMatchers(HttpMethod.POST, "/api/business/assign-cargo").hasAuthority("BUSINESS")
                .antMatchers(HttpMethod.GET, "/api/business/my-cargos").hasAuthority("BUSINESS")
                .antMatchers(HttpMethod.GET, "/api/business/search-cargo").hasAuthority("BUSINESS")
                .antMatchers(HttpMethod.GET, "/api/business/available-drivers").hasAuthority("BUSINESS")
                .antMatchers(HttpMethod.POST, "/api/business/approve-delivery").hasAuthority("BUSINESS")
                .antMatchers(HttpMethod.GET, "/api/business/pending-approvals").hasAuthority("BUSINESS")
                
                // Driver endpoints
                .antMatchers(HttpMethod.GET, "/api/driver/cargo").hasAuthority("DRIVER")
                .antMatchers(HttpMethod.GET, "/api/driver/pending-requests").hasAuthority("DRIVER")
                .antMatchers(HttpMethod.POST, "/api/driver/accept-cargo").hasAuthority("DRIVER")
                .antMatchers(HttpMethod.POST, "/api/driver/reject-cargo").hasAuthority("DRIVER")
                .antMatchers(HttpMethod.PUT, "/api/driver/update-cargo-status").hasAuthority("DRIVER")
                .antMatchers(HttpMethod.PUT, "/api/driver/update-status").hasAuthority("DRIVER")
                .antMatchers(HttpMethod.GET, "/api/driver/verification-status").hasAuthority("DRIVER")
                .antMatchers(HttpMethod.POST, "/api/driver/verify-delivery-otp").hasAuthority("DRIVER")

                
                // Customer endpoints
                .antMatchers(HttpMethod.GET, "/api/customer/cargo-status").hasAuthority("CUSTOMER")
                .antMatchers(HttpMethod.GET, "/api/customer/my-cargos").permitAll()
                .antMatchers(HttpMethod.GET, "/api/customer/search-cargo").permitAll()
                .antMatchers(HttpMethod.GET, "/api/customer/track-by-number").permitAll() // NEW
                .antMatchers(HttpMethod.POST, "/api/customer/verify-delivery-otp").permitAll() // NEW
                
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
 
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }
 
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}