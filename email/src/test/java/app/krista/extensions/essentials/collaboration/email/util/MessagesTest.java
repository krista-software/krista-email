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

package app.krista.extensions.essentials.collaboration.email.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {

    @Test
    void decodeMessageId_withEncodedAngleBrackets_returnsDecoded() {
        String encoded = "&lt;abc123@mail.example.com&gt;";
        String decoded = Messages.decodeMessageId(encoded);
        assertEquals("<abc123@mail.example.com>", decoded);
    }

    @Test
    void decodeMessageId_withNormalAngleBrackets_returnsUnchanged() {
        String messageId = "<abc123@mail.example.com>";
        String result = Messages.decodeMessageId(messageId);
        assertEquals("<abc123@mail.example.com>", result);
    }

    @Test
    void decodeMessageId_withNoAngleBrackets_returnsUnchanged() {
        String messageId = "plain-message-id";
        assertEquals("plain-message-id", Messages.decodeMessageId(messageId));
    }

    @Test
    void encodeMessageId_withAngleBrackets_returnsEncoded() {
        String messageId = "<abc123@mail.example.com>";
        String encoded = Messages.encodeMessageId(messageId);
        assertEquals("&lt;abc123@mail.example.com&gt;", encoded);
    }

    @Test
    void encodeMessageId_withEncodedBrackets_returnsUnchanged() {
        String messageId = "&lt;abc123@mail.example.com&gt;";
        String result = Messages.encodeMessageId(messageId);
        assertEquals("&lt;abc123@mail.example.com&gt;", result);
    }

    @Test
    void encodeMessageId_withNoAngleBrackets_returnsUnchanged() {
        String messageId = "plain-message-id";
        assertEquals("plain-message-id", Messages.encodeMessageId(messageId));
    }

    @Test
    void decodeAndEncode_areInverses() {
        String original = "<test.message@example.com>";
        String encoded = Messages.encodeMessageId(original);
        String decoded = Messages.decodeMessageId(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void constants_haveNonNullValues() {
        assertNotNull(Messages.FOLDER_DOES_NOT_EXISTS);
        assertNotNull(Messages.UNMATCHED_SUBJECT);
        assertNotNull(Messages.UNMATCHED_FOLDER);
        assertNotNull(Messages.UNMATCHED_SENDER);
        assertNotNull(Messages.SERVER_ERROR);
        assertNotNull(Messages.MAIL_MOVED_SUCCESSFULLY);
        assertNotNull(Messages.MAIL_MOVEMENT_FAILED);
        assertNotNull(Messages.CONNECTION_RESET);
    }
}