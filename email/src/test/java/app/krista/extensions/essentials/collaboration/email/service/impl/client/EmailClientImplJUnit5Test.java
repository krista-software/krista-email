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

package app.krista.extensions.essentials.collaboration.email.service.impl.client;

import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.service.FolderClient;
import app.krista.extensions.essentials.collaboration.email.service.SmtpClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.EmailAttributesChangeDetector;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClientFactory;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.ksdk.context.ExecutionMode;
import app.krista.ksdk.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.Session;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailClientImplJUnit5Test {

    @Mock private EmailAttributes emailAttributes;
    @Mock private EmailAttributeStore emailAttributeStore;
    @Mock private RequestContext requestContext;
    @Mock private AuthClient authClient;
    @Mock private SmtpClient smtpClient;

    private EmailAttributesChangeDetector changeDetector;
    private EmailClientImpl emailClient;

    @BeforeEach
    void setUp() {
        changeDetector = new EmailAttributesChangeDetector();
        emailClient = new EmailClientImpl(changeDetector, emailAttributes, emailAttributeStore, requestContext);
        lenient().when(requestContext.getExecutionMode()).thenReturn(ExecutionMode.TEST);
    }

    @Test
    void getFolderClient_firstCall_createsNewInstance() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);
            when(authClient.sessionConnect()).thenReturn(null);

            FolderClient client = emailClient.getFolderClient();
            assertNotNull(client);
        }
    }

    @Test
    void getFolderClient_secondCall_returnsCachedInstance() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);

            FolderClient first = emailClient.getFolderClient();
            FolderClient second = emailClient.getFolderClient();

            assertSame(first, second);
            // AuthClientFactory called only once since no change detected
            factory.verify(() -> AuthClientFactory.getClient(any(), any(), any()), times(1));
        }
    }

    @Test
    void getFolderClient_afterChangeDetected_createsNewInstance() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);

            FolderClient first = emailClient.getFolderClient();
            changeDetector.setToUpdateFolderClient(true);
            FolderClient second = emailClient.getFolderClient();

            // A new instance is created after flag is set
            assertNotNull(second);
            factory.verify(() -> AuthClientFactory.getClient(any(), any(), any()), times(2));
        }
    }

    @Test
    void getImapClient_firstCall_createsNewInstance() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);

            assertNotNull(emailClient.getImapClient());
        }
    }

    @Test
    void getSmtpClient_firstCall_createsNewSmtpClient() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);
            when(authClient.getAccessToken()).thenReturn("token-abc");
            when(authClient.getSmtpSession()).thenReturn(Session.getInstance(new Properties()));

            SmtpClient client = emailClient.getSmtpClient();
            assertNotNull(client);
        }
    }

    @Test
    void getSmtpClient_tokenUnchanged_returnsSameSession() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);
            when(authClient.getAccessToken()).thenReturn("same-token");
            when(authClient.getSmtpSession()).thenReturn(Session.getInstance(new Properties()));

            SmtpClient first = emailClient.getSmtpClient();
            SmtpClient second = emailClient.getSmtpClient();

            // Token hasn't changed — same SmtpClient instance reused
            assertSame(first, second);
            verify(authClient, times(2)).getAccessToken();
            verify(authClient, times(1)).getSmtpSession();
        }
    }

    @Test
    void getSmtpClient_tokenChanged_recreatesSmtpClient() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);
            when(authClient.getSmtpSession()).thenReturn(Session.getInstance(new Properties()));
            when(authClient.getAccessToken())
                    .thenReturn("old-token")
                    .thenReturn("new-token");

            SmtpClient first = emailClient.getSmtpClient();
            SmtpClient second = emailClient.getSmtpClient();

            assertNotSame(first, second);
        }
    }

    @Test
    void getEmailStore_returnsEmailAttributeStore() {
        assertSame(emailAttributeStore, emailClient.getEmailStore());
    }

    @Test
    void me_returnsUserFromAuthClient() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);
            when(authClient.getUser()).thenReturn("user@example.com");

            assertEquals("user@example.com", emailClient.me());
        }
    }

    @Test
    void getMaxAttachmentSize_returnsValueFromAuthClient() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);
            when(authClient.getMaxAttachmentSize()).thenReturn(25_000_000L);

            assertEquals(25_000_000L, emailClient.getMaxAttachmentSize());
        }
    }

    @Test
    void getSession_returnsSessionFromAuthClient() {
        try (MockedStatic<AuthClientFactory> factory = mockStatic(AuthClientFactory.class)) {
            factory.when(() -> AuthClientFactory.getClient(any(), any(), any())).thenReturn(authClient);
            Session session = Session.getInstance(new Properties());
            when(authClient.getImapSession()).thenReturn(session);

            assertSame(session, emailClient.getSession());
        }
    }
}
