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

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToCcFilterTest {

    @Mock
    private Email email;
    @Mock
    private EmailAddress toAddress1;
    @Mock
    private EmailAddress ccAddress1;
    @Mock
    private AbstractFilter<Email> nextFilter;

    private ToCcFilter toCcFilter;

    @BeforeEach
    void setUp() {
        lenient().when(toAddress1.getAddress()).thenReturn("recipient@example.com");
        lenient().when(ccAddress1.getAddress()).thenReturn("cc@example.com");
        lenient().when(email.getToEmailAddresses()).thenReturn(Arrays.asList(toAddress1));
        lenient().when(email.getCcEmailAddresses()).thenReturn(Collections.singletonList(ccAddress1));
    }

    @Test
    void testApply_ToAddressMatches_ReturnsEmail() throws MatchingFailureException {
        toCcFilter = new ToCcFilter(".*recipient@example\\.com.*");
        Email result = toCcFilter.apply(email);
        assertNotNull(result);
    }

    @Test
    void testApply_CcAddressMatches_ReturnsEmail() throws MatchingFailureException {
        toCcFilter = new ToCcFilter(".*cc@example\\.com.*");
        Email result = toCcFilter.apply(email);
        assertNotNull(result);
    }

    @Test
    void testApply_NoMatch_ThrowsException() {
        toCcFilter = new ToCcFilter(".*@gmail\\.com.*");
        assertThrows(MatchingFailureException.class, () -> toCcFilter.apply(email));
    }

    @Test
    void testApply_NullRegex_ReturnsNull() throws MatchingFailureException {
        toCcFilter = new ToCcFilter(null);
        Email result = toCcFilter.apply(email);
        assertNull(result);
    }

    @Test
    void testApply_WithNextFilter_BothMatch_ReturnsEmail() throws MatchingFailureException {
        toCcFilter = new ToCcFilter(".*@example\\.com.*");
        toCcFilter.setNextFilter(nextFilter);
        when(nextFilter.apply(email)).thenReturn(email);
        Email result = toCcFilter.apply(email);
        assertNotNull(result);
        verify(nextFilter).apply(email);
    }

    @Test
    void testApply_WithNextFilter_NoMatch_ThrowsException() throws MatchingFailureException {
        toCcFilter = new ToCcFilter(".*@gmail\\.com.*");
        toCcFilter.setNextFilter(nextFilter);
        when(nextFilter.apply(email)).thenReturn(email);
        assertThrows(MatchingFailureException.class, () -> toCcFilter.apply(email));
    }
}

