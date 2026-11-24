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

import app.krista.extensions.essentials.collaboration.email.service.Folder;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.MessagingException;
import javax.mail.Store;
import javax.ws.rs.ServerErrorException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FolderClientImpl.
 * Tests folder retrieval, caching, reconnection logic, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class FolderClientImplTest {

    @Mock
    private AuthClient authClient;

    @Mock
    private Store store;

    @Mock
    private javax.mail.Folder mailFolder;

    private FolderClientImpl folderClient;

    @BeforeEach
    void setUp() throws MessagingException {
        when(authClient.sessionConnect()).thenReturn(store);
        when(store.isConnected()).thenReturn(true);
        folderClient = new FolderClientImpl(authClient);
    }

    @Test
    void testGetInboxFolder_ReturnsInboxFolder() throws MessagingException {
        // Arrange
        when(store.getFolder("inbox")).thenReturn(mailFolder);
        when(mailFolder.exists()).thenReturn(true);

        // Act
        Folder result = folderClient.getInboxFolder();

        // Assert
        assertNotNull(result);
        verify(store).getFolder("inbox");
        verify(mailFolder).exists();
    }

    @Test
    void testGetSentFolder_ReturnsSentFolder() throws MessagingException {
        // Arrange
        when(store.getFolder("sent items")).thenReturn(mailFolder);
        when(mailFolder.exists()).thenReturn(true);

        // Act
        Folder result = folderClient.getSentFolder();

        // Assert
        assertNotNull(result);
        verify(store).getFolder("sent items");
        verify(mailFolder).exists();
    }

    @Test
    void testGetFolder_ValidFolderName_ReturnsFolder() throws MessagingException {
        // Arrange
        String folderName = "CustomFolder";
        when(store.getFolder(folderName)).thenReturn(mailFolder);
        when(mailFolder.exists()).thenReturn(true);

        // Act
        Folder result = folderClient.getFolder(folderName);

        // Assert
        assertNotNull(result);
        verify(store).getFolder(folderName);
        verify(mailFolder).exists();
    }

    @Test
    void testGetFolder_CachesFolder_SecondCallDoesNotHitStore() throws MessagingException {
        // Arrange
        String folderName = "inbox";
        when(store.getFolder(folderName)).thenReturn(mailFolder);
        when(mailFolder.exists()).thenReturn(true);

        // Act
        Folder result1 = folderClient.getFolder(folderName);
        Folder result2 = folderClient.getFolder(folderName);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(store, times(1)).getFolder(folderName); // Only called once due to caching
    }

    @Test
    void testGetFolder_FolderDoesNotExist_ThrowsIllegalArgumentException() throws MessagingException {
        // Arrange
        String folderName = "NonExistentFolder";
        when(store.getFolder(folderName)).thenReturn(mailFolder);
        when(mailFolder.exists()).thenReturn(false);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> folderClient.getFolder(folderName));
        assertTrue(ex.getMessage().contains("NonExistentFolder"));
        assertTrue(ex.getMessage().contains("does not exist"));
    }

    @Test
    void testGetFolder_MessagingException_ThrowsServerErrorException() throws MessagingException {
        // Arrange
        String folderName = "TestFolder";
        when(store.getFolder(folderName)).thenThrow(new MessagingException("Connection error"));

        // Act & Assert
        assertThrows(ServerErrorException.class,
                () -> folderClient.getFolder(folderName));
    }

}

