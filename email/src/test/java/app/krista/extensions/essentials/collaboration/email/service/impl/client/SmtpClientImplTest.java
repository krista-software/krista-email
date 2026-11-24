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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.ServerErrorException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SmtpClientImpl to verify connection reuse, reconnect logic, and error handling.
 */
public class SmtpClientImplTest {

    private Session session;
    private Transport transport;
    private SmtpClientImpl smtpClient;

    @BeforeEach
    public void setup() throws Exception {
        session = mock(Session.class);
        transport = mock(Transport.class);
        when(session.getTransport("smtp")).thenReturn(transport);
        when(session.getProperty("mail.smtp.host")).thenReturn("smtp.gmail.com");
        when(transport.isConnected()).thenReturn(false);  // Default: not connected
        smtpClient = new SmtpClientImpl(session);
    }

    /**
     * Test that multiple send() calls reuse the same Transport connection.
     * Verifies that connect() is called only once, but sendMessage() is called multiple times.
     */
    @Test
    public void testConnectionReuse() throws Exception {
        // Create a flag to track connection state
        final boolean[] connected = {false};

        // Mock isConnected() to return the connection state
        when(transport.isConnected()).thenAnswer(invocation -> connected[0]);

        // Mock connect() to set the connection state to true
        doAnswer(invocation -> {
            connected[0] = true;
            return null;
        }).when(transport).connect();

        Message msg1 = mock(Message.class);
        Message msg2 = mock(Message.class);
        Message msg3 = mock(Message.class);

        Address[] recipients = new Address[]{new InternetAddress("test@example.com")};
        when(msg1.getAllRecipients()).thenReturn(recipients);
        when(msg2.getAllRecipients()).thenReturn(recipients);
        when(msg3.getAllRecipients()).thenReturn(recipients);

        // Send 3 messages
        smtpClient.send(msg1);
        smtpClient.send(msg2);
        smtpClient.send(msg3);

        // Verify transport.connect() called only once (connection reused)
        verify(transport, times(1)).connect();

        // Verify sendMessage called 3 times (one per email)
        verify(transport, times(3)).sendMessage(any(Message.class), any(Address[].class));
    }

    /**
     * Test that when send() fails, it reconnects and retries once.
     * Verifies the retry logic with automatic reconnection.
     */
    @Test
    public void testReconnectOnFailure() throws Exception {
        Transport transport2 = mock(Transport.class);
        when(session.getTransport("smtp")).thenReturn(transport, transport2);

        Message msg = mock(Message.class);
        Address[] recipients = new Address[]{new InternetAddress("test@example.com")};
        when(msg.getAllRecipients()).thenReturn(recipients);

        // First send attempt fails with MessagingException
        doThrow(new MessagingException("Connection lost"))
            .when(transport).sendMessage(any(Message.class), any(Address[].class));

        // Send the message - should succeed after retry
        smtpClient.send(msg);

        // Verify old transport was closed
        verify(transport).close();
        
        // Verify new transport was created and connected
        verify(transport2).connect();
        
        // Verify message was sent via new transport
        verify(transport2).sendMessage(msg, recipients);
    }

    /**
     * Test that send() throws IllegalArgumentException when no recipients are specified.
     */
    @Test
    public void testSendWithNoRecipients() throws Exception {
        Message msg = mock(Message.class);
        when(msg.getAllRecipients()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            smtpClient.send(msg);
        });
    }

    /**
     * Test that send() throws IllegalArgumentException when recipients array is empty.
     */
    @Test
    public void testSendWithEmptyRecipients() throws Exception {
        Message msg = mock(Message.class);
        when(msg.getAllRecipients()).thenReturn(new Address[0]);

        assertThrows(IllegalArgumentException.class, () -> {
            smtpClient.send(msg);
        });
    }

    /**
     * Test that close() properly closes the Transport connection.
     */
    @Test
    public void testClose() throws Exception {
        Message msg = mock(Message.class);
        Address[] recipients = new Address[]{new InternetAddress("test@example.com")};
        when(msg.getAllRecipients()).thenReturn(recipients);

        // Send a message to create the connection
        smtpClient.send(msg);
        
        // Close the client
        smtpClient.close();

        // Verify transport was closed
        verify(transport).close();
    }

    /**
     * Test that getSession() returns the correct Session object.
     */
    @Test
    public void testGetSession() {
        Session result = smtpClient.getSession();
        assertEquals(session, result);
    }

    /**
     * Test that transport reconnects if it becomes disconnected between sends.
     */
    @Test
    public void testReconnectIfDisconnected() throws Exception {
        Message msg1 = mock(Message.class);
        Message msg2 = mock(Message.class);
        Address[] recipients = new Address[]{new InternetAddress("test@example.com")};
        when(msg1.getAllRecipients()).thenReturn(recipients);
        when(msg2.getAllRecipients()).thenReturn(recipients);

        // First send - transport is connected
        when(transport.isConnected()).thenReturn(false, true, false, true);
        
        smtpClient.send(msg1);
        smtpClient.send(msg2);

        // Verify connect() was called twice (once for each send, since isConnected() returns false)
        verify(transport, times(2)).connect();
    }

    /**
     * Test that close() before any send (transport is null) does not throw.
     */
    @Test
    public void testCloseBeforeSendDoesNotThrow() {
        assertDoesNotThrow(() -> smtpClient.close());
    }

    /**
     * Test that when both the initial send AND the retry fail,
     * a ServerErrorException is thrown and both transports are closed.
     */
    @Test
    public void testBothSendAndRetryFail_throwsServerErrorException() throws Exception {
        Transport transport2 = mock(Transport.class);
        when(session.getTransport("smtp")).thenReturn(transport, transport2);

        Message msg = mock(Message.class);
        Address[] recipients = new Address[]{new InternetAddress("test@example.com")};
        when(msg.getAllRecipients()).thenReturn(recipients);

        doThrow(new MessagingException("Connection lost"))
                .when(transport).sendMessage(any(Message.class), any(Address[].class));
        doThrow(new MessagingException("Retry also failed"))
                .when(transport2).sendMessage(any(Message.class), any(Address[].class));

        assertThrows(ServerErrorException.class, () -> smtpClient.send(msg));

        verify(transport).close();
        verify(transport2).close();
    }

    /**
     * Test that calling close() multiple times is safe (idempotent).
     * transport.close() should only be invoked once.
     */
    @Test
    public void testCloseIsIdempotent() throws Exception {
        Message msg = mock(Message.class);
        Address[] recipients = new Address[]{new InternetAddress("test@example.com")};
        when(msg.getAllRecipients()).thenReturn(recipients);

        smtpClient.send(msg);
        smtpClient.close();
        smtpClient.close(); // second call should be a no-op

        verify(transport, times(1)).close();
    }

    /**
     * Test that after close(), send() creates a fresh connection and sends successfully.
     */
    @Test
    public void testSendAfterClose_reconnects() throws Exception {
        final boolean[] connected = {false};
        when(transport.isConnected()).thenAnswer(inv -> connected[0]);
        doAnswer(inv -> { connected[0] = true; return null; }).when(transport).connect();
        doAnswer(inv -> { connected[0] = false; return null; }).when(transport).close();
        when(session.getTransport("smtp")).thenReturn(transport);

        Message msg = mock(Message.class);
        Address[] recipients = new Address[]{new InternetAddress("test@example.com")};
        when(msg.getAllRecipients()).thenReturn(recipients);

        smtpClient.send(msg);
        smtpClient.close();
        smtpClient.send(msg);

        // connect() called twice — once before first send, once after close
        verify(transport, times(2)).connect();
        verify(transport, times(2)).sendMessage(any(Message.class), any(Address[].class));
    }
}

