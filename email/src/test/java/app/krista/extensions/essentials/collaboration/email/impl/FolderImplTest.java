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

package app.krista.extensions.essentials.collaboration.email.impl;

import app.krista.extensions.essentials.collaboration.email.service.Folder;
import app.krista.extensions.essentials.collaboration.email.service.KristaMediaClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.NotSupportedException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FolderImpl — focuses on null-folder paths and mock-based scenarios.
 */
@ExtendWith(MockitoExtension.class)
class FolderImplTest {

    @Mock
    private javax.mail.Folder mockFolder;

    @Mock
    private KristaMediaClient mockKristaMediaClient;

    // ============== null folder paths ==============

    @Test
    void getFolderName_withNullFolder_returnsNull() {
        FolderImpl impl = new FolderImpl(null);
        assertNull(impl.getFolderName());
    }

    @Test
    void getFolderId_withNullFolder_returnsNull() {
        FolderImpl impl = new FolderImpl(null);
        assertNull(impl.getFolderId());
    }

    @Test
    void getParent_withNullFolder_returnsNull() {
        FolderImpl impl = new FolderImpl(null);
        assertNull(impl.getParent());
    }

    @Test
    void getFolderPath_withNullFolder_returnsEmptyList() {
        FolderImpl impl = new FolderImpl(null);
        List<String> path = impl.getFolderPath();
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    @Test
    void getChildFolderByName_withNullFolder_returnsNull() {
        FolderImpl impl = new FolderImpl(null);
        assertNull(impl.getChildFolderByName("child"));
    }

    @Test
    void getEmails_withNullFolder_returnsEmptyList() {
        FolderImpl impl = new FolderImpl(null);
        List<?> emails = impl.getEmails(1.0, 10.0, mockKristaMediaClient);
        assertNotNull(emails);
        assertTrue(emails.isEmpty());
    }

    @Test
    void getUnreadEmails_withNullFolder_returnsEmptyList() {
        FolderImpl impl = new FolderImpl(null);
        List<?> emails = impl.getUnreadEmails(1.0, 10.0, mockKristaMediaClient);
        assertNotNull(emails);
        assertTrue(emails.isEmpty());
    }

    // ============== UnsupportedOperation paths ==============

    @Test
    void getChildFolderNames_throwsNotSupportedException() {
        FolderImpl impl = new FolderImpl(mockFolder);
        assertThrows(NotSupportedException.class, impl::getChildFolderNames);
    }

    @Test
    void getChildFolderByNameList_throwsNotSupportedException() {
        FolderImpl impl = new FolderImpl(mockFolder);
        assertThrows(NotSupportedException.class, () -> impl.getChildFolderByName(List.of("a", "b")));
    }

    @Test
    void getChildFolderIds_throwsNotSupportedException() {
        FolderImpl impl = new FolderImpl(mockFolder);
        assertThrows(NotSupportedException.class, impl::getChildFolderIds);
    }

    @Test
    void moveInto_throwsNotSupportedException() {
        FolderImpl impl = new FolderImpl(mockFolder);
        Folder mockTargetFolder = mock(Folder.class);
        assertThrows(NotSupportedException.class, () -> impl.moveInto(mockTargetFolder));
    }

    // ============== real mock folder paths ==============

    @Test
    void getFolderName_withRealFolder_returnsFolderName() {
        when(mockFolder.getName()).thenReturn("INBOX");
        FolderImpl impl = new FolderImpl(mockFolder);
        assertEquals("INBOX", impl.getFolderName());
    }

    @Test
    void getJavaMailFolder_returnsFolder() {
        FolderImpl impl = new FolderImpl(mockFolder);
        assertSame(mockFolder, impl.getJavaMailFolder());
    }

    @Test
    void getMessageCount_returnsCount() throws MessagingException {
        when(mockFolder.getMessageCount()).thenReturn(42);
        FolderImpl impl = new FolderImpl(mockFolder);
        assertEquals(42, impl.getMessageCount());
    }

    @Test
    void getMessageCount_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockFolder.getMessageCount()).thenThrow(new MessagingException("error"));
        FolderImpl impl = new FolderImpl(mockFolder);
        assertThrows(IllegalStateException.class, impl::getMessageCount);
    }

    @Test
    void getUnreadMessageCount_returnsCount() throws MessagingException {
        when(mockFolder.getUnreadMessageCount()).thenReturn(5);
        FolderImpl impl = new FolderImpl(mockFolder);
        assertEquals(5, impl.getUnreadMessageCount());
    }

    @Test
    void getUnreadMessageCount_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockFolder.getUnreadMessageCount()).thenThrow(new MessagingException("error"));
        FolderImpl impl = new FolderImpl(mockFolder);
        assertThrows(IllegalStateException.class, impl::getUnreadMessageCount);
    }

    @Test
    void open_whenFolderAlreadyOpen_doesNotOpen() throws MessagingException {
        when(mockFolder.isOpen()).thenReturn(true);
        FolderImpl impl = new FolderImpl(mockFolder);
        impl.open();
        verify(mockFolder, never()).open(anyInt());
    }

    @Test
    void open_whenFolderClosed_opensFolder() throws MessagingException {
        when(mockFolder.isOpen()).thenReturn(false);
        FolderImpl impl = new FolderImpl(mockFolder);
        impl.open();
        verify(mockFolder).open(javax.mail.Folder.READ_ONLY);
    }

    @Test
    void open_messagingException_doesNotThrow() throws MessagingException {
        when(mockFolder.isOpen()).thenReturn(false);
        doThrow(new MessagingException("error")).when(mockFolder).open(anyInt());
        FolderImpl impl = new FolderImpl(mockFolder);
        assertDoesNotThrow(impl::open);
    }

    @Test
    void getChildFolder_delegatesToGetChildFolderByName() throws MessagingException {
        javax.mail.Folder childMockFolder = mock(javax.mail.Folder.class);
        when(mockFolder.getFolder("inbox")).thenReturn(childMockFolder);
        FolderImpl impl = new FolderImpl(mockFolder);
        Folder result = impl.getChildFolder("inbox");
        assertNotNull(result);
    }

    @Test
    void listFolders_firstCall_callsList() throws MessagingException {
        javax.mail.Folder[] folders = new javax.mail.Folder[]{mockFolder};
        when(mockFolder.list()).thenReturn(folders);
        FolderImpl impl = new FolderImpl(mockFolder);
        javax.mail.Folder[] result = impl.listFolders();
        assertNotNull(result);
        assertEquals(1, result.length);
        verify(mockFolder, times(1)).list();
    }

    @Test
    void listFolders_secondCall_usesCachedResult() throws MessagingException {
        javax.mail.Folder[] folders = new javax.mail.Folder[]{mockFolder};
        when(mockFolder.list()).thenReturn(folders);
        FolderImpl impl = new FolderImpl(mockFolder);
        impl.listFolders();
        impl.listFolders(); // second call
        verify(mockFolder, times(1)).list(); // list() called only once
    }

    @Test
    void getEmails_withNullPageNumber_usesDefault() throws MessagingException {
        when(mockFolder.getMessages()).thenReturn(new javax.mail.Message[0]);
        FolderImpl impl = new FolderImpl(mockFolder);
        List<?> result = impl.getEmails(null, null, mockKristaMediaClient);
        assertNotNull(result);
    }

    @Test
    void getEmails_withPageNumberLessThanOne_throwsIllegalArgumentException() {
        FolderImpl impl = new FolderImpl(mockFolder);
        assertThrows(IllegalArgumentException.class,
                () -> impl.getEmails(0.0, 10.0, mockKristaMediaClient));
    }

    @Test
    void getEmails_withPageBeyondData_returnsEmptyList() throws MessagingException {
        when(mockFolder.getMessages()).thenReturn(new javax.mail.Message[0]);
        FolderImpl impl = new FolderImpl(mockFolder);
        // page 5 with page size 10, but no messages → returns empty
        List<?> result = impl.getEmails(5.0, 10.0, mockKristaMediaClient);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}