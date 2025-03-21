// package com.PortfolioHeatmap.security;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// public class SecurityConfig {

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//                 .csrf(csrf -> csrf.disable()) // Disable CSRF for testing
//                 .authorizeHttpRequests(auth -> auth
//                         .anyRequest().permitAll() // Allow all requests without authentication
//                 )
//                 .httpBasic(httpBasic -> httpBasic.disable()) // Disable default login prompt
//                 .formLogin(formLogin -> formLogin.disable()); // Disable form login

//         return http.build();
//     }
// }
