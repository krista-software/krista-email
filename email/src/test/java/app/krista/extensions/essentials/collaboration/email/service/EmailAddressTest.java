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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailAddressTest {

    @Test
    void getName_returnsName() {
        EmailAddress addr = new EmailAddress("John Doe", "john@example.com");
        assertEquals("John Doe", addr.getName());
    }

    @Test
    void getAddress_returnsAddress() {
        EmailAddress addr = new EmailAddress("John Doe", "john@example.com");
        assertEquals("john@example.com", addr.getAddress());
    }

    @Test
    void withNullName_getNameReturnsNull() {
        EmailAddress addr = new EmailAddress(null, "john@example.com");
        assertNull(addr.getName());
    }

    @Test
    void withNullAddress_getAddressReturnsNull() {
        EmailAddress addr = new EmailAddress("John", null);
        assertNull(addr.getAddress());
    }

    @Test
    void twoAddresses_withSameData_areIndependent() {
        EmailAddress addr1 = new EmailAddress("Alice", "alice@example.com");
        EmailAddress addr2 = new EmailAddress("Alice", "alice@example.com");
        assertEquals(addr1.getName(), addr2.getName());
        assertEquals(addr1.getAddress(), addr2.getAddress());
    }
}