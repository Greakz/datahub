package com.datahub.authentication.authenticator;

import com.datahub.authentication.Actor;
import com.datahub.authentication.ActorType;
import com.datahub.authentication.Authentication;
import com.datahub.authentication.AuthenticationException;
import com.datahub.authentication.AuthenticationRequest;
import com.datahub.authentication.AuthenticatorContext;
import com.datahub.plugins.auth.authentication.Authenticator;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.datahub.authentication.AuthenticationConstants.SYSTEM_CLIENT_ID_CONFIG;


/**
 * This Authenticator is used for allowing access for unauthenticated health check endpoints
 *
 * It exists to support load balancers, liveness/readiness checks
 *
 */
@Slf4j
public class HealthStatusAuthenticator implements Authenticator {
  private static final Set<String> HEALTH_ENDPOINTS = Set.of(
          "/openapi/check/",
          "/openapi/up/"
  );
  private String systemClientId;

  @Override
  public void init(@Nonnull final Map<String, Object> config, @Nullable final AuthenticatorContext context) {
    Objects.requireNonNull(config, "Config parameter cannot be null");
    this.systemClientId = Objects.requireNonNull((String) config.get(SYSTEM_CLIENT_ID_CONFIG),
            String.format("Missing required config %s", SYSTEM_CLIENT_ID_CONFIG));
  }

  @Override
  public Authentication authenticate(@Nonnull AuthenticationRequest context) throws AuthenticationException {
    Objects.requireNonNull(context);
    if (HEALTH_ENDPOINTS.stream().anyMatch(prefix -> String.join("", context.getServletInfo(), context.getPathInfo()).startsWith(prefix))) {
      return new Authentication(
              new Actor(ActorType.USER, systemClientId),
              "",
              Collections.emptyMap()
      );
    }
    throw new AuthenticationException("Authorization not allowed. Non-health check endpoint.");
  }
}
