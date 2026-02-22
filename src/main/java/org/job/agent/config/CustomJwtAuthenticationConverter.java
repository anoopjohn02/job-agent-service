package org.job.agent.config;

import org.job.agent.model.LoggedInUser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class CustomJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        String userId = jwt.getSubject(); // sub
        String email  = jwt.getClaimAsString("email");
        String name  = jwt.getClaimAsString("name");
        List<String> roles = getRoles(jwt);

        LoggedInUser user = new LoggedInUser(userId, email, name, roles);

        List<? extends GrantedAuthority> authorities =
                roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList();

        return new UsernamePasswordAuthenticationToken(user, jwt, authorities);
    }

    private List<String> getRoles(Jwt jwt) {
        Map<String, Object> realmAccess =
                jwt.getClaim("realm_access");

        List<String> realmRoles = realmAccess != null
                ? (List<String>) realmAccess.getOrDefault("roles", List.of())
                : List.of();

        Map<String, Object> resourceAccess =
                jwt.getClaim("resource_access");

        List<String> clientRoles = List.of();

        if (resourceAccess != null && resourceAccess.containsKey("account")) {
            Map<String, Object> account =
                    (Map<String, Object>) resourceAccess.get("account");

            clientRoles =
                    (List<String>) account.getOrDefault("roles", List.of());
        }

        // -------- MERGE ROLES --------
        return Stream
                .concat(realmRoles.stream(), clientRoles.stream())
                .distinct()
                .toList();
    }
}

