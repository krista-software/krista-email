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

import app.krista.extensions.essentials.collaboration.email.service.Attachment;
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
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.ServerErrorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Additional unit tests for EmailImpl covering uncovered code paths.
 */
@ExtendWith(MockitoExtension.class)
class EmailImplAdditionalTest {

    @Mock
    private MimeMessage mockMessage;

    @Mock
    private KristaMediaClient mockKristaMediaClient;

    private EmailImpl emailImpl;

    @BeforeEach
    void setUp() {
        emailImpl = new EmailImpl(mockMessage, mockKristaMediaClient);
    }

    // ============== getUID ==============

    @Test
    void getUID_success_returnsUidString() throws MessagingException {
        javax.mail.Folder mockFolder = mock(javax.mail.Folder.class,
                withSettings().extraInterfaces(UIDFolder.class));
        when(mockMessage.getFolder()).thenReturn(mockFolder);
        when(((UIDFolder) mockFolder).getUID(mockMessage)).thenReturn(42L);
        assertEquals("42", emailImpl.getUID());
    }

    @Test
    void getUID_messagingException_returnsEmptyString() throws MessagingException {
        javax.mail.Folder mockFolder = mock(javax.mail.Folder.class,
                withSettings().extraInterfaces(UIDFolder.class));
        when(mockMessage.getFolder()).thenReturn(mockFolder);
        when(((UIDFolder) mockFolder).getUID(mockMessage)).thenThrow(new MessagingException("uid error"));
        assertEquals("", emailImpl.getUID());
    }

    // ============== getEmailId ==============

    @Test
    void getEmailId_withValidMessage_returnsMessageId() throws Exception {
        Session testSession = Session.getDefaultInstance(new Properties());
        String rawEmail = "Message-ID: <test-id@example.com>\r\nSubject: Test\r\n\r\nBody\r\n";
        InputStream is = new ByteArrayInputStream(rawEmail.getBytes("UTF-8"));
        when(mockMessage.getSession()).thenReturn(testSession);
        when(mockMessage.getInputStream()).thenReturn(is);
        String emailId = emailImpl.getEmailId();
        assertEquals("<test-id@example.com>", emailId);
    }

    @Test
    void getEmailId_withIOException_throwsIllegalStateException() throws Exception {
        Session testSession = Session.getDefaultInstance(new Properties());
        when(mockMessage.getSession()).thenReturn(testSession);
        when(mockMessage.getInputStream()).thenThrow(new IOException("IO error"));
        assertThrows(IllegalStateException.class, emailImpl::getEmailId);
    }

    // ============== getSendDateAndTime ==============

    @Test
    void getSendDateAndTime_returnsDateInMillis() throws MessagingException {
        Date date = new Date(1700000000000L);
        when(mockMessage.getSentDate()).thenReturn(date);
        assertEquals(1700000000000L, emailImpl.getSendDateAndTime());
    }

    @Test
    void getSendDateAndTime_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getSentDate()).thenThrow(new MessagingException("date error"));
        assertThrows(IllegalStateException.class, emailImpl::getSendDateAndTime);
    }

    // ============== getReceivedDateAndTime ==============

    @Test
    void getReceivedDateAndTime_returnsDateInMillis() throws MessagingException {
        Date date = new Date(1800000000000L);
        when(mockMessage.getReceivedDate()).thenReturn(date);
        assertEquals(1800000000000L, emailImpl.getReceivedDateAndTime());
    }

    @Test
    void getReceivedDateAndTime_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getReceivedDate()).thenThrow(new MessagingException("date error"));
        assertThrows(IllegalStateException.class, emailImpl::getReceivedDateAndTime);
    }

    // ============== getContentType ==============

    @Test
    void getContentType_returnsContentType() throws MessagingException {
        when(mockMessage.getContentType()).thenReturn("text/plain; charset=UTF-8");
        assertEquals("text/plain; charset=UTF-8", emailImpl.getContentType());
    }

    @Test
    void getContentType_messagingException_throwsIllegalStateException() throws MessagingException {
        when(mockMessage.getContentType()).thenThrow(new MessagingException("ct error"));
        assertThrows(IllegalStateException.class, emailImpl::getContentType);
    }

    // ============== getBccEmailAddresses ==============

    @Test
    void getBccEmailAddresses_withRecipients_returnsList() throws MessagingException {
        InternetAddress addr = new InternetAddress("bcc@example.com", false);
        when(mockMessage.getRecipients(Message.RecipientType.BCC))
                .thenReturn(new Address[]{addr});
        List<EmailAddress> result = emailImpl.getBccEmailAddresses();
        assertEquals(1, result.size());
        assertEquals("bcc@example.com", result.get(0).getAddress());
    }

    @Test
    void getBccEmailAddresses_withNullRecipients_returnsEmptyList() throws MessagingException {
        when(mockMessage.getRecipients(Message.RecipientType.BCC)).thenReturn(null);
        assertTrue(emailImpl.getBccEmailAddresses().isEmpty());
    }

    @Test
    void getBccEmailAddresses_withEmptyRecipients_returnsEmptyList() throws MessagingException {
        when(mockMessage.getRecipients(Message.RecipientType.BCC)).thenReturn(new Address[0]);
        assertTrue(emailImpl.getBccEmailAddresses().isEmpty());
    }

    // ============== getReplyToEmailAddresses ==============

    @Test
    void getReplyToEmailAddresses_withAddresses_returnsList() throws MessagingException {
        InternetAddress addr = new InternetAddress("reply@example.com", false);
        when(mockMessage.getReplyTo()).thenReturn(new Address[]{addr});
        List<EmailAddress> result = emailImpl.getReplyToEmailAddresses();
        assertEquals(1, result.size());
        assertEquals("reply@example.com", result.get(0).getAddress());
    }

    // ============== getContent multipart paths ==============

    @Test
    void getContent_multipartMixed_withTextPlainBodyPart_returnsText() throws Exception {
        MimeMultipart mimeMultipart = mock(MimeMultipart.class);
        BodyPart bodyPart = mock(BodyPart.class);

        when(mockMessage.isMimeType("text/plain")).thenReturn(false);
        when(mockMessage.isMimeType("multipart/*")).thenReturn(true);
        when(mockMessage.getContent()).thenReturn(mimeMultipart);
        when(mimeMultipart.getCount()).thenReturn(1);
        when(mimeMultipart.getContentType()).thenReturn("multipart/mixed");
        when(mimeMultipart.getBodyPart(0)).thenReturn(bodyPart);
        when(bodyPart.isMimeType("text/plain")).thenReturn(true);
        when(bodyPart.getContent()).thenReturn("Hello World from multipart");

        assertEquals("Hello World from multipart", emailImpl.getContent());
    }

    @Test
    void getContent_multipartAlternative_returnsLastBodyPart() throws Exception {
        MimeMultipart mimeMultipart = mock(MimeMultipart.class);
        BodyPart lastBodyPart = mock(BodyPart.class);

        when(mockMessage.isMimeType("text/plain")).thenReturn(false);
        when(mockMessage.isMimeType("multipart/*")).thenReturn(true);
        when(mockMessage.getContent()).thenReturn(mimeMultipart);
        when(mimeMultipart.getCount()).thenReturn(2);
        when(mimeMultipart.getContentType()).thenReturn("multipart/alternative");
        when(mimeMultipart.getBodyPart(1)).thenReturn(lastBodyPart);
        when(lastBodyPart.isMimeType("text/plain")).thenReturn(true);
        when(lastBodyPart.getContent()).thenReturn("Alternative plain text");

        assertEquals("Alternative plain text", emailImpl.getContent());
    }

    @Test
    void getContent_multipartMixed_withHtmlBodyPart_returnsExtractedText() throws Exception {
        MimeMultipart mimeMultipart = mock(MimeMultipart.class);
        BodyPart bodyPart = mock(BodyPart.class);

        when(mockMessage.isMimeType("text/plain")).thenReturn(false);
        when(mockMessage.isMimeType("multipart/*")).thenReturn(true);
        when(mockMessage.getContent()).thenReturn(mimeMultipart);
        when(mimeMultipart.getCount()).thenReturn(1);
        when(mimeMultipart.getContentType()).thenReturn("multipart/mixed");
        when(mimeMultipart.getBodyPart(0)).thenReturn(bodyPart);
        when(bodyPart.isMimeType("text/plain")).thenReturn(false);
        when(bodyPart.isMimeType("text/html")).thenReturn(true);
        when(bodyPart.getContent()).thenReturn("<html><body><p>Hello HTML</p></body></html>");

        String result = emailImpl.getContent();
        assertEquals("Hello HTML", result);
    }

    @Test
    void getContent_multipartWithZeroBodyParts_throwsIllegalState() throws Exception {
        MimeMultipart mimeMultipart = mock(MimeMultipart.class);

        when(mockMessage.isMimeType("text/plain")).thenReturn(false);
        when(mockMessage.isMimeType("multipart/*")).thenReturn(true);
        when(mockMessage.getContent()).thenReturn(mimeMultipart);
        when(mimeMultipart.getCount()).thenReturn(0);

        assertThrows(IllegalStateException.class, emailImpl::getContent);
    }

    // ============== getFileAttachments with multipart ==============

    @Test
    void getFileAttachments_withMultipartContent_withAttachment_returnsAttachmentList() throws Exception {
        MimeMultipart multipart = mock(MimeMultipart.class);
        BodyPart attachmentPart = mock(BodyPart.class);

        when(mockMessage.getContent()).thenReturn(multipart);
        when(multipart.getCount()).thenReturn(1);
        when(multipart.getBodyPart(0)).thenReturn(attachmentPart);
        when(attachmentPart.getDisposition()).thenReturn(Part.ATTACHMENT);
        when(attachmentPart.getFileName()).thenReturn("file.pdf");

        List<Attachment> result = emailImpl.getFileAttachments();
        assertEquals(1, result.size());
        assertEquals("file.pdf", result.get(0).getName());
    }

    @Test
    void getFileAttachments_withMultipartContent_inlinePart_returnsEmptyList() throws Exception {
        MimeMultipart multipart = mock(MimeMultipart.class);
        BodyPart bodyPart = mock(BodyPart.class);

        when(mockMessage.getContent()).thenReturn(multipart);
        when(multipart.getCount()).thenReturn(1);
        when(multipart.getBodyPart(0)).thenReturn(bodyPart);
        when(bodyPart.getDisposition()).thenReturn(Part.INLINE);

        List<Attachment> result = emailImpl.getFileAttachments();
        assertTrue(result.isEmpty());
    }

    @Test
    void getFileAttachments_withMultipartContent_multipleAttachments_returnsAll() throws Exception {
        MimeMultipart multipart = mock(MimeMultipart.class);
        BodyPart part1 = mock(BodyPart.class);
        BodyPart part2 = mock(BodyPart.class);

        when(mockMessage.getContent()).thenReturn(multipart);
        when(multipart.getCount()).thenReturn(2);
        when(multipart.getBodyPart(0)).thenReturn(part1);
        when(multipart.getBodyPart(1)).thenReturn(part2);
        when(part1.getDisposition()).thenReturn(Part.ATTACHMENT);
        when(part2.getDisposition()).thenReturn(Part.ATTACHMENT);

        List<Attachment> result = emailImpl.getFileAttachments();
        assertEquals(2, result.size());
    }

    @Test
    void getFileAttachments_withIOException_throwsServerErrorException() throws Exception {
        when(mockMessage.getContent()).thenThrow(new IOException("io error"));
        assertThrows(ServerErrorException.class, emailImpl::getFileAttachments);
    }
}
