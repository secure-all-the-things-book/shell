package com.example.cli;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
class ShellClientComponent {

	private final RestClient http;

	private final Granter granter;

	ShellClientComponent(RestClient.Builder http, Granter granter) {
		this.http = http.build();
		this.granter = granter;
	}

	// <.>
	@Command(description = "get a secured message")
	String message() throws Exception {
		var at = this.granter.grant().accessToken(); // <.>
		return Objects
			.requireNonNull(this.http.get()
				.uri("http://localhost:8081/message")
				.headers(h -> h.setBearerAuth(at)) // <.>
				.retrieve()
				.body(Message.class))
			.message();
	}

	record Message(String message) {
	}

}
