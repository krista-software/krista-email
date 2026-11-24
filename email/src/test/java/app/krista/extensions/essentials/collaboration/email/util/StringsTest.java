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

class StringsTest {

    @Test
    void isNullOrBlank_withNull_returnsTrue() {
        assertTrue(Strings.isNullOrBlank(null));
    }

    @Test
    void isNullOrBlank_withEmptyString_returnsTrue() {
        assertTrue(Strings.isNullOrBlank(""));
    }

    @Test
    void isNullOrBlank_withBlankString_returnsTrue() {
        assertTrue(Strings.isNullOrBlank("   "));
    }

    @Test
    void isNullOrBlank_withNonBlankString_returnsFalse() {
        assertFalse(Strings.isNullOrBlank("hello"));
    }

    @Test
    void isNullOrBlank_withSingleChar_returnsFalse() {
        assertFalse(Strings.isNullOrBlank("x"));
    }

    @Test
    void isObjectNullOrBlank_withNull_returnsTrue() {
        assertTrue(Strings.isObjectNullOrBlank(null));
    }

    @Test
    void isObjectNullOrBlank_withNonNullObject_returnsFalse() {
        assertFalse(Strings.isObjectNullOrBlank(42));
    }

    @Test
    void isObjectNullOrBlank_withStringObject_returnsFalse() {
        assertFalse(Strings.isObjectNullOrBlank("value"));
    }

    @Test
    void isObjectNullOrBlank_withBooleanTrue_returnsFalse() {
        assertFalse(Strings.isObjectNullOrBlank(Boolean.TRUE));
    }

    @Test
    void isEqual_withMatchingStrings_returnsTrue() {
        assertTrue(Strings.isEqual("hello", "hello"));
    }

    @Test
    void isEqual_withCaseDifference_returnsTrue() {
        assertTrue(Strings.isEqual("HELLO", "hello"));
        assertTrue(Strings.isEqual("hello", "HELLO"));
    }

    @Test
    void isEqual_withNonMatchingStrings_returnsFalse() {
        assertFalse(Strings.isEqual("hello", "world"));
    }

    @Test
    void isEqual_withNullFirst_returnsFalse() {
        assertFalse(Strings.isEqual(null, "hello"));
    }

    @Test
    void isEqual_withNullSecond_returnsFalse() {
        // obj1 is not null, equalsIgnoreCase(null) returns false
        assertFalse(Strings.isEqual("hello", null));
    }

    @Test
    void constants_haveExpectedValues() {
        assertEquals(" ", Strings.SPACE);
        assertEquals(";", Strings.SEMI_COLON);
        assertEquals("; ", Strings.SEMI_COLON_WITH_SPACE);
    }
}