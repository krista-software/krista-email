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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.SaslException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OAuth2SaslClientTest {

    private static final String OAUTH_TOKEN = "ya29.test-token";
    private static final String USER_EMAIL = "user@example.com";

    private CallbackHandler callbackHandler;
    private OAuth2SaslClient client;

    @BeforeEach
    void setUp() {
        callbackHandler = callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(USER_EMAIL);
                }
            }
        };
        client = new OAuth2SaslClient(OAUTH_TOKEN, callbackHandler);
    }

    @Test
    void getMechanismName_returnsXOAUTH2() {
        assertEquals("XOAUTH2", client.getMechanismName());
    }

    @Test
    void hasInitialResponse_returnsTrue() {
        assertTrue(client.hasInitialResponse());
    }

    @Test
    void isComplete_initiallyFalse() {
        assertFalse(client.isComplete());
    }

    @Test
    void evaluateChallenge_firstCall_returnsOAuth2Response() throws SaslException {
        byte[] response = client.evaluateChallenge(new byte[0]);
        assertNotNull(response);
        String responseStr = new String(response);
        assertTrue(responseStr.contains("user=" + USER_EMAIL));
        assertTrue(responseStr.contains("auth=Bearer " + OAUTH_TOKEN));
    }

    @Test
    void evaluateChallenge_firstCall_setsIsComplete() throws SaslException {
        assertFalse(client.isComplete());
        client.evaluateChallenge(new byte[0]);
        assertTrue(client.isComplete());
    }

    @Test
    void evaluateChallenge_secondCall_returnsEmptyBytes() throws SaslException {
        client.evaluateChallenge(new byte[0]); // first call
        byte[] response = client.evaluateChallenge(new byte[0]); // second call
        assertNotNull(response);
        assertEquals(0, response.length);
    }

    @Test
    void evaluateChallenge_withUnsupportedCallback_throwsSaslException() {
        CallbackHandler badHandler = callbacks -> {
            throw new UnsupportedCallbackException(callbacks[0]);
        };
        OAuth2SaslClient badClient = new OAuth2SaslClient(OAUTH_TOKEN, badHandler);
        assertThrows(SaslException.class, () -> badClient.evaluateChallenge(new byte[0]));
    }

    @Test
    void evaluateChallenge_withIOException_throwsSaslException() {
        CallbackHandler ioHandler = callbacks -> {
            throw new IOException("IO error");
        };
        OAuth2SaslClient ioClient = new OAuth2SaslClient(OAUTH_TOKEN, ioHandler);
        assertThrows(SaslException.class, () -> ioClient.evaluateChallenge(new byte[0]));
    }

    @Test
    void unwrap_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> client.unwrap(new byte[0], 0, 0));
    }

    @Test
    void wrap_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> client.wrap(new byte[0], 0, 0));
    }

    @Test
    void getNegotiatedProperty_whenNotComplete_throwsIllegalStateException() {
        assertFalse(client.isComplete());
        assertThrows(IllegalStateException.class,
                () -> client.getNegotiatedProperty("some.prop"));
    }

    @Test
    void getNegotiatedProperty_whenComplete_returnsNull() throws SaslException {
        client.evaluateChallenge(new byte[0]); // complete the auth
        assertNull(client.getNegotiatedProperty("some.prop"));
    }

    @Test
    void dispose_doesNotThrow() {
        assertDoesNotThrow(() -> client.dispose());
    }
}