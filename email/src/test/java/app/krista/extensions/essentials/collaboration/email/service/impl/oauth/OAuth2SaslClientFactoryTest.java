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

package app.krista.extensions.essentials.collaboration.email.service.impl.oauth;

import org.junit.jupiter.api.Test;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OAuth2SaslClientFactoryTest {

    private final OAuth2SaslClientFactory factory = new OAuth2SaslClientFactory();

    @Test
    void getMechanismNames_returnsXOAUTH2() {
        String[] names = factory.getMechanismNames(null);
        assertEquals(1, names.length);
        assertEquals("XOAUTH2", names[0]);
    }

    @Test
    void getMechanismNames_withProps_returnsXOAUTH2() {
        Map<String, String> props = new HashMap<>();
        String[] names = factory.getMechanismNames(props);
        assertEquals(1, names.length);
        assertEquals("XOAUTH2", names[0]);
    }

    @Test
    void createSaslClient_withXOAUTH2Mechanism_returnsSaslClient() {
        Map<String, Object> props = new HashMap<>();
        props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, "test-token");
        CallbackHandler handler = mock(CallbackHandler.class);

        SaslClient client = factory.createSaslClient(
                new String[]{"XOAUTH2"}, null, "imap", "imap.gmail.com", props, handler);

        assertNotNull(client);
        assertEquals("XOAUTH2", client.getMechanismName());
    }

    @Test
    void createSaslClient_withNoMatchingMechanism_returnsNull() {
        Map<String, Object> props = new HashMap<>();
        CallbackHandler handler = mock(CallbackHandler.class);

        SaslClient client = factory.createSaslClient(
                new String[]{"PLAIN", "LOGIN"}, null, "imap", "imap.gmail.com", props, handler);

        assertNull(client);
    }

    @Test
    void createSaslClient_withCaseInsensitiveXOAUTH2_returnsSaslClient() {
        Map<String, Object> props = new HashMap<>();
        props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, "test-token");
        CallbackHandler handler = mock(CallbackHandler.class);

        SaslClient client = factory.createSaslClient(
                new String[]{"xoauth2"}, null, "imap", "imap.gmail.com", props, handler);

        assertNotNull(client);
    }

    @Test
    void createSaslClient_withMultipleMechanismsIncludingXOAUTH2_returnsSaslClient() {
        Map<String, Object> props = new HashMap<>();
        props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, "token123");
        CallbackHandler handler = mock(CallbackHandler.class);

        SaslClient client = factory.createSaslClient(
                new String[]{"PLAIN", "XOAUTH2", "LOGIN"}, null, "imap", "imap.gmail.com", props, handler);

        assertNotNull(client);
    }

    @Test
    void createSaslClient_hasInitialResponse() {
        Map<String, Object> props = new HashMap<>();
        props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, "my-token");
        CallbackHandler handler = mock(CallbackHandler.class);

        SaslClient client = factory.createSaslClient(
                new String[]{"XOAUTH2"}, null, "imap", "imap.gmail.com", props, handler);

        assertNotNull(client);
        assertTrue(client.hasInitialResponse());
    }

    @Test
    void oauthTokenProp_hasExpectedValue() {
        assertEquals("mail.imaps.sasl.mechanisms.oauth2.oauthToken",
                OAuth2SaslClientFactory.OAUTH_TOKEN_PROP);
    }
}
