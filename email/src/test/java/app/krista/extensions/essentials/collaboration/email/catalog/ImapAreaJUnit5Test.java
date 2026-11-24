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

package app.krista.extensions.essentials.collaboration.email.catalog;

import app.krista.extensions.essentials.collaboration.email.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.email.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 unit tests for ImapArea using mocks.
 */
@ExtendWith(MockitoExtension.class)
class ImapAreaJUnit5Test {

    @Mock
    private EmailClient emailClient;

    @Mock
    private KristaMediaClient kristaMediaClient;

    @Mock
    private FolderClient folderClient;

    @Mock
    private Folder folder;

    @Mock
    private Email mockEmail;

    private ImapArea imapArea;

    @BeforeEach
    void setUp() {
        imapArea = new ImapArea(emailClient, kristaMediaClient);
    }

    @Test
    void retrieveUnreadEmailsFromFolder_withNullFolder_returnsEmptyList() {
        when(emailClient.getFolderClient()).thenReturn(folderClient);
        when(folderClient.getFolder("INBOX")).thenReturn(null);

        List<MailDetails> result = imapArea.retrieveUnreadEmailsFromFolder("INBOX", null, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void retrieveUnreadEmailsFromFolder_withZeroMessageCount_returnsEmptyList() {
        when(emailClient.getFolderClient()).thenReturn(folderClient);
        when(folderClient.getFolder("Sent")).thenReturn(folder);
        when(folder.getMessageCount()).thenReturn(0);

        List<MailDetails> result = imapArea.retrieveUnreadEmailsFromFolder("Sent", null, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void retrieveUnreadEmailsFromFolder_withNoUnreadEmails_returnsEmptyList() {
        when(emailClient.getFolderClient()).thenReturn(folderClient);
        when(folderClient.getFolder("INBOX")).thenReturn(folder);
        when(folder.getMessageCount()).thenReturn(5);
        when(folder.getUnreadEmails(any(), any(), any())).thenReturn(List.of());

        List<MailDetails> result = imapArea.retrieveUnreadEmailsFromFolder("INBOX", 1.0, 10.0);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void retrieveUnreadEmailsFromFolder_withNullEmailsList_returnsEmptyList() {
        when(emailClient.getFolderClient()).thenReturn(folderClient);
        when(folderClient.getFolder("INBOX")).thenReturn(folder);
        when(folder.getMessageCount()).thenReturn(3);
        when(folder.getUnreadEmails(any(), any(), any())).thenReturn(null);

        List<MailDetails> result = imapArea.retrieveUnreadEmailsFromFolder("INBOX", 1.0, 10.0);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void retrieveUnreadEmailsFromFolder_withEmails_returnsMappedMailDetails() {
        EmailAddress sender = new EmailAddress("Sender", "sender@example.com");
        EmailAddress to1 = new EmailAddress(null, "to@example.com");

        when(mockEmail.getSenderEmailAddress()).thenReturn(sender);
        when(mockEmail.getToEmailAddresses()).thenReturn(List.of(to1));
        when(mockEmail.getContent()).thenReturn("Test body");
        when(mockEmail.getSubject()).thenReturn("Test subject");
        when(mockEmail.getFileAttachments()).thenReturn(List.of());
        when(mockEmail.getItemAttachments()).thenReturn(List.of());
        when(mockEmail.getMessageId()).thenReturn("<id@example.com>");
        when(mockEmail.getCcEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getRead()).thenReturn(false);
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getSendDateAndTime()).thenReturn(1000000L);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(2000000L);

        when(emailClient.getFolderClient()).thenReturn(folderClient);
        when(folderClient.getFolder("INBOX")).thenReturn(folder);
        when(folder.getMessageCount()).thenReturn(1);
        when(folder.getUnreadEmails(any(), any(), any())).thenReturn(List.of(mockEmail));

        List<MailDetails> result = imapArea.retrieveUnreadEmailsFromFolder("INBOX", 1.0, 10.0);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("sender@example.com", result.get(0).from);
        assertEquals("Test subject", result.get(0).subject);
    }

    @Test
    void retrieveUnreadEmailsFromFolder_callsOpenOnFolder_whenEmailsExist() {
        when(emailClient.getFolderClient()).thenReturn(folderClient);
        when(folderClient.getFolder("INBOX")).thenReturn(folder);
        when(folder.getMessageCount()).thenReturn(3);
        when(folder.getUnreadEmails(any(), any(), any())).thenReturn(List.of());

        imapArea.retrieveUnreadEmailsFromFolder("INBOX", 0.0, 50.0);

        verify(folder, times(1)).open();
    }

    @Test
    void retrieveUnreadEmailsFromFolder_withNullEmailInList_throwsNoSuchElementException() {
        // CatalogTypes.fromEmail(null) throws MatchingFailureException, caught and rethrown as NoSuchElementException
        when(emailClient.getFolderClient()).thenReturn(folderClient);
        when(folderClient.getFolder("INBOX")).thenReturn(folder);
        when(folder.getMessageCount()).thenReturn(1);
        when(folder.getUnreadEmails(any(), any(), any())).thenReturn(Arrays.asList((Email) null));

        assertThrows(NoSuchElementException.class,
                () -> imapArea.retrieveUnreadEmailsFromFolder("INBOX", 0.0, 50.0));
    }

    @Test
    void retrieveUnreadEmailsFromFolder_withMultipleEmails_returnsMappedList() {
        EmailAddress sender1 = new EmailAddress("Alice", "alice@example.com");
        EmailAddress sender2 = new EmailAddress("Bob", "bob@example.com");

        Email secondEmail = mock(Email.class);

        when(mockEmail.getSenderEmailAddress()).thenReturn(sender1);
        when(mockEmail.getToEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getContent()).thenReturn("Body 1");
        when(mockEmail.getSubject()).thenReturn("Subject 1");
        when(mockEmail.getFileAttachments()).thenReturn(List.of());
        when(mockEmail.getItemAttachments()).thenReturn(List.of());
        when(mockEmail.getMessageId()).thenReturn("<msg1@example.com>");
        when(mockEmail.getCcEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getRead()).thenReturn(true);
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getSendDateAndTime()).thenReturn(1000L);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(2000L);

        when(secondEmail.getSenderEmailAddress()).thenReturn(sender2);
        when(secondEmail.getToEmailAddresses()).thenReturn(List.of());
        when(secondEmail.getContent()).thenReturn("Body 2");
        when(secondEmail.getSubject()).thenReturn("Subject 2");
        when(secondEmail.getFileAttachments()).thenReturn(List.of());
        when(secondEmail.getItemAttachments()).thenReturn(List.of());
        when(secondEmail.getMessageId()).thenReturn("<msg2@example.com>");
        when(secondEmail.getCcEmailAddresses()).thenReturn(List.of());
        when(secondEmail.getRead()).thenReturn(false);
        when(secondEmail.getReplyToEmailAddresses()).thenReturn(List.of());
        when(secondEmail.getSendDateAndTime()).thenReturn(3000L);
        when(secondEmail.getReceivedDateAndTime()).thenReturn(4000L);

        when(emailClient.getFolderClient()).thenReturn(folderClient);
        when(folderClient.getFolder("INBOX")).thenReturn(folder);
        when(folder.getMessageCount()).thenReturn(2);
        when(folder.getUnreadEmails(any(), any(), any())).thenReturn(List.of(mockEmail, secondEmail));

        List<MailDetails> result = imapArea.retrieveUnreadEmailsFromFolder("INBOX", 0.0, 10.0);

        assertEquals(2, result.size());
        assertEquals("alice@example.com", result.get(0).from);
        assertEquals("bob@example.com", result.get(1).from);
    }
}
