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

import app.krista.extensions.essentials.collaboration.email.service.*;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.Message;
import javax.mail.Session;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessagingArea.
 */
@ExtendWith(MockitoExtension.class)
class MessagingAreaTest {

    @Mock
    private EmailClient emailClient;

    @Mock
    private KristaMediaClient kristaMediaClient;

    @Mock
    private MessageClient messageClient;

    @Mock
    private FolderClient folderClient;

    @Mock
    private Folder folder;

    @Mock
    private SmtpClient smtpClient;

    @Mock
    private EmailAttributeStore emailAttributeStore;

    private MessagingArea messagingArea;
    private Session session;

    @BeforeEach
    void setUp() {
        session = Session.getDefaultInstance(new Properties());
        messagingArea = new MessagingArea(emailClient, kristaMediaClient);
    }

    // ============== moveMessage ==============

    @Test
    void moveMessage_callsEnqueueWithDecodedId() {
        when(emailClient.getImapClient()).thenReturn(messageClient);
        when(messageClient.enqueueMessageForMove("INBOX", "<abc@example.com>"))
                .thenReturn("task-id-1");
        String result = messagingArea.moveMessage("&lt;abc@example.com&gt;", "INBOX");
        assertEquals("task-id-1", result);
        verify(messageClient).enqueueMessageForMove("INBOX", "<abc@example.com>");
    }

    @Test
    void moveMessage_withPlainMessageId_callsEnqueueDirectly() {
        when(emailClient.getImapClient()).thenReturn(messageClient);
        when(messageClient.enqueueMessageForMove("Sent", "msg-plain-id"))
                .thenReturn("task-id-2");
        String result = messagingArea.moveMessage("msg-plain-id", "Sent");
        assertEquals("task-id-2", result);
    }

    @Test
    void moveMessage_withEncodedAngles_decodesBeforeEnqueue() {
        when(emailClient.getImapClient()).thenReturn(messageClient);
        when(messageClient.enqueueMessageForMove(anyString(), anyString())).thenReturn("ok");
        messagingArea.moveMessage("&lt;test&gt;", "inbox");
        verify(messageClient).enqueueMessageForMove("inbox", "<test>");
    }

    // ============== sendMail ==============

    @Test
    void sendMail_withAllFields_sendsEmailAndReturnsSuccess() throws Exception {
        when(emailClient.getSmtpClient()).thenReturn(smtpClient);
        when(smtpClient.getSession()).thenReturn(session);
        when(emailClient.me()).thenReturn("from@example.com");
        when(emailClient.getMaxAttachmentSize()).thenReturn(26214400L);

        String result = messagingArea.sendMail(
                "Test Subject",
                "Hello World",
                null,
                "to@example.com",
                null,
                null,
                null);

        assertEquals("Mail sent successfully.", result);
        verify(smtpClient).send(any(Message.class));
    }

    @Test
    void sendMail_replaceSemicolonWithCommaInTo() throws Exception {
        when(emailClient.getSmtpClient()).thenReturn(smtpClient);
        when(smtpClient.getSession()).thenReturn(session);
        when(emailClient.me()).thenReturn("from@example.com");
        when(emailClient.getMaxAttachmentSize()).thenReturn(26214400L);

        // "to1@example.com;to2@example.com" should be converted to comma-separated
        String result = messagingArea.sendMail(
                "Subject",
                "Body",
                null,
                "to1@example.com;to2@example.com",
                null,
                null,
                null);

        assertEquals("Mail sent successfully.", result);
    }

    @Test
    void sendMail_withNullToField_doesNotThrow() throws Exception {
        when(emailClient.getSmtpClient()).thenReturn(smtpClient);
        when(smtpClient.getSession()).thenReturn(session);
        when(emailClient.me()).thenReturn("from@example.com");
        when(emailClient.getMaxAttachmentSize()).thenReturn(26214400L);

        String result = messagingArea.sendMail("Subj", "Body", null, null, null, null, null);
        assertEquals("Mail sent successfully.", result);
    }

    @Test
    void sendMail_withCcAndBcc_populatesFields() throws Exception {
        when(emailClient.getSmtpClient()).thenReturn(smtpClient);
        when(smtpClient.getSession()).thenReturn(session);
        when(emailClient.me()).thenReturn("from@example.com");
        when(emailClient.getMaxAttachmentSize()).thenReturn(26214400L);

        String result = messagingArea.sendMail(
                "Subject", "Body", null,
                "to@example.com",
                "bcc@example.com",
                "cc@example.com",
                "reply@example.com");
        assertEquals("Mail sent successfully.", result);
    }

    // ============== fetchMails ==============

    @Test
    void fetchMails_withNoMessages_returnsEmptyList() {
        when(emailClient.getEmailStore()).thenReturn(emailAttributeStore);
        when(emailAttributeStore.getLastFetchMailsRequestTime()).thenReturn(0L);
        when(emailClient.getFolderClient()).thenReturn(folderClient);
        when(folderClient.getFolder("INBOX")).thenReturn(folder);
        when(folder.search(anyLong())).thenReturn(new Message[0]);

        List<?> result = messagingArea.fetchMails("INBOX", null, null, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(emailAttributeStore).saveLastFetchMailsRequestTime(anyLong());
    }

    @Test
    void fetchMails_savesCurrentRequestTime() {
        when(emailClient.getEmailStore()).thenReturn(emailAttributeStore);
        when(emailAttributeStore.getLastFetchMailsRequestTime()).thenReturn(1000L);
        when(emailClient.getFolderClient()).thenReturn(folderClient);
        when(folderClient.getFolder("Sent")).thenReturn(folder);
        when(folder.search(anyLong())).thenReturn(new Message[0]);

        messagingArea.fetchMails("Sent", null, null, null);
        verify(emailAttributeStore).saveLastFetchMailsRequestTime(anyLong());
    }
}
