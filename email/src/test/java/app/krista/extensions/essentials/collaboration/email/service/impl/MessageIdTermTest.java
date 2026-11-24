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

package app.krista.extensions.essentials.collaboration.email.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageIdTerm.
 */
@ExtendWith(MockitoExtension.class)
class MessageIdTermTest {

    private static final String MESSAGE_ID = "<abc123@mail.example.com>";

    @Test
    void match_withMatchingMessageId_returnsTrue() throws MessagingException {
        MimeMessage msg = mock(MimeMessage.class);
        when(msg.getMessageID()).thenReturn(MESSAGE_ID);

        MessageIdTerm term = new MessageIdTerm(MESSAGE_ID);
        assertTrue(term.match(msg));
    }

    @Test
    void match_withDifferentMessageId_returnsFalse() throws MessagingException {
        MimeMessage msg = mock(MimeMessage.class);
        when(msg.getMessageID()).thenReturn("<different@example.com>");

        MessageIdTerm term = new MessageIdTerm(MESSAGE_ID);
        assertFalse(term.match(msg));
    }

    @Test
    void match_withNullMessageId_returnsFalse() throws MessagingException {
        MimeMessage msg = mock(MimeMessage.class);
        when(msg.getMessageID()).thenReturn(null);

        MessageIdTerm term = new MessageIdTerm(MESSAGE_ID);
        assertFalse(term.match(msg));
    }

    @Test
    void match_whenGetMessageIdThrows_returnsFalse() throws MessagingException {
        MimeMessage msg = mock(MimeMessage.class);
        when(msg.getMessageID()).thenThrow(new MessagingException("IMAP error"));

        MessageIdTerm term = new MessageIdTerm(MESSAGE_ID);
        assertFalse(term.match(msg));
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        MessageIdTerm term = new MessageIdTerm(MESSAGE_ID);
        assertEquals(term, term);
    }

    @Test
    void equals_sameMessageId_returnsTrue() {
        MessageIdTerm term1 = new MessageIdTerm(MESSAGE_ID);
        MessageIdTerm term2 = new MessageIdTerm(MESSAGE_ID);
        assertEquals(term1, term2);
    }

    @Test
    void equals_differentMessageId_returnsFalse() {
        MessageIdTerm term1 = new MessageIdTerm(MESSAGE_ID);
        MessageIdTerm term2 = new MessageIdTerm("<other@example.com>");
        assertNotEquals(term1, term2);
    }

    @Test
    void equals_null_returnsFalse() {
        MessageIdTerm term = new MessageIdTerm(MESSAGE_ID);
        assertNotEquals(term, null);
    }

    @Test
    void equals_differentClass_returnsFalse() {
        MessageIdTerm term = new MessageIdTerm(MESSAGE_ID);
        assertNotEquals(term, "not a MessageIdTerm");
    }

    @Test
    void hashCode_sameMessageId_sameHash() {
        MessageIdTerm term1 = new MessageIdTerm(MESSAGE_ID);
        MessageIdTerm term2 = new MessageIdTerm(MESSAGE_ID);
        assertEquals(term1.hashCode(), term2.hashCode());
    }

    @Test
    void hashCode_differentMessageIds_differentHashes() {
        MessageIdTerm term1 = new MessageIdTerm("<abc@example.com>");
        MessageIdTerm term2 = new MessageIdTerm("<xyz@example.com>");
        assertNotEquals(term1.hashCode(), term2.hashCode());
    }
}