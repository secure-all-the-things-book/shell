package com.example.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

class DeviceClientAuthenticationConverter implements AuthenticationConverter {

	private final RequestMatcher deviceAuthorizationRequestMatcher;

	private final RequestMatcher deviceAccessTokenRequestMatcher;

	DeviceClientAuthenticationConverter(String deviceAuthorizationEndpointUri) {
		// <.>
		this.deviceAuthorizationRequestMatcher = PathPatternRequestMatcher.withDefaults()
			.matcher(HttpMethod.POST, deviceAuthorizationEndpointUri);
		// <.>
		this.deviceAccessTokenRequestMatcher = request -> AuthorizationGrantType.DEVICE_CODE.getValue()
			.equals(request.getParameter(OAuth2ParameterNames.GRANT_TYPE))
				&& request.getParameter(OAuth2ParameterNames.DEVICE_CODE) != null;
	}

	@Override
	public Authentication convert(HttpServletRequest request) {
		if (!this.deviceAuthorizationRequestMatcher.matches(request)
				&& !this.deviceAccessTokenRequestMatcher.matches(request)) {
			return null;
		}
		// <.>
		if (request.getHeader(HttpHeaders.AUTHORIZATION) != null
				|| request.getParameter(OAuth2ParameterNames.CLIENT_SECRET) != null) {
			return null;
		}
		var clientIds = request.getParameterValues(OAuth2ParameterNames.CLIENT_ID);
		if (clientIds == null || clientIds.length != 1 || !StringUtils.hasText(clientIds[0])) {
			throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
		}
		return new OAuth2ClientAuthenticationToken(clientIds[0], ClientAuthenticationMethod.NONE, null, null); // <.>
	}

}
