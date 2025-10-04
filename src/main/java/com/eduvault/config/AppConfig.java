package com.eduvault.config;


import com.eduvault.user.repo.UserRepository;
import com.eduvault.user.service.UserInfoUserDetailsService;
import com.eduvault.user.UserInfoUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {
    private final UserRepository userRepository;
    private final UserInfoUserDetailsService userInfoUserDetailsService;


    public AppConfig(UserRepository userRepository, UserInfoUserDetailsService userInfoUserDetailsService) {
        this.userRepository = userRepository;
        this.userInfoUserDetailsService = userInfoUserDetailsService;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Supports both email and matricNumber
        return identifier -> userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByMatricNumber(identifier))
                .map(UserInfoUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
