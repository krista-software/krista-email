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

import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.imap.IMAPStoreImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.SearchTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Real-world scenario tests for MessageClientImpl move message validation.
 * Validates that enqueueMessageForMove() properly validates message ID and folder
 * before enqueuing, throwing IllegalArgumentException for invalid inputs.
 */
@ExtendWith(MockitoExtension.class)
class MessageClientImplTest {

    @Mock
    private AuthClient authClient;

    @Mock
    private IMAPStoreImpl imapStore;

    private MessageClientImpl messageClient;

    @BeforeEach
    void setUp() throws MessagingException {
        when(authClient.sessionConnect()).thenReturn(imapStore);
        lenient().when(imapStore.isConnected()).thenReturn(true);
        messageClient = new MessageClientImpl(authClient);
    }

    @AfterEach
    void tearDown() {
        MessageClientImpl.resetSchedulerExecutorService();
    }

    /**
     * Scenario: User tries to move a message with an ID that doesn't exist in any folder.
     * Expected: IllegalArgumentException is thrown with descriptive message.
     */
    @Test
    void enqueueMessageForMove_invalidMessageId_throwsIllegalArgument() throws MessagingException {
        // Arrange — store returns folders but search finds no messages
        Folder mockFolder = mock(Folder.class);
        when(mockFolder.isOpen()).thenReturn(false);
        doNothing().when(mockFolder).open(Folder.READ_WRITE);
        when(mockFolder.search(any(SearchTerm.class))).thenReturn(new Message[0]);
        doNothing().when(mockFolder).close(false);
        when(imapStore.listAllFolders()).thenReturn(new Folder[]{mockFolder});

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> messageClient.enqueueMessageForMove("Inbox", "nonexistent-msg-id"));
        assertTrue(ex.getMessage().contains("nonexistent-msg-id"));
    }

    /**
     * Scenario: User tries to move a message to a folder that doesn't exist.
     * Expected: IllegalArgumentException is thrown with folder name.
     */
    @Test
    void enqueueMessageForMove_invalidFolder_throwsIllegalArgument() throws MessagingException {
        // Arrange — message exists but target folder doesn't
        String messageId = "<valid-msg-123@gmail.com>";
        String invalidFolder = "NonExistentFolder";

        Folder mockFolder = mock(Folder.class);
        when(mockFolder.isOpen()).thenReturn(false);
        doNothing().when(mockFolder).open(Folder.READ_WRITE);
        Message mockMessage = mock(Message.class);
        when(mockFolder.search(any(SearchTerm.class))).thenReturn(new Message[]{mockMessage});
        doNothing().when(mockFolder).close(false);
        when(imapStore.listAllFolders()).thenReturn(new Folder[]{mockFolder});

        // Mock folder validation — target folder does NOT exist
        Folder mockTargetFolder = mock(Folder.class);
        when(imapStore.getFolder(invalidFolder)).thenReturn(mockTargetFolder);
        when(mockTargetFolder.exists()).thenReturn(false);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> messageClient.enqueueMessageForMove(invalidFolder, messageId));
        assertTrue(ex.getMessage().contains(invalidFolder));
    }

    /**
     * Scenario: Valid message ID exists in a folder — should enqueue successfully.
     * Expected: Method returns the message ID without throwing.
     */
    @Test
    void enqueueMessageForMove_validMessageAndFolder_enqueuesSuccessfully() throws MessagingException {
        // Arrange — mock a folder that contains the target message
        String messageId = "<valid-msg-123@gmail.com>";
        String targetFolder = "Archive";

        Folder mockFolder = mock(Folder.class);
        when(mockFolder.isOpen()).thenReturn(false);
        doNothing().when(mockFolder).open(Folder.READ_WRITE);

        Message mockMessage = mock(Message.class);
        when(mockFolder.search(any(SearchTerm.class))).thenReturn(new Message[]{mockMessage});
        doNothing().when(mockFolder).close(false);
        when(imapStore.listAllFolders()).thenReturn(new Folder[]{mockFolder});

        // Mock folder validation — target folder exists
        Folder mockTargetFolder = mock(Folder.class);
        when(imapStore.getFolder(targetFolder)).thenReturn(mockTargetFolder);
        when(mockTargetFolder.exists()).thenReturn(true);

        // Act
        String result = messageClient.enqueueMessageForMove(targetFolder, messageId);

        // Assert — returns the message ID, no exception thrown
        assertEquals(messageId, result);
    }

    /**
     * Scenario: Store is disconnected when move is attempted.
     * Expected: ensureStoreIsConnected() reconnects, then validation proceeds.
     */
    @Test
    void enqueueMessageForMove_storeDisconnected_reconnectsAndValidates() throws MessagingException {
        // Arrange — store is disconnected, then connects
        when(imapStore.isConnected()).thenReturn(false);
        doNothing().when(imapStore).connect();

        Folder mockFolder = mock(Folder.class);
        when(mockFolder.isOpen()).thenReturn(false);
        doNothing().when(mockFolder).open(Folder.READ_WRITE);
        when(mockFolder.search(any(SearchTerm.class))).thenReturn(new Message[0]);
        doNothing().when(mockFolder).close(false);
        when(imapStore.listAllFolders()).thenReturn(new Folder[]{mockFolder});

        // Act & Assert — message not found after reconnect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> messageClient.enqueueMessageForMove("Inbox", "missing-msg-id"));
        assertTrue(ex.getMessage().contains("missing-msg-id"));
        verify(imapStore).connect();
    }
}

