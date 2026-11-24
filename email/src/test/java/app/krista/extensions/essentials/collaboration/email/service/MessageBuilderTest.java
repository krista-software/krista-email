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

package app.krista.extensions.essentials.collaboration.email.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageBuilder.
 */
@ExtendWith(MockitoExtension.class)
class MessageBuilderTest {

    @Mock
    private KristaMediaClient mockKristaMediaClient;

    private Session session;

    @BeforeEach
    void setUp() {
        session = Session.getDefaultInstance(new Properties());
    }

    @Test
    void subject_setsSubject() throws MessagingException {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        Message msg = builder
                .subject("Test Subject")
                .from("from@example.com")
                .to("to@example.com")
                .attachments(null, 0L)
                .build();
        assertEquals("Test Subject", msg.getSubject());
    }

    @Test
    void from_setsFromAddress() throws MessagingException {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        Message msg = builder
                .subject("Test")
                .from("sender@example.com")
                .to("to@example.com")
                .attachments(null, 0L)
                .build();
        assertNotNull(msg.getFrom());
        assertEquals("sender@example.com", ((javax.mail.internet.InternetAddress) msg.getFrom()[0]).getAddress());
    }

    @Test
    void to_setsToRecipients() throws MessagingException {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        Message msg = builder
                .subject("Test")
                .from("from@example.com")
                .to("to1@example.com,to2@example.com")
                .attachments(null, 0L)
                .build();
        javax.mail.Address[] recipients = msg.getRecipients(Message.RecipientType.TO);
        assertNotNull(recipients);
        assertEquals(2, recipients.length);
    }

    @Test
    void cc_setCcRecipients() throws MessagingException {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        Message msg = builder
                .subject("Test")
                .from("from@example.com")
                .to("to@example.com")
                .cc("cc@example.com")
                .attachments(null, 0L)
                .build();
        javax.mail.Address[] cc = msg.getRecipients(Message.RecipientType.CC);
        assertNotNull(cc);
        assertEquals(1, cc.length);
    }

    @Test
    void bcc_setsBccRecipients() throws MessagingException {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        Message msg = builder
                .subject("Test")
                .from("from@example.com")
                .to("to@example.com")
                .bcc("bcc@example.com")
                .attachments(null, 0L)
                .build();
        javax.mail.Address[] bcc = msg.getRecipients(Message.RecipientType.BCC);
        assertNotNull(bcc);
        assertEquals(1, bcc.length);
    }

    @Test
    void replyTo_setsReplyToAddresses() throws MessagingException {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        Message msg = builder
                .subject("Test")
                .from("from@example.com")
                .to("to@example.com")
                .replyTo("replyto@example.com")
                .attachments(null, 0L)
                .build();
        javax.mail.Address[] replyTo = msg.getReplyTo();
        assertNotNull(replyTo);
        assertEquals(1, replyTo.length);
    }

    @Test
    void messageBody_setsBodyContent() {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        MessageBuilder result = builder.messageBody("Hello <b>World</b>");
        assertNotNull(result); // method chaining works
    }

    @Test
    void attachments_withNullList_doesNotThrow() {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        assertDoesNotThrow(() -> builder
                .subject("Test")
                .from("from@example.com")
                .to("to@example.com")
                .attachments(null, 26214400L)
                .build());
    }

    @Test
    void attachments_withEmptyList_doesNotThrow() {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        assertDoesNotThrow(() -> builder
                .subject("Test")
                .from("from@example.com")
                .to("to@example.com")
                .attachments(List.of(), 26214400L)
                .build());
    }

    @Test
    void build_withBodyContent_createsMessage() throws MessagingException {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        Message msg = builder
                .subject("Subject")
                .from("from@example.com")
                .to("to@example.com")
                .messageBody("Hello World")
                .attachments(null, 0L)
                .build();
        assertNotNull(msg);
    }

    @Test
    void build_withNullFrom_doesNotThrow() {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        assertDoesNotThrow(() -> builder
                .subject("Test")
                .to("to@example.com")
                .attachments(null, 0L)
                .build());
    }

    @Test
    void from_withNull_doesNotThrow() {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        // null emailAddress → getInternetAddresses returns null → fromAddress = null
        assertDoesNotThrow(() -> builder.from(null));
    }

    @Test
    void to_withNull_doesNotThrow() {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        assertDoesNotThrow(() -> builder.to(null));
    }

    @Test
    void cc_withNull_doesNotThrow() {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        assertDoesNotThrow(() -> builder.cc(null));
    }

    @Test
    void bcc_withNull_doesNotThrow() {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        assertDoesNotThrow(() -> builder.bcc(null));
    }

    @Test
    void replyTo_withNull_doesNotThrow() {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        assertDoesNotThrow(() -> builder.replyTo(null));
    }

    @Test
    void methodChaining_returnsBuilderInstance() {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        MessageBuilder result = builder
                .subject("Test")
                .from("from@example.com")
                .to("to@example.com")
                .cc("cc@example.com")
                .bcc("bcc@example.com")
                .replyTo("reply@example.com");
        assertSame(builder, result);
    }
}