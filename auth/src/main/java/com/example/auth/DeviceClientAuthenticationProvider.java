package com.example.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

class DeviceClientAuthenticationProvider implements AuthenticationProvider {

	private final RegisteredClientRepository repository;

	DeviceClientAuthenticationProvider(RegisteredClientRepository repository) {
		this.repository = repository;
	}

	@Override
	public Authentication authenticate(Authentication authentication) {
		var clientAuthentication = (OAuth2ClientAuthenticationToken) authentication;
		// <.>
		if (!ClientAuthenticationMethod.NONE.equals(clientAuthentication.getClientAuthenticationMethod())
				|| clientAuthentication.getAdditionalParameters().containsKey(PkceParameterNames.CODE_VERIFIER)) {
			return null;
		}
		var registeredClient = this.repository.findByClientId(clientAuthentication.getPrincipal().toString());
		// <.>
		if (registeredClient == null
				|| !registeredClient.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE)
				|| !registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.DEVICE_CODE)) {
			throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
		}
		return new OAuth2ClientAuthenticationToken(registeredClient, ClientAuthenticationMethod.NONE, null); // <.>
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
