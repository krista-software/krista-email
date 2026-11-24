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

package app.krista.extensions.essentials.collaboration.email.listener;

import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.ksdk.context.ExecutionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.event.ConnectionEvent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for ConnectionListenerImpl.
 * Tests the easy paths (non-IMAPSSL source) where reconnect logic is skipped.
 */
@ExtendWith(MockitoExtension.class)
class ConnectionListenerImplTest {

    @Mock
    private EmailAttributes emailAttributes;

    @Mock
    private EmailAttributeStore emailAttributeStore;

    private ConnectionListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new ConnectionListenerImpl(emailAttributes, emailAttributeStore, ExecutionMode.TEST);
    }

    @Test
    void opened_withNonStoreSource_doesNotThrow() {
        // Source is 'this' (a test class instance), not an IMAPSSL store - just logs
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.OPENED);
        assertDoesNotThrow(() -> listener.opened(event));
    }

    @Test
    void disconnected_withNonImapSSLSource_doesNotThrow() {
        // Source is 'this' - reconnectStore skips because source is not an IMAPSSLStore
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.DISCONNECTED);
        assertDoesNotThrow(() -> listener.disconnected(event));
    }

    @Test
    void closed_withNonImapSSLSource_doesNotThrow() {
        // Source is 'this' - reconnectStore skips because source is not an IMAPSSLStore
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CLOSED);
        assertDoesNotThrow(() -> listener.closed(event));
    }

    @Test
    void disconnected_withStringSource_doesNotReconnect() {
        // Source is a String - not an IMAPSSLStore - reconnect is skipped
        String nonStoreSource = "some-string-source";
        ConnectionEvent event = new ConnectionEvent(nonStoreSource, ConnectionEvent.DISCONNECTED);
        assertDoesNotThrow(() -> listener.disconnected(event));
    }

    @Test
    void closed_withStringSource_doesNotReconnect() {
        String nonStoreSource = "some-string-source";
        ConnectionEvent event = new ConnectionEvent(nonStoreSource, ConnectionEvent.CLOSED);
        assertDoesNotThrow(() -> listener.closed(event));
    }
}
