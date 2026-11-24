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

import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.ksdk.context.ExecutionMode;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.MessagingException;
import javax.mail.Store;
import javax.ws.rs.ServerErrorException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OutlookAuthClient — covers both simple getters and
 * OAuth token retrieval scenarios using a spy to isolate getAuthService().
 */
@ExtendWith(MockitoExtension.class)
class OutlookAuthClientTest {

    @Mock
    private EmailAttributes emailAttributes;

    @Mock
    private EmailAttributeStore emailAttributeStore;

    @Mock
    private OAuth20Service oAuth20Service;

    private OutlookAuthClient outlookAuthClient;

    @BeforeEach
    void setUp() {
        lenient().when(emailAttributeStore.getEmailAttributes()).thenReturn(null);
        outlookAuthClient = new OutlookAuthClient(emailAttributes, emailAttributeStore, ExecutionMode.TEST);
    }

    // ---- Simple getter tests ----

    @Test
    void getUser_returnsEmailFromAttributes() {
        when(emailAttributes.getEmail()).thenReturn("user@outlook.com");
        assertEquals("user@outlook.com", outlookAuthClient.getUser());
    }

    @Test
    void getMaxAttachmentSize_returnsOutlookLimit() {
        assertEquals(Constants.MAX_ATTACHMENT_SIZE_SUPPORTED_BY_OUTLOOK_IN_BYTES,
                outlookAuthClient.getMaxAttachmentSize());
    }

    @Test
    void getRefreshToken_returnsNull() {
        // OutlookAuthClient uses client credentials flow — no refresh token via code exchange
        assertNull(outlookAuthClient.getRefreshToken("some-code"));
    }

    @Test
    void constructor_storesParameters() {
        assertNotNull(outlookAuthClient);
    }

    // ---- getAccessToken() tests via spy ----

    /**
     * Scenario: Outlook uses client-credentials grant — token retrieved successfully.
     */
    @Test
    void getAccessToken_success_returnsToken() throws Exception {
        OutlookAuthClient spyClient = spy(new OutlookAuthClient(emailAttributes, emailAttributeStore, ExecutionMode.TEST));
        doReturn(oAuth20Service).when(spyClient).getAuthService();

        OAuth2AccessToken oAuth2AccessToken = mock(OAuth2AccessToken.class);
        when(oAuth2AccessToken.getAccessToken()).thenReturn("outlook-access-token");
        when(oAuth20Service.getAccessTokenClientCredentialsGrant()).thenReturn(oAuth2AccessToken);

        String result = spyClient.getAccessToken();

        assertEquals("outlook-access-token", result);
        verify(oAuth20Service, times(1)).getAccessTokenClientCredentialsGrant();
    }

    /**
     * Scenario: Network failure during token fetch — IOException is thrown.
     * Expected: ServerErrorException is thrown.
     */
    @Test
    void getAccessToken_ioException_throwsServerErrorException() throws Exception {
        OutlookAuthClient spyClient = spy(new OutlookAuthClient(emailAttributes, emailAttributeStore, ExecutionMode.TEST));
        doReturn(oAuth20Service).when(spyClient).getAuthService();
        when(oAuth20Service.getAccessTokenClientCredentialsGrant())
                .thenThrow(new IOException("Connection reset"));

        assertThrows(ServerErrorException.class, spyClient::getAccessToken);
    }

    /**
     * Scenario: OAuth service execution fails — ExecutionException is thrown.
     * Expected: ServerErrorException is thrown.
     */
    @Test
    void getAccessToken_executionException_throwsServerErrorException() throws Exception {
        OutlookAuthClient spyClient = spy(new OutlookAuthClient(emailAttributes, emailAttributeStore, ExecutionMode.TEST));
        doReturn(oAuth20Service).when(spyClient).getAuthService();
        when(oAuth20Service.getAccessTokenClientCredentialsGrant())
                .thenThrow(new ExecutionException("OAuth error", new RuntimeException()));

        assertThrows(ServerErrorException.class, spyClient::getAccessToken);
    }

    // ---- reconnect() tests ----

    /**
     * Scenario: Store reconnect succeeds with a fresh token.
     */
    @Test
    void reconnect_success_callsStoreConnect() throws Exception {
        OutlookAuthClient spyClient = spy(new OutlookAuthClient(emailAttributes, emailAttributeStore, ExecutionMode.TEST));
        doReturn("fresh-token").when(spyClient).getAccessToken();
        when(emailAttributes.getImapServer()).thenReturn("outlook.office365.com");
        when(emailAttributes.getEmail()).thenReturn("user@outlook.com");

        Store store = mock(Store.class);
        spyClient.reconnect(store);

        verify(store).connect("outlook.office365.com", "user@outlook.com", "fresh-token");
    }

    /**
     * Scenario: Store.connect() throws MessagingException during reconnect.
     * Expected: IllegalStateException is thrown.
     */
    @Test
    void reconnect_messagingException_throwsIllegalState() throws Exception {
        OutlookAuthClient spyClient = spy(new OutlookAuthClient(emailAttributes, emailAttributeStore, ExecutionMode.TEST));
        doReturn("some-token").when(spyClient).getAccessToken();
        when(emailAttributes.getImapServer()).thenReturn("outlook.office365.com");
        when(emailAttributes.getEmail()).thenReturn("user@outlook.com");

        Store store = mock(Store.class);
        doThrow(new MessagingException("Connection refused")).when(store)
                .connect(anyString(), anyString(), anyString());

        assertThrows(IllegalStateException.class, () -> spyClient.reconnect(store));
    }
}
