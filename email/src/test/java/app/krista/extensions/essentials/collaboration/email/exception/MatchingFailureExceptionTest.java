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

package app.krista.extensions.essentials.collaboration.email.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchingFailureExceptionTest {

    @Test
    void defaultConstructor_createsException() {
        MatchingFailureException ex = new MatchingFailureException();
        assertNotNull(ex);
        assertNull(ex.getMessage());
    }

    @Test
    void messageConstructor_setsMessage() {
        MatchingFailureException ex = new MatchingFailureException("No match found");
        assertEquals("No match found", ex.getMessage());
    }

    @Test
    void messageConstructor_withEmptyString() {
        MatchingFailureException ex = new MatchingFailureException("");
        assertEquals("", ex.getMessage());
    }

    @Test
    void isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new MatchingFailureException());
    }

    @Test
    void canBeThrownAndCaught() {
        assertThrows(MatchingFailureException.class, () -> {
            throw new MatchingFailureException("test error");
        });
    }

    @Test
    void canBeCaughtAsRuntimeException() {
        assertThrows(RuntimeException.class, () -> {
            throw new MatchingFailureException("test");
        });
    }
}