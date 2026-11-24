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

package app.krista.extensions.essentials.collaboration.email.service.impl.authclients;

import app.krista.extensions.essentials.collaboration.email.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.Store;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthClient default methods.
 */
@ExtendWith(MockitoExtension.class)
class AuthClientTest {

    // Use a mock with CALLS_REAL_METHODS so default methods execute
    private final AuthClient authClient = mock(AuthClient.class, CALLS_REAL_METHODS);

    @Test
    void getProperties_containsMailStoreProtocol() {
        Properties props = authClient.getProperties();
        assertNotNull(props);
        assertEquals("imaps", props.getProperty("mail.store.protocol"));
    }

    @Test
    void getProperties_containsImapConnectionPoolSize() {
        Properties props = authClient.getProperties();
        assertEquals("5", props.getProperty("mail.imaps.connectionpoolsize"));
    }

    @Test
    void getProperties_containsSaslEnabledForImap() {
        Properties props = authClient.getProperties();
        assertEquals("true", props.get("mail.imaps.sasl.enable"));
    }

    @Test
    void getProperties_containsXoauth2SaslMechanism() {
        Properties props = authClient.getProperties();
        assertEquals(Constants.XOAUTH2, props.get("mail.imaps.sasl.mechanisms"));
    }

    @Test
    void getProperties_containsSmtpAuthEnabled() {
        Properties props = authClient.getProperties();
        assertEquals("true", props.get("mail.smtp.auth"));
    }

    @Test
    void getProperties_containsSmtpStarttls() {
        Properties props = authClient.getProperties();
        assertEquals("true", props.get("mail.smtp.starttls.enable"));
    }

    @Test
    void getProperties_containsStatusCacheTimeout() {
        Properties props = authClient.getProperties();
        assertEquals("300000", props.getProperty("mail.imaps.statuscachetimeout"));
    }

    @Test
    void getProperties_containsImapPort993() {
        Properties props = authClient.getProperties();
        assertEquals("993", props.get("mail.imap.port"));
    }

    @Test
    void getProperties_containsSmtpPort587() {
        Properties props = authClient.getProperties();
        assertEquals("587", props.get("mail.smtp.port"));
    }

    @Test
    void testConnection_callsSessionConnectAndReturnsTrue() {
        Store mockStore = mock(Store.class);
        doReturn(mockStore).when(authClient).sessionConnect();
        boolean result = authClient.testConnection();
        assertTrue(result);
        verify(authClient).sessionConnect();
    }

    @Test
    void defaultProtocol_isImaps() {
        assertEquals("imaps", AuthClient.DEFAULT_PROTOCOL);
    }
}
