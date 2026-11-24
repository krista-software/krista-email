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

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Additional unit tests for FolderImpl covering paths not tested in FolderImplTest.
 */
@ExtendWith(MockitoExtension.class)
class FolderImplAdditionalTest {

    @Mock
    private javax.mail.Folder mockFolder;

    @Mock
    private KristaMediaClient mockKristaMediaClient;

    // ============== getFolderId with real folder ==============

    @Test
    void getFolderId_withRealFolder_returnsUrlNameString() throws MessagingException {
        URLName urlName = new URLName("imaps", "example.com", 993, "INBOX", "user", null);
        when(mockFolder.getURLName()).thenReturn(urlName);
        FolderImpl impl = new FolderImpl(mockFolder);
        String result = impl.getFolderId();
        assertNotNull(result);
        assertTrue(result.contains("example.com"));
    }

    @Test
    void getFolderId_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockFolder.getURLName()).thenThrow(new MessagingException("url error"));
        FolderImpl impl = new FolderImpl(mockFolder);
        assertThrows(IllegalStateException.class, impl::getFolderId);
    }

    // ============== getParent ==============

    @Test
    void getParent_withRealFolder_returnsParentFolder() throws MessagingException {
        javax.mail.Folder parentMockFolder = mock(javax.mail.Folder.class);
        when(mockFolder.getParent()).thenReturn(parentMockFolder);
        FolderImpl impl = new FolderImpl(mockFolder);
        Folder result = impl.getParent();
        assertNotNull(result);
    }

    @Test
    void getParent_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockFolder.getParent()).thenThrow(new MessagingException("parent error"));
        FolderImpl impl = new FolderImpl(mockFolder);
        assertThrows(IllegalStateException.class, impl::getParent);
    }

    // ============== getFolderPath ==============

    @Test
    void getFolderPath_withRealFolder_returnsListWithUrlName() throws MessagingException {
        URLName urlName = new URLName("imaps", "example.com", 993, "Sent", "user", null);
        when(mockFolder.getURLName()).thenReturn(urlName);
        FolderImpl impl = new FolderImpl(mockFolder);
        List<String> path = impl.getFolderPath();
        assertNotNull(path);
        assertFalse(path.isEmpty());
    }

    @Test
    void getFolderPath_messagingException_throwsServerErrorException() throws MessagingException {
        when(mockFolder.getURLName()).thenThrow(new MessagingException("url error"));
        FolderImpl impl = new FolderImpl(mockFolder);
        assertThrows(javax.ws.rs.ServerErrorException.class, impl::getFolderPath);
    }

    // ============== getChildFolderByName (non-null folder) ==============

    @Test
    void getChildFolderByName_withExistingChild_returnsChildFolder() throws MessagingException {
        javax.mail.Folder childMock = mock(javax.mail.Folder.class);
        when(mockFolder.getFolder("Drafts")).thenReturn(childMock);
        FolderImpl impl = new FolderImpl(mockFolder);
        Folder result = impl.getChildFolderByName("Drafts");
        assertNotNull(result);
    }

    @Test
    void getChildFolderByName_withNullChild_returnsNull() throws MessagingException {
        when(mockFolder.getFolder("NonExistent")).thenReturn(null);
        FolderImpl impl = new FolderImpl(mockFolder);
        assertNull(impl.getChildFolderByName("NonExistent"));
    }

    @Test
    void getChildFolderByName_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockFolder.getFolder(anyString())).thenThrow(new MessagingException("folder error"));
        FolderImpl impl = new FolderImpl(mockFolder);
        assertThrows(IllegalStateException.class, () -> impl.getChildFolderByName("Error"));
    }

    // ============== getUnreadEmails ==============

    @Test
    void getUnreadEmails_withNoMessages_returnsEmptyList() throws MessagingException {
        when(mockFolder.search(any(SearchTerm.class))).thenReturn(new Message[0]);
        FolderImpl impl = new FolderImpl(mockFolder);
        List<?> result = impl.getUnreadEmails(1.0, 10.0, mockKristaMediaClient);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUnreadEmails_withNullPageNumbers_usesDefaults() throws MessagingException {
        when(mockFolder.search(any(SearchTerm.class))).thenReturn(new Message[0]);
        FolderImpl impl = new FolderImpl(mockFolder);
        List<?> result = impl.getUnreadEmails(null, null, mockKristaMediaClient);
        assertNotNull(result);
    }

    // ============== getEmails with messages ==============

    @Test
    void getEmails_withMessages_returnsEmailList() throws MessagingException {
        Message mockMsg = mock(MimeMessage.class);
        when(mockFolder.getMessages()).thenReturn(new Message[]{mockMsg});
        FolderImpl impl = new FolderImpl(mockFolder);
        List<?> result = impl.getEmails(1.0, 10.0, mockKristaMediaClient);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getEmails_withMessages_pageTwo_returnsEmptyList() throws MessagingException {
        // Only 1 message but requesting page 2 with pageSize 10 → start > size → empty
        Message mockMsg = mock(MimeMessage.class);
        when(mockFolder.getMessages()).thenReturn(new Message[]{mockMsg});
        FolderImpl impl = new FolderImpl(mockFolder);
        List<?> result = impl.getEmails(2.0, 10.0, mockKristaMediaClient);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmails_withMultipleMessages_pageSizeOne_returnsSingleEmail() throws MessagingException {
        Message msg1 = mock(MimeMessage.class);
        Message msg2 = mock(MimeMessage.class);
        Message msg3 = mock(MimeMessage.class);
        when(mockFolder.getMessages()).thenReturn(new Message[]{msg1, msg2, msg3});
        FolderImpl impl = new FolderImpl(mockFolder);
        List<?> result = impl.getEmails(1.0, 1.0, mockKristaMediaClient);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getEmails_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockFolder.getMessages()).thenThrow(new MessagingException("folder error"));
        FolderImpl impl = new FolderImpl(mockFolder);
        assertThrows(IllegalStateException.class, () -> impl.getEmails(1.0, 10.0, mockKristaMediaClient));
    }
}
