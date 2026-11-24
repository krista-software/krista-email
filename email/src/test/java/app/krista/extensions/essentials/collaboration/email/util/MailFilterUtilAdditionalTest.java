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

import app.krista.extensions.essentials.collaboration.email.service.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * JUnit 5 tests for MailFilterUtil covering additional paths.
 */
@ExtendWith(MockitoExtension.class)
class MailFilterUtilAdditionalTest {

    @Mock
    private Email mockEmail;

    // ============== filterMail ==============

    @Test
    void filterMail_withAllNullParams_returnsNull() {
        assertNull(MailFilterUtil.filterMail(mockEmail, null, null, null, null));
    }

    @Test
    void filterMail_withNonMatchingFolder_returnsNull() {
        // FolderFilter compares email.getFolderName() with expected folder
        // "Drafts" != "INBOX" → throws MatchingFailureException → filterMail returns null
        when(mockEmail.getFolderName()).thenReturn("Drafts");
        Email result = MailFilterUtil.filterMail(mockEmail, "INBOX", null, null, null);
        assertNull(result);
    }

    // ============== isPatternMatches ==============

    @Test
    void isPatternMatches_withMatchingPattern_returnsTrue() {
        assertTrue(MailFilterUtil.isPatternMatches("hello world", "hello"));
    }

    @Test
    void isPatternMatches_withNonMatchingPattern_returnsFalse() {
        assertFalse(MailFilterUtil.isPatternMatches("hello world", "xyz"));
    }

    @Test
    void isPatternMatches_withMultiplePatternsFirstMatches_returnsTrue() {
        assertTrue(MailFilterUtil.isPatternMatches("hello world", "hello;xyz"));
    }

    @Test
    void isPatternMatches_withMultiplePatternsSecondMatches_returnsTrue() {
        assertTrue(MailFilterUtil.isPatternMatches("hello world", "xyz;world"));
    }

    @Test
    void isPatternMatches_withMultiplePatternsNoneMatch_returnsFalse() {
        assertFalse(MailFilterUtil.isPatternMatches("hello world", "abc;def;xyz"));
    }

    @Test
    void isPatternMatches_isCaseInsensitive() {
        assertTrue(MailFilterUtil.isPatternMatches("HELLO WORLD", "hello"));
    }

    @Test
    void isPatternMatches_withRegexPattern_matchesCorrectly() {
        assertTrue(MailFilterUtil.isPatternMatches("test@example.com", ".*@example\\.com"));
    }
}
