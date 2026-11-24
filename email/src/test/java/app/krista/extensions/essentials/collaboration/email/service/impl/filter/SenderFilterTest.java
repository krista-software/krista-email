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
import app.krista.extensions.essentials.collaboration.email.service.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SenderFilter.
 * Tests sender pattern matching, filter chaining, and exception handling.
 */
@ExtendWith(MockitoExtension.class)
class SenderFilterTest {

    @Mock
    private Email email;

    @Mock
    private EmailAddress senderAddress;

    @Mock
    private AbstractFilter<Email> nextFilter;

    private SenderFilter senderFilter;

    @BeforeEach
    void setUp() {
        lenient().when(email.getSenderEmailAddress()).thenReturn(senderAddress);
        lenient().when(senderAddress.getAddress()).thenReturn("sender@example.com");
    }

    @Test
    void testApply_SenderMatches_ReturnsEmail() throws MatchingFailureException {
        // Arrange
        senderFilter = new SenderFilter(".*@example\\.com");

        // Act
        Email result = senderFilter.apply(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result);
    }

    @Test
    void testApply_SenderDoesNotMatch_ThrowsException() {
        // Arrange
        senderFilter = new SenderFilter(".*@gmail\\.com");

        // Act & Assert
        assertThrows(MatchingFailureException.class, () -> senderFilter.apply(email));
    }

    @Test
    void testApply_NullRegex_ReturnsNull() throws MatchingFailureException {
        // Arrange
        senderFilter = new SenderFilter(null);

        // Act
        Email result = senderFilter.apply(email);

        // Assert
        assertNull(result);
    }

    @Test
    void testApply_BlankRegex_ReturnsNull() throws MatchingFailureException {
        // Arrange
        senderFilter = new SenderFilter("   ");

        // Act
        Email result = senderFilter.apply(email);

        // Assert
        assertNull(result);
    }

    @Test
    void testApply_WithNextFilter_BothMatch_ReturnsEmail() throws MatchingFailureException {
        // Arrange
        senderFilter = new SenderFilter(".*@example\\.com");
        senderFilter.setNextFilter(nextFilter);
        when(nextFilter.apply(email)).thenReturn(email);

        // Act
        Email result = senderFilter.apply(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result);
        verify(nextFilter).apply(email);
    }

    @Test
    void testApply_WithNextFilter_NextFilterReturnsNull_ChecksSender() throws MatchingFailureException {
        // Arrange
        senderFilter = new SenderFilter(".*@example\\.com");
        senderFilter.setNextFilter(nextFilter);
        when(nextFilter.apply(email)).thenReturn(null);

        // Act
        Email result = senderFilter.apply(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result);
        verify(nextFilter).apply(email);
    }

    @Test
    void testApply_WithNextFilter_SenderDoesNotMatch_ThrowsException() throws MatchingFailureException {
        // Arrange
        senderFilter = new SenderFilter(".*@gmail\\.com");
        senderFilter.setNextFilter(nextFilter);
        when(nextFilter.apply(email)).thenReturn(email);

        // Act & Assert
        assertThrows(MatchingFailureException.class, () -> senderFilter.apply(email));
        verify(nextFilter).apply(email);
    }

    @Test
    void testApply_SpecificSenderEmail_ReturnsEmail() throws MatchingFailureException {
        // Arrange
        when(senderAddress.getAddress()).thenReturn("john.doe@company.com");
        senderFilter = new SenderFilter("john\\.doe@company\\.com");

        // Act
        Email result = senderFilter.apply(email);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testApply_WildcardPattern_ReturnsEmail() throws MatchingFailureException {
        // Arrange
        when(senderAddress.getAddress()).thenReturn("admin@example.com");
        senderFilter = new SenderFilter(".*admin.*");

        // Act
        Email result = senderFilter.apply(email);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testApply_CaseInsensitiveMatch_ReturnsEmail() throws MatchingFailureException {
        // Arrange
        when(senderAddress.getAddress()).thenReturn("Admin@Example.COM");
        senderFilter = new SenderFilter(".*admin@example\\.com");

        // Act
        Email result = senderFilter.apply(email);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testSetNextFilter_SetsCorrectly() {
        // Arrange
        senderFilter = new SenderFilter(".*@example\\.com");

        // Act
        senderFilter.setNextFilter(nextFilter);

        // Assert - verify by applying and checking nextFilter is called
        when(nextFilter.apply(email)).thenReturn(email);
        assertDoesNotThrow(() -> senderFilter.apply(email));
        verify(nextFilter).apply(email);
    }
}

