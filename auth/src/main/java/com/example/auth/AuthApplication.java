package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

	@Bean
	Customizer<HttpSecurity> httpSecurityCustomizer(RegisteredClientRepository repository,
			AuthorizationServerSettings settings) {
		return http -> http //
			.oauth2AuthorizationServer(configurer -> {
				configurer.oidc(Customizer.withDefaults()); // <.>
				configurer.deviceAuthorizationEndpoint(Customizer.withDefaults()); // <.>
				configurer.deviceVerificationEndpoint(Customizer.withDefaults()); // <.>
				configurer.clientAuthentication(clientAuthentication -> clientAuthentication // <.>
					.authenticationConverter(
							new DeviceClientAuthenticationConverter(settings.getDeviceAuthorizationEndpoint()))
					.authenticationProvider(new DeviceClientAuthenticationProvider(repository)));
			});
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder passwordEncoder) {
		return new InMemoryUserDetailsManager(
				User.withUsername("rob").password(passwordEncoder.encode("pw")).roles("USER").build(),
				User.withUsername("josh").password(passwordEncoder.encode("pw")).roles("USER").build());
	}

}

@Controller
class SuccessController {

	// <.>
	@GetMapping("/")
	String success() {
		return "success";
	}

}
