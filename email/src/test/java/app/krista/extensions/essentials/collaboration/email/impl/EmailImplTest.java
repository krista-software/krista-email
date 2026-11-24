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

import app.krista.extensions.essentials.collaboration.email.service.EmailAddress;
import app.krista.extensions.essentials.collaboration.email.service.KristaMediaClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.ServerErrorException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailImpl covering all getter methods and error handling.
 */
@ExtendWith(MockitoExtension.class)
class EmailImplTest {

    @Mock
    private MimeMessage mockMessage;

    @Mock
    private KristaMediaClient mockKristaMediaClient;

    @Mock
    private javax.mail.Folder mockFolder;

    private EmailImpl emailImpl;

    @BeforeEach
    void setUp() {
        emailImpl = new EmailImpl(mockMessage, mockKristaMediaClient);
    }

    // ============== getMessageId ==============

    @Test
    void getMessageId_success_returnsEncodedId() throws MessagingException {
        when(mockMessage.getMessageID()).thenReturn("<abc@example.com>");
        String id = emailImpl.getMessageId();
        assertEquals("&lt;abc@example.com&gt;", id);
    }

    @Test
    void getMessageId_withoutAngleBrackets_returnsUnchanged() throws MessagingException {
        when(mockMessage.getMessageID()).thenReturn("plain-id");
        assertEquals("plain-id", emailImpl.getMessageId());
    }

    @Test
    void getMessageId_messagingException_throwsServerErrorException() throws MessagingException {
        when(mockMessage.getMessageID()).thenThrow(new MessagingException("IMAP error"));
        assertThrows(ServerErrorException.class, () -> emailImpl.getMessageId());
    }

    // ============== getSubject ==============

    @Test
    void getSubject_success_returnsSubject() throws MessagingException {
        when(mockMessage.getSubject()).thenReturn("Test Subject");
        assertEquals("Test Subject", emailImpl.getSubject());
    }

    @Test
    void getSubject_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getSubject()).thenThrow(new MessagingException("error"));
        assertThrows(IllegalStateException.class, () -> emailImpl.getSubject());
    }

    // ============== getSenderEmailAddress ==============

    @Test
    void getSenderEmailAddress_success_returnsAddress() throws MessagingException {
        InternetAddress addr = new InternetAddress("sender@example.com");
        when(mockMessage.getFrom()).thenReturn(new Address[]{addr});
        EmailAddress result = emailImpl.getSenderEmailAddress();
        assertNotNull(result);
        assertEquals("sender@example.com", result.getAddress());
    }

    @Test
    void getSenderEmailAddress_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getFrom()).thenThrow(new MessagingException("error"));
        assertThrows(IllegalStateException.class, () -> emailImpl.getSenderEmailAddress());
    }

    // ============== getToEmailAddresses ==============

    @Test
    void getToEmailAddresses_nullRecipients_returnsEmptyList() throws MessagingException {
        when(mockMessage.getRecipients(Message.RecipientType.TO)).thenReturn(null);
        List<EmailAddress> result = emailImpl.getToEmailAddresses();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getToEmailAddresses_emptyRecipients_returnsEmptyList() throws MessagingException {
        when(mockMessage.getRecipients(Message.RecipientType.TO)).thenReturn(new Address[0]);
        List<EmailAddress> result = emailImpl.getToEmailAddresses();
        assertTrue(result.isEmpty());
    }

    @Test
    void getToEmailAddresses_withRecipients_returnsList() throws MessagingException {
        InternetAddress addr1 = new InternetAddress("to1@example.com", false);
        InternetAddress addr2 = new InternetAddress("to2@example.com", false);
        when(mockMessage.getRecipients(Message.RecipientType.TO))
                .thenReturn(new Address[]{addr1, addr2});
        List<EmailAddress> result = emailImpl.getToEmailAddresses();
        assertEquals(2, result.size());
        assertEquals("to1@example.com", result.get(0).getAddress());
        assertEquals("to2@example.com", result.get(1).getAddress());
    }

    @Test
    void getToEmailAddresses_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getRecipients(Message.RecipientType.TO)).thenThrow(new MessagingException("error"));
        assertThrows(IllegalStateException.class, () -> emailImpl.getToEmailAddresses());
    }

    // ============== getReplyToEmailAddresses ==============

    @Test
    void getReplyToEmailAddresses_nullReplyTo_returnsEmptyList() throws MessagingException {
        when(mockMessage.getReplyTo()).thenReturn(null);
        List<EmailAddress> result = emailImpl.getReplyToEmailAddresses();
        assertTrue(result.isEmpty());
    }

    @Test
    void getReplyToEmailAddresses_emptyReplyTo_returnsEmptyList() throws MessagingException {
        when(mockMessage.getReplyTo()).thenReturn(new Address[0]);
        List<EmailAddress> result = emailImpl.getReplyToEmailAddresses();
        assertTrue(result.isEmpty());
    }

    @Test
    void getReplyToEmailAddresses_withAddresses_returnsList() throws MessagingException {
        InternetAddress addr = new InternetAddress("replyto@example.com", false);
        when(mockMessage.getReplyTo()).thenReturn(new Address[]{addr});
        List<EmailAddress> result = emailImpl.getReplyToEmailAddresses();
        assertEquals(1, result.size());
        assertEquals("replyto@example.com", result.get(0).getAddress());
    }

    @Test
    void getReplyToEmailAddresses_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getReplyTo()).thenThrow(new MessagingException("error"));
        assertThrows(IllegalStateException.class, () -> emailImpl.getReplyToEmailAddresses());
    }

    // ============== getCcEmailAddresses ==============

    @Test
    void getCcEmailAddresses_nullCc_returnsEmptyList() throws MessagingException {
        when(mockMessage.getRecipients(Message.RecipientType.CC)).thenReturn(null);
        List<EmailAddress> result = emailImpl.getCcEmailAddresses();
        assertTrue(result.isEmpty());
    }

    @Test
    void getCcEmailAddresses_withRecipients_returnsList() throws MessagingException {
        InternetAddress addr = new InternetAddress("cc@example.com", false);
        when(mockMessage.getRecipients(Message.RecipientType.CC)).thenReturn(new Address[]{addr});
        List<EmailAddress> result = emailImpl.getCcEmailAddresses();
        assertEquals(1, result.size());
        assertEquals("cc@example.com", result.get(0).getAddress());
    }

    @Test
    void getCcEmailAddresses_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getRecipients(Message.RecipientType.CC)).thenThrow(new MessagingException("error"));
        assertThrows(IllegalStateException.class, () -> emailImpl.getCcEmailAddresses());
    }

    // ============== getBccEmailAddresses ==============

    @Test
    void getBccEmailAddresses_nullBcc_returnsEmptyList() throws MessagingException {
        when(mockMessage.getRecipients(Message.RecipientType.BCC)).thenReturn(null);
        List<EmailAddress> result = emailImpl.getBccEmailAddresses();
        assertTrue(result.isEmpty());
    }

    @Test
    void getBccEmailAddresses_withRecipients_returnsList() throws MessagingException {
        InternetAddress addr = new InternetAddress("bcc@example.com", false);
        when(mockMessage.getRecipients(Message.RecipientType.BCC)).thenReturn(new Address[]{addr});
        List<EmailAddress> result = emailImpl.getBccEmailAddresses();
        assertEquals(1, result.size());
    }

    @Test
    void getBccEmailAddresses_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getRecipients(Message.RecipientType.BCC)).thenThrow(new MessagingException("error"));
        assertThrows(IllegalStateException.class, () -> emailImpl.getBccEmailAddresses());
    }

    // ============== getRead ==============

    @Test
    void getRead_answeredFlag_returnsTrue() throws MessagingException {
        when(mockMessage.isSet(Flags.Flag.ANSWERED)).thenReturn(true);
        assertTrue(emailImpl.getRead());
    }

    @Test
    void getRead_seenFlag_returnsTrue() throws MessagingException {
        when(mockMessage.isSet(Flags.Flag.ANSWERED)).thenReturn(false);
        when(mockMessage.isSet(Flags.Flag.SEEN)).thenReturn(true);
        assertTrue(emailImpl.getRead());
    }

    @Test
    void getRead_noFlags_returnsFalse() throws MessagingException {
        when(mockMessage.isSet(Flags.Flag.ANSWERED)).thenReturn(false);
        when(mockMessage.isSet(Flags.Flag.SEEN)).thenReturn(false);
        assertFalse(emailImpl.getRead());
    }

    @Test
    void getRead_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.isSet(Flags.Flag.ANSWERED)).thenThrow(new MessagingException("error"));
        assertThrows(IllegalStateException.class, () -> emailImpl.getRead());
    }

    // ============== getSendDateAndTime ==============

    @Test
    void getSendDateAndTime_success_returnsTimestamp() throws MessagingException {
        Date date = new Date(1700000000000L);
        when(mockMessage.getSentDate()).thenReturn(date);
        assertEquals(1700000000000L, emailImpl.getSendDateAndTime());
    }

    @Test
    void getSendDateAndTime_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getSentDate()).thenThrow(new MessagingException("error"));
        assertThrows(IllegalStateException.class, () -> emailImpl.getSendDateAndTime());
    }

    // ============== getReceivedDateAndTime ==============

    @Test
    void getReceivedDateAndTime_success_returnsTimestamp() throws MessagingException {
        Date date = new Date(1700000000000L);
        when(mockMessage.getReceivedDate()).thenReturn(date);
        assertEquals(1700000000000L, emailImpl.getReceivedDateAndTime());
    }

    @Test
    void getReceivedDateAndTime_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getReceivedDate()).thenThrow(new MessagingException("error"));
        assertThrows(IllegalStateException.class, () -> emailImpl.getReceivedDateAndTime());
    }

    // ============== getContentType ==============

    @Test
    void getContentType_success_returnsContentType() throws MessagingException {
        when(mockMessage.getContentType()).thenReturn("text/plain");
        assertEquals("text/plain", emailImpl.getContentType());
    }

    @Test
    void getContentType_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getContentType()).thenThrow(new MessagingException("error"));
        assertThrows(IllegalStateException.class, () -> emailImpl.getContentType());
    }

    // ============== markAsRead / markAsUnread ==============

    @Test
    void markAsRead_callsSetFlag() throws MessagingException {
        emailImpl.markAsRead();
        verify(mockMessage).setFlag(Flags.Flag.SEEN, true);
    }

    @Test
    void markAsRead_messagingException_throwsIllegalStateException() throws MessagingException {
        doThrow(new MessagingException("error")).when(mockMessage).setFlag(Flags.Flag.SEEN, true);
        assertThrows(IllegalStateException.class, () -> emailImpl.markAsRead());
    }

    @Test
    void markAsUnread_callsSetFlag() throws MessagingException {
        emailImpl.markAsUnread();
        verify(mockMessage).setFlag(Flags.Flag.RECENT, true);
    }

    @Test
    void markAsUnread_messagingException_throwsIllegalStateException() throws MessagingException {
        doThrow(new MessagingException("error")).when(mockMessage).setFlag(Flags.Flag.RECENT, true);
        assertThrows(IllegalStateException.class, () -> emailImpl.markAsUnread());
    }

    // ============== getItemAttachments ==============

    @Test
    void getItemAttachments_returnsEmptyList() {
        List<String> result = emailImpl.getItemAttachments();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ============== replyText ==============

    @Test
    void replyText_throwsNotSupportedException() {
        assertThrows(NotSupportedException.class, () -> emailImpl.replyText("msg", List.of()));
    }

    // ============== getFolderName ==============

    @Test
    void getFolderName_returnsFolderName() {
        when(mockMessage.getFolder()).thenReturn(mockFolder);
        when(mockFolder.getName()).thenReturn("INBOX");
        assertEquals("INBOX", emailImpl.getFolderName());
    }

    // ============== getContent ==============

    @Test
    void getContent_textPlain_returnsText() throws Exception {
        when(mockMessage.isMimeType("text/plain")).thenReturn(true);
        when(mockMessage.getContent()).thenReturn("Hello World");
        assertEquals("Hello World", emailImpl.getContent());
    }

    @Test
    void getContent_notTextPlain_notMultipart_returnsEmpty() throws Exception {
        when(mockMessage.isMimeType("text/plain")).thenReturn(false);
        when(mockMessage.isMimeType("multipart/*")).thenReturn(false);
        assertEquals("", emailImpl.getContent());
    }

    @Test
    void getContent_messagingException_throwsIllegalStateException() throws Exception {
        when(mockMessage.isMimeType("text/plain")).thenThrow(new MessagingException("error"));
        assertThrows(IllegalStateException.class, () -> emailImpl.getContent());
    }

    // ============== getFileAttachments ==============

    @Test
    void getFileAttachments_withNonMultipartContent_returnsEmptyList() throws Exception {
        when(mockMessage.getContent()).thenReturn("plain text content");
        List<?> attachments = emailImpl.getFileAttachments();
        assertNotNull(attachments);
        assertTrue(attachments.isEmpty());
    }

    @Test
    void getFileAttachments_messagingException_throwsServerErrorException() throws Exception {
        when(mockMessage.getContent()).thenThrow(new MessagingException("error"));
        assertThrows(ServerErrorException.class, () -> emailImpl.getFileAttachments());
    }
}
