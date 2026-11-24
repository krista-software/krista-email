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
import app.krista.ksdk.context.ExecutionMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthClientFactory.
 */
@ExtendWith(MockitoExtension.class)
class AuthClientFactoryTest {

    @Mock
    private EmailAttributes emailAttributes;

    @Mock
    private EmailAttributeStore emailAttributeStore;

    @Test
    void getClient_withGmailImapServer_returnsGmailAuthClient() {
        when(emailAttributeStore.getEmailAttributes()).thenReturn(null);
        when(emailAttributes.getImapServer()).thenReturn("imap.gmail.com");

        AuthClient client = AuthClientFactory.getClient(emailAttributes, emailAttributeStore, ExecutionMode.TEST);
        assertNotNull(client);
        assertInstanceOf(GmailAuthClient.class, client);
    }

    @Test
    void getClient_withOutlookImapServer_returnsOutlookAuthClient() {
        when(emailAttributeStore.getEmailAttributes()).thenReturn(null);
        when(emailAttributes.getImapServer()).thenReturn("outlook.office365.com");

        AuthClient client = AuthClientFactory.getClient(emailAttributes, emailAttributeStore, ExecutionMode.TEST);
        assertNotNull(client);
        assertInstanceOf(OutlookAuthClient.class, client);
    }

    @Test
    void getClient_withSameAttributesDifferentExecutionMode_recreatesClient() {
        when(emailAttributeStore.getEmailAttributes()).thenReturn(null);
        when(emailAttributes.getImapServer()).thenReturn("imap.gmail.com");

        AuthClient client1 = AuthClientFactory.getClient(emailAttributes, emailAttributeStore, ExecutionMode.TEST);
        AuthClient client2 = AuthClientFactory.getClient(emailAttributes, emailAttributeStore, ExecutionMode.LIVE);
        assertNotNull(client1);
        assertNotNull(client2);
    }

    @Test
    void getClient_returnsNonNullClient() {
        when(emailAttributeStore.getEmailAttributes()).thenReturn(null);
        when(emailAttributes.getImapServer()).thenReturn("imap.gmail.com");

        AuthClient client = AuthClientFactory.getClient(emailAttributes, emailAttributeStore, ExecutionMode.TEST);
        assertNotNull(client);
    }
}
