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

package app.krista.extensions.essentials.collaboration.email.service;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.ksdk.context.ExecutionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailRequestAuthenticator.
 */
@ExtendWith(MockitoExtension.class)
class EmailRequestAuthenticatorTest {

    @Mock
    private EmailAttributes emailAttributes;

    @Mock
    private EmailAttributeStore emailAttributeStore;

    private EmailRequestAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        authenticator = new EmailRequestAuthenticator(emailAttributes, emailAttributeStore, ExecutionMode.TEST);
    }

    @Test
    void getScheme_returnsNull() {
        assertNull(authenticator.getScheme());
    }

    @Test
    void getSupportedProtocols_returnsEmptySet() {
        assertTrue(authenticator.getSupportedProtocols().isEmpty());
    }

    @Test
    void getAuthenticatedAccountId_returnsNull() {
        assertNull(authenticator.getAuthenticatedAccountId(null));
    }

    @Test
    void setServiceAuthorization_returnsFalse() {
        assertFalse(authenticator.setServiceAuthorization("test-token"));
    }

    @Test
    void getAttributeFields_returnsEmptyMap() {
        assertTrue(authenticator.getAttributeFields().isEmpty());
    }

    @Test
    void getMustAuthenticateResponseWithRequest_returnsNull() {
        MustAuthenticateException exception = mock(MustAuthenticateException.class);
        assertNull(authenticator.getMustAuthenticateResponse(exception, null));
    }

    @Test
    void getMustAuthenticateResponse_returnsNull() {
        MustAuthenticateException exception = mock(MustAuthenticateException.class);
        assertNull(authenticator.getMustAuthenticateResponse(exception));
    }

    @Test
    void getMustAuthorizeResponseWithRequest_returnsNull() {
        MustAuthorizeException exception = mock(MustAuthorizeException.class);
        assertNull(authenticator.getMustAuthorizeResponse(exception, null));
    }

    @Test
    void getMustAuthorizeResponse_withEmptyDetails_returnsNull() {
        MustAuthorizeException exception = mock(MustAuthorizeException.class);
        when(exception.getDetails()).thenReturn(List.of());
        assertNull(authenticator.getMustAuthorizeResponse(exception));
    }
}
