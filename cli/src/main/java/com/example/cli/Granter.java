package com.example.cli;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.Map;

@Component
class Granter {

	private final RestClient http;

	private final ClientRegistration registration;

	Granter(InMemoryClientRegistrationRepository repository, RestClient.Builder http) {
		this.registration = repository.findByRegistrationId("messages"); // <.>
		this.http = http.defaultHeaders(headers -> headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)) // <.>
			.build();
	}

	Grant grant() throws Exception {
		// <.>
		var json = this.http.post()
			.uri(this.registration.getProviderDetails().getConfigurationMetadata().get("device_authorization_endpoint")
					+ "")
			.body(MultiValueMap.fromSingleValue(Map.of("scope", String.join(" ", this.registration.getScopes()),
					"client_id", this.registration.getClientId())))
			.retrieve()
			.body(Map.class);
		var uri = json.get("verification_uri_complete") + ""; // <.>
		var deviceCode = json.get("device_code") + "";
		IO.println("please go to " + uri); // <.>
		while (true) { // <.>
			try {
				Thread.sleep(Duration.ofSeconds(5));
				return new Grant(uri, this.token(deviceCode));
			}
			catch (Throwable throwable) { // <.>
				IO.println("got an error!! " + throwable.getLocalizedMessage());
				// meh
			}
		}
	}

	private String token(String deviceCode) {
		var tokenJson = http.post()
			.uri(this.registration.getProviderDetails().getTokenUri())
			.body(MultiValueMap.fromSingleValue(Map.of("client_id", this.registration.getClientId(), "device_code",
					deviceCode, "grant_type", AuthorizationGrantType.DEVICE_CODE.getValue()))) // <.>
			.retrieve()
			.body(JsonNode.class);
		return tokenJson.get("access_token").asString();
	}

}
