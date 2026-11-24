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

import app.krista.extensions.essentials.collaboration.email.exception.MatchingFailureException;
import app.krista.extensions.essentials.collaboration.email.service.AbstractFilter;
import app.krista.extensions.essentials.collaboration.email.service.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FolderFilter.
 */
@ExtendWith(MockitoExtension.class)
class FolderFilterTest {

    @Mock
    private Email email;

    @Mock
    private AbstractFilter<Email> nextFilter;

    @Test
    void apply_nullFolderName_noNextFilter_returnsNull() throws MatchingFailureException {
        FolderFilter filter = new FolderFilter(null);
        Email result = filter.apply(email);
        assertNull(result);
    }

    @Test
    void apply_blankFolderName_noNextFilter_returnsNull() throws MatchingFailureException {
        FolderFilter filter = new FolderFilter("   ");
        Email result = filter.apply(email);
        assertNull(result);
    }

    @Test
    void apply_matchingFolder_noNextFilter_returnsMail() throws MatchingFailureException {
        when(email.getFolderName()).thenReturn("INBOX");
        FolderFilter filter = new FolderFilter("inbox");
        Email result = filter.apply(email);
        assertSame(email, result);
    }

    @Test
    void apply_matchingFolderCaseInsensitive_noNextFilter_returnsMail() throws MatchingFailureException {
        when(email.getFolderName()).thenReturn("Sent");
        FolderFilter filter = new FolderFilter("SENT");
        Email result = filter.apply(email);
        assertSame(email, result);
    }

    @Test
    void apply_nonMatchingFolder_noNextFilter_throwsException() {
        when(email.getFolderName()).thenReturn("inbox");
        FolderFilter filter = new FolderFilter("drafts");
        assertThrows(MatchingFailureException.class, () -> filter.apply(email));
    }

    @Test
    void apply_withNextFilter_nextReturnsEmail_folderMatches_returnsMail() throws MatchingFailureException {
        when(nextFilter.apply(email)).thenReturn(email);
        when(email.getFolderName()).thenReturn("inbox");
        FolderFilter filter = new FolderFilter("inbox");
        filter.setNextFilter(nextFilter);
        Email result = filter.apply(email);
        assertSame(email, result);
        verify(nextFilter).apply(email);
    }

    @Test
    void apply_withNextFilter_nextReturnsEmail_folderDoesNotMatch_throwsException() throws MatchingFailureException {
        when(nextFilter.apply(email)).thenReturn(email);
        when(email.getFolderName()).thenReturn("inbox");
        FolderFilter filter = new FolderFilter("drafts");
        filter.setNextFilter(nextFilter);
        assertThrows(MatchingFailureException.class, () -> filter.apply(email));
        verify(nextFilter).apply(email);
    }

    @Test
    void apply_withNextFilter_nextReturnsEmail_nullFolderName_returnsFilteredMail() throws MatchingFailureException {
        when(nextFilter.apply(email)).thenReturn(email);
        FolderFilter filter = new FolderFilter(null);
        filter.setNextFilter(nextFilter);
        Email result = filter.apply(email);
        assertSame(email, result);
        verify(nextFilter).apply(email);
    }

    @Test
    void apply_withNextFilter_nextReturnsNull_folderMatches_returnsMail() throws MatchingFailureException {
        when(nextFilter.apply(email)).thenReturn(null);
        when(email.getFolderName()).thenReturn("inbox");
        FolderFilter filter = new FolderFilter("inbox");
        filter.setNextFilter(nextFilter);
        Email result = filter.apply(email);
        assertSame(email, result);
    }

    @Test
    void apply_withNextFilter_nextReturnsNull_folderDoesNotMatch_throwsException() throws MatchingFailureException {
        when(nextFilter.apply(email)).thenReturn(null);
        when(email.getFolderName()).thenReturn("inbox");
        FolderFilter filter = new FolderFilter("drafts");
        filter.setNextFilter(nextFilter);
        assertThrows(MatchingFailureException.class, () -> filter.apply(email));
    }

    @Test
    void apply_withNextFilter_nextReturnsNull_nullFolderName_returnsNull() throws MatchingFailureException {
        when(nextFilter.apply(email)).thenReturn(null);
        FolderFilter filter = new FolderFilter(null);
        filter.setNextFilter(nextFilter);
        Email result = filter.apply(email);
        assertNull(result);
    }

    @Test
    void setNextFilter_setsCorrectly() throws MatchingFailureException {
        FolderFilter filter = new FolderFilter(null);
        filter.setNextFilter(nextFilter);
        when(nextFilter.apply(email)).thenReturn(null);
        filter.apply(email);
        verify(nextFilter).apply(email);
    }
}
