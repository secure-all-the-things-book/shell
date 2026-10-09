package com.example.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    Customizer<HttpSecurity> httpSecurityCustomizer() {
        return http -> http //
                .oauth2AuthorizationServer(configurer -> {
                    configurer.oidc(Customizer.withDefaults()); //
                    configurer.deviceAuthorizationEndpoint(Customizer.withDefaults()); //
                    configurer.deviceVerificationEndpoint(Customizer.withDefaults()); //
                });
    }

    @Controller
    static class SuccessController {

        @GetMapping ("/")
        void success (HttpServletResponse response) throws Exception {
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.getWriter().write("""
                    <html> 
                     <head> 
                      <title> 
                       [hacker voice]: You're In!
                      </title>
                     </head>
                     <body>
                      <P> Success!</P>
                     </body>
                    </html>
                    """);

        }

    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("rob")
                        .password(passwordEncoder.encode("pw"))
                        .roles("USER").build(),
                User.withUsername("josh")
                        .password(passwordEncoder.encode("pw"))
                        .roles("USER")
                        .build()
        );
    }

}
