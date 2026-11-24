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
import app.krista.extensions.essentials.collaboration.email.exception.MatchingFailureException;
import app.krista.extensions.essentials.collaboration.email.service.Attachment;
import app.krista.extensions.essentials.collaboration.email.service.Email;
import app.krista.extensions.essentials.collaboration.email.service.EmailAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CatalogTypes.
 */
@ExtendWith(MockitoExtension.class)
class CatalogTypesTest {

    @Mock
    private Email mockEmail;

    @Mock
    private Attachment mockAttachment;

    @Test
    void fromEmail_withNullEmail_throwsMatchingFailureException() {
        assertThrows(MatchingFailureException.class, () -> CatalogTypes.fromEmail(null));
    }

    @Test
    void fromEmail_withValidEmail_mapsAllFields() throws MatchingFailureException {
        EmailAddress sender = new EmailAddress("Sender", "sender@example.com");
        EmailAddress to1 = new EmailAddress("To1", "to1@example.com");
        EmailAddress cc1 = new EmailAddress(null, "cc@example.com");
        EmailAddress replyTo = new EmailAddress(null, "reply@example.com");

        when(mockEmail.getSenderEmailAddress()).thenReturn(sender);
        when(mockEmail.getToEmailAddresses()).thenReturn(List.of(to1));
        when(mockEmail.getContent()).thenReturn("Email body");
        when(mockEmail.getSubject()).thenReturn("Test Subject");
        when(mockEmail.getFileAttachments()).thenReturn(List.of());
        when(mockEmail.getItemAttachments()).thenReturn(List.of());
        when(mockEmail.getMessageId()).thenReturn("<msg-id@example.com>");
        when(mockEmail.getCcEmailAddresses()).thenReturn(List.of(cc1));
        when(mockEmail.getRead()).thenReturn(true);
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(List.of(replyTo));
        when(mockEmail.getSendDateAndTime()).thenReturn(1000000L);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(2000000L);

        MailDetails result = CatalogTypes.fromEmail(mockEmail);

        assertEquals("sender@example.com", result.from);
        assertEquals("to1@example.com", result.to);
        assertEquals("Email body", result.message);
        assertEquals("Test Subject", result.subject);
        assertNotNull(result.fileAttachment);
        assertTrue(result.fileAttachment.isEmpty());
        assertNotNull(result.itemAttachment);
        assertTrue(result.itemAttachment.isEmpty());
        assertEquals("<msg-id@example.com>", result.messageID);
        assertEquals("cc@example.com", result.cc);
        assertTrue(result.isRead);
        assertEquals("reply@example.com", result.replyTo);
        assertEquals(1000000L, result.sendDateAndTime);
        assertEquals(2000000L, result.receivedDateAndTime);
    }

    @Test
    void fromEmail_withEmptyReplyToList_setsReplyToEmptyString() throws MatchingFailureException {
        EmailAddress sender = new EmailAddress("Sender", "sender@example.com");

        when(mockEmail.getSenderEmailAddress()).thenReturn(sender);
        when(mockEmail.getToEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getContent()).thenReturn("");
        when(mockEmail.getSubject()).thenReturn("Sub");
        when(mockEmail.getFileAttachments()).thenReturn(List.of());
        when(mockEmail.getItemAttachments()).thenReturn(List.of());
        when(mockEmail.getMessageId()).thenReturn("id1");
        when(mockEmail.getCcEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getRead()).thenReturn(false);
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getSendDateAndTime()).thenReturn(0L);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(0L);

        MailDetails result = CatalogTypes.fromEmail(mockEmail);
        assertEquals("", result.replyTo);
    }

    @Test
    void fromEmail_withMultipleToAddresses_joinsWithSemicolon() throws MatchingFailureException {
        EmailAddress sender = new EmailAddress("Sender", "sender@example.com");
        EmailAddress to1 = new EmailAddress(null, "to1@example.com");
        EmailAddress to2 = new EmailAddress(null, "to2@example.com");

        when(mockEmail.getSenderEmailAddress()).thenReturn(sender);
        when(mockEmail.getToEmailAddresses()).thenReturn(List.of(to1, to2));
        when(mockEmail.getContent()).thenReturn("");
        when(mockEmail.getSubject()).thenReturn("Sub");
        when(mockEmail.getFileAttachments()).thenReturn(List.of());
        when(mockEmail.getItemAttachments()).thenReturn(List.of());
        when(mockEmail.getMessageId()).thenReturn("id");
        when(mockEmail.getCcEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getRead()).thenReturn(false);
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getSendDateAndTime()).thenReturn(0L);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(0L);

        MailDetails result = CatalogTypes.fromEmail(mockEmail);
        assertTrue(result.to.contains("to1@example.com"));
        assertTrue(result.to.contains("to2@example.com"));
    }

    @Test
    void fromEmail_withNonEmptyItemAttachments_setsItemAttachments() throws MatchingFailureException {
        EmailAddress sender = new EmailAddress("Sender", "sender@example.com");

        when(mockEmail.getSenderEmailAddress()).thenReturn(sender);
        when(mockEmail.getToEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getContent()).thenReturn("");
        when(mockEmail.getSubject()).thenReturn("Sub");
        when(mockEmail.getFileAttachments()).thenReturn(List.of());
        when(mockEmail.getItemAttachments()).thenReturn(List.of("item1", "item2"));
        when(mockEmail.getMessageId()).thenReturn("id");
        when(mockEmail.getCcEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getRead()).thenReturn(false);
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getSendDateAndTime()).thenReturn(0L);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(0L);

        MailDetails result = CatalogTypes.fromEmail(mockEmail);
        assertEquals(List.of("item1", "item2"), result.itemAttachment);
    }

    @Test
    void fromEmail_withMultipleCcAddresses_joinsWithSemicolon() throws MatchingFailureException {
        EmailAddress sender = new EmailAddress("Sender", "sender@example.com");
        EmailAddress cc1 = new EmailAddress(null, "cc1@example.com");
        EmailAddress cc2 = new EmailAddress(null, "cc2@example.com");

        when(mockEmail.getSenderEmailAddress()).thenReturn(sender);
        when(mockEmail.getToEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getContent()).thenReturn("");
        when(mockEmail.getSubject()).thenReturn("Sub");
        when(mockEmail.getFileAttachments()).thenReturn(List.of());
        when(mockEmail.getItemAttachments()).thenReturn(List.of());
        when(mockEmail.getMessageId()).thenReturn("id");
        when(mockEmail.getCcEmailAddresses()).thenReturn(List.of(cc1, cc2));
        when(mockEmail.getRead()).thenReturn(false);
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(List.of());
        when(mockEmail.getSendDateAndTime()).thenReturn(0L);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(0L);

        MailDetails result = CatalogTypes.fromEmail(mockEmail);
        assertTrue(result.cc.contains("cc1@example.com"));
        assertTrue(result.cc.contains("cc2@example.com"));
    }
}
