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
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClientFactory;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.ksdk.context.ExecutionMode;
import app.krista.ksdk.context.RequestContext;
import com.github.scribejava.core.model.OAuth2AccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationResourceTest {

    @Mock private EmailAttributes emailAttributes;
    @Mock private Invoker invoker;
    @Mock private EmailAttributeStore emailAttributeStore;
    @Mock private RequestContext requestContext;
    @Mock private AuthClient authClient;
    @Mock private OAuth2AccessToken oAuth2AccessToken;

    private AuthenticationResource resource;

    @BeforeEach
    void setUp() {
        resource = new AuthenticationResource(emailAttributes, invoker, emailAttributeStore, requestContext);
    }

    @Test
    void startInvoker_returnsOk() {
        Response response = resource.startInvoker();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void callback_withValidRefreshToken_savesTokenAndReturnsOk() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);

            when(requestContext.getExecutionMode()).thenReturn(ExecutionMode.TEST);
            when(authClient.getRefreshToken("auth-code")).thenReturn(oAuth2AccessToken);
            when(oAuth2AccessToken.getRefreshToken()).thenReturn("valid-refresh-token");
            when(emailAttributeStore.getEmailAttributes()).thenReturn(null);
            when(invoker.getAttributes()).thenReturn(Map.of(EmailAttributes.EMAIL, "user@example.com"));

            Response response = resource.callback("auth-code", "state");

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(emailAttributeStore).saveAccessToken("user@example.com", "valid-refresh-token");
            verify(authClient).sessionConnect();
            verify(emailAttributeStore).removeAttributes();
        }
    }

    @Test
    void callback_withNullOAuthToken_returnsServerError() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);

            when(requestContext.getExecutionMode()).thenReturn(ExecutionMode.TEST);
            when(authClient.getRefreshToken("bad-code")).thenReturn(null);

            Response response = resource.callback("bad-code", "state");

            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            verifyNoInteractions(emailAttributeStore);
        }
    }

    @Test
    void callback_withBlankRefreshToken_returnsServerError() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);

            when(requestContext.getExecutionMode()).thenReturn(ExecutionMode.TEST);
            when(authClient.getRefreshToken("code")).thenReturn(oAuth2AccessToken);
            when(oAuth2AccessToken.getRefreshToken()).thenReturn("");

            Response response = resource.callback("code", "state");

            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            verify(emailAttributeStore, never()).saveAccessToken(any(), any());
        }
    }

    @Test
    void callback_withUpdatedSetupAttributes_usesNewEmail() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);

            EmailAttributes setupAttributes = mock(EmailAttributes.class);
            when(setupAttributes.getEmail()).thenReturn("new@example.com");

            when(requestContext.getExecutionMode()).thenReturn(ExecutionMode.TEST);
            when(authClient.getRefreshToken("code")).thenReturn(oAuth2AccessToken);
            when(oAuth2AccessToken.getRefreshToken()).thenReturn("refresh-token");
            when(emailAttributeStore.getEmailAttributes()).thenReturn(setupAttributes);
            when(invoker.getAttributes()).thenReturn(Map.of(EmailAttributes.EMAIL, "old@example.com"));

            Response response = resource.callback("code", "state");

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(emailAttributeStore).removeAccessToken("old@example.com");
            verify(emailAttributeStore).saveAccessToken("new@example.com", "refresh-token");
        }
    }
}
