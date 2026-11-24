/*
 * Email Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.email.api;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClientFactory;
import app.krista.extensions.essentials.collaboration.email.util.Strings;
import app.krista.ksdk.context.RequestContext;
import com.github.scribejava.core.model.OAuth2AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Objects;

@Path("/")
public class AuthenticationResource {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationResource.class);

    private static final String CODE = "code";
    private static final String STATE = "state";

    private final EmailAttributes emailAttributes;
    private final Invoker invoker;
    private final EmailAttributeStore emailAttributeStore;
    private final RequestContext requestContext;

    @Inject
    public AuthenticationResource(EmailAttributes emailAttributes, Invoker invoker,
                                  EmailAttributeStore emailAttributeStore, RequestContext requestContext) {
        this.emailAttributes = emailAttributes;
        this.invoker = invoker;
        this.emailAttributeStore = emailAttributeStore;
        this.requestContext = requestContext;
    }

    @GET
    @Path("/callback")
    public Response callback(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        logger.debug("callback called");
        AuthClient authClient = AuthClientFactory.getClient(emailAttributes, emailAttributeStore, requestContext.getExecutionMode());
        OAuth2AccessToken oAuth2AccessToken = authClient.getRefreshToken(code);
        if (!Objects.isNull(oAuth2AccessToken)) {
            EmailAttributes setupAttributes = emailAttributeStore.getEmailAttributes();
            String username = String.valueOf(invoker.getAttributes().get(EmailAttributes.EMAIL));
            if (!Objects.isNull(setupAttributes) && !username.equalsIgnoreCase(setupAttributes.getEmail())) {
                emailAttributeStore.removeAccessToken(username);
                username = setupAttributes.getEmail();
            }
            String refreshToken = oAuth2AccessToken.getRefreshToken();
            if (!Strings.isNullOrBlank(refreshToken)) {
                logger.debug("user refresh token found successfully");
                emailAttributeStore.saveAccessToken(username, refreshToken);
                authClient.sessionConnect();
                emailAttributeStore.removeAttributes();
                logger.info("Access token received and saved successfully");
                return Response.ok("User authenticated successfully").build();
            }
            logger.error("No refresh token found.");
        }
        return Response.serverError().build();
    }

    @GET
    @Path("/reloadInvoker")
    public Response startInvoker() {
        logger.info("Dummy Request triggered to spawn invoker and to register listeners");
        return Response.ok().build();
    }
}
