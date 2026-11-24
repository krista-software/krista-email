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

package app.krista.extensions.essentials.collaboration.email.service.impl.filter;

import app.krista.extensions.essentials.collaboration.email.exception.MatchingFailureException;
import app.krista.extensions.essentials.collaboration.email.service.AbstractFilter;
import app.krista.extensions.essentials.collaboration.email.service.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubjectFilter.
 * Tests subject pattern matching, filter chaining, and exception handling.
 */
@ExtendWith(MockitoExtension.class)
class SubjectFilterTest {

    @Mock
    private Email email;

    @Mock
    private AbstractFilter<Email> nextFilter;

    private SubjectFilter subjectFilter;

    @BeforeEach
    void setUp() {
        lenient().when(email.getSubject()).thenReturn("Test Subject");
    }

    @Test
    void testApply_SubjectMatches_ReturnsEmail() throws MatchingFailureException {
        // Arrange
        subjectFilter = new SubjectFilter(".*Test.*");

        // Act
        Email result = subjectFilter.apply(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result);
    }

    @Test
    void testApply_SubjectDoesNotMatch_ThrowsException() {
        // Arrange
        subjectFilter = new SubjectFilter(".*Important.*");

        // Act & Assert
        assertThrows(MatchingFailureException.class, () -> subjectFilter.apply(email));
    }

    @Test
    void testApply_NullOrBlankRegex_ReturnsNull() throws MatchingFailureException {
        // Arrange - null regex
        subjectFilter = new SubjectFilter(null);

        // Act
        Email result = subjectFilter.apply(email);

        // Assert
        assertNull(result);
    }

    @Test
    void testApply_BlankRegex_ReturnsNull() throws MatchingFailureException {
        // Arrange - blank regex
        subjectFilter = new SubjectFilter("   ");

        // Act
        Email result = subjectFilter.apply(email);

        // Assert
        assertNull(result);
    }

    @Test
    void testApply_WithNextFilter_BothMatch_ReturnsEmail() throws MatchingFailureException {
        // Arrange
        subjectFilter = new SubjectFilter(".*Test.*");
        subjectFilter.setNextFilter(nextFilter);
        when(nextFilter.apply(email)).thenReturn(email);

        // Act
        Email result = subjectFilter.apply(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result);
        verify(nextFilter).apply(email);
    }

    @Test
    void testApply_WithNextFilter_NextFilterReturnsNull_ChecksSubject() throws MatchingFailureException {
        // Arrange
        subjectFilter = new SubjectFilter(".*Test.*");
        subjectFilter.setNextFilter(nextFilter);
        when(nextFilter.apply(email)).thenReturn(null);

        // Act
        Email result = subjectFilter.apply(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result);
        verify(nextFilter).apply(email);
    }

    @Test
    void testApply_WithNextFilter_SubjectDoesNotMatch_ThrowsException() throws MatchingFailureException {
        // Arrange
        subjectFilter = new SubjectFilter(".*Important.*");
        subjectFilter.setNextFilter(nextFilter);
        when(nextFilter.apply(email)).thenReturn(email);

        // Act & Assert
        assertThrows(MatchingFailureException.class, () -> subjectFilter.apply(email));
        verify(nextFilter).apply(email);
    }

    @Test
    void testApply_CaseInsensitiveMatch_ReturnsEmail() throws MatchingFailureException {
        // Arrange
        when(email.getSubject()).thenReturn("test subject");
        subjectFilter = new SubjectFilter(".*TEST.*");

        // Act
        Email result = subjectFilter.apply(email);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testApply_ComplexRegex_ReturnsEmail() throws MatchingFailureException {
        // Arrange
        when(email.getSubject()).thenReturn("Invoice #12345");
        subjectFilter = new SubjectFilter(".*Invoice #[0-9]+.*");

        // Act
        Email result = subjectFilter.apply(email);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testSetNextFilter_SetsCorrectly() {
        // Arrange
        subjectFilter = new SubjectFilter(".*Test.*");

        // Act
        subjectFilter.setNextFilter(nextFilter);

        // Assert - verify by applying and checking nextFilter is called
        when(nextFilter.apply(email)).thenReturn(email);
        assertDoesNotThrow(() -> subjectFilter.apply(email));
        verify(nextFilter).apply(email);
    }
}

