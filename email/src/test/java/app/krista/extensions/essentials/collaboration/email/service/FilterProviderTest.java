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

import app.krista.extensions.essentials.collaboration.email.exception.MatchingFailureException;
import app.krista.extensions.essentials.collaboration.email.mock.EmailMock;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FilterProvider — verifies that the filter chain is correctly composed.
 */
class FilterProviderTest {

    private Email createEmail(String sender, String subject, String folder,
                               List<EmailAddress> to, List<EmailAddress> cc) {
        return new EmailMock(sender, subject, folder, to, cc);
    }

    @Test
    void get_returnsNonNullFilter() {
        AbstractFilter<Email> filter = FilterProvider.get("inbox", null, null, null);
        assertNotNull(filter);
    }

    @Test
    void get_withMatchingFolder_returnsEmail() throws MatchingFailureException {
        Email email = createEmail("sender@example.com", "Test subject", "inbox",
                List.of(new EmailAddress("User", "user@example.com")),
                List.of());
        AbstractFilter<Email> filter = FilterProvider.get("inbox", null, null, null);
        Email result = filter.apply(email);
        assertNotNull(result);
    }

    @Test
    void get_withNonMatchingFolder_throwsException() {
        Email email = createEmail("sender@example.com", "Test subject", "inbox",
                List.of(new EmailAddress("User", "user@example.com")),
                List.of());
        AbstractFilter<Email> filter = FilterProvider.get("drafts", null, null, null);
        assertThrows(MatchingFailureException.class, () -> filter.apply(email));
    }

    @Test
    void get_withAllNullParams_returnsNullForEachFilter() throws MatchingFailureException {
        Email email = createEmail("sender@example.com", "Test subject", "inbox",
                List.of(new EmailAddress("User", "user@example.com")),
                List.of());
        AbstractFilter<Email> filter = FilterProvider.get(null, null, null, null);
        // With all null filters, each individual filter returns null
        Email result = filter.apply(email);
        assertNull(result);
    }

    @Test
    void get_withMatchingAllFilters_returnsEmail() throws MatchingFailureException {
        Email email = createEmail("sender@example.com", "Test subject", "inbox",
                List.of(new EmailAddress("User", "recipient@example.com")),
                List.of());
        AbstractFilter<Email> filter = FilterProvider.get(
                "inbox",
                ".*sender@example\\.com.*",
                ".*Test.*",
                ".*recipient.*");
        Email result = filter.apply(email);
        assertNotNull(result);
    }

    @Test
    void get_withNonMatchingSender_throwsException() {
        Email email = createEmail("sender@example.com", "Test subject", "inbox",
                List.of(new EmailAddress("User", "recipient@example.com")),
                List.of());
        AbstractFilter<Email> filter = FilterProvider.get(
                "inbox",
                ".*noone@example\\.com.*",
                null,
                null);
        assertThrows(MatchingFailureException.class, () -> filter.apply(email));
    }

    @Test
    void get_withNonMatchingSubject_throwsException() {
        Email email = createEmail("sender@example.com", "Test subject", "inbox",
                List.of(new EmailAddress("User", "recipient@example.com")),
                List.of());
        AbstractFilter<Email> filter = FilterProvider.get(
                null,
                null,
                ".*Invoice.*",
                null);
        assertThrows(MatchingFailureException.class, () -> filter.apply(email));
    }
}