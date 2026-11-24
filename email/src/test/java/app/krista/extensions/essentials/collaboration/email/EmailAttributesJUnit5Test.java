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

package app.krista.extensions.essentials.collaboration.email;

import app.krista.extension.executor.Invoker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for EmailAttributes.
 */
@ExtendWith(MockitoExtension.class)
class EmailAttributesJUnit5Test {

    @Mock
    private Invoker invoker;

    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        attributes.put(EmailAttributes.IMAP_SERVER, "imap.example.com");
        attributes.put(EmailAttributes.SMTP_SERVER, "smtp.example.com");
        attributes.put(EmailAttributes.EMAIL, "test@example.com");
        attributes.put(EmailAttributes.CLIENT_ID, "client-id-123");
        attributes.put(EmailAttributes.CLIENT_SECRET, "secret-abc");
        attributes.put(EmailAttributes.TENANT_ID, "tenant-xyz");
    }

    @Test
    void getImapServer_returnsCorrectValue() {
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertEquals("imap.example.com", ea.getImapServer());
    }

    @Test
    void getSmtpServer_returnsCorrectValue() {
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertEquals("smtp.example.com", ea.getSmtpServer());
    }

    @Test
    void getEmail_returnsCorrectValue() {
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertEquals("test@example.com", ea.getEmail());
    }

    @Test
    void getClientId_returnsCorrectValue() {
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertEquals("client-id-123", ea.getClientId());
    }

    @Test
    void getClientSecret_returnsCorrectValue() {
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertEquals("secret-abc", ea.getClientSecret());
    }

    @Test
    void getTenantId_returnsCorrectValue() {
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertEquals("tenant-xyz", ea.getTenantId());
    }

    @Test
    void validateAttributes_withValidAttributes_doesNotThrow() {
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertDoesNotThrow(ea::validateAttributes);
    }

    @Test
    void validateAttributes_missingImapServer_throwsIllegalArgumentException() {
        attributes.put(EmailAttributes.IMAP_SERVER, null);
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertThrows(IllegalArgumentException.class, ea::validateAttributes);
    }

    @Test
    void validateAttributes_emptyImapServer_throwsIllegalArgumentException() {
        attributes.put(EmailAttributes.IMAP_SERVER, "");
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertThrows(IllegalArgumentException.class, ea::validateAttributes);
    }

    @Test
    void validateAttributes_missingEmail_throwsIllegalArgumentException() {
        attributes.put(EmailAttributes.EMAIL, null);
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertThrows(IllegalArgumentException.class, ea::validateAttributes);
    }

    @Test
    void validateAttributes_invalidEmailFormat_throwsIllegalArgumentException() {
        attributes.put(EmailAttributes.EMAIL, "not-an-email");
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertThrows(IllegalArgumentException.class, ea::validateAttributes);
    }

    @Test
    void validateAttributes_missingClientSecret_throwsIllegalArgumentException() {
        attributes.put(EmailAttributes.CLIENT_SECRET, null);
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertThrows(IllegalArgumentException.class, ea::validateAttributes);
    }

    @Test
    void validateAttributes_emptyClientSecret_throwsIllegalArgumentException() {
        attributes.put(EmailAttributes.CLIENT_SECRET, "");
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertThrows(IllegalArgumentException.class, ea::validateAttributes);
    }

    @Test
    void equals_withSameAttributes_returnsTrue() {
        EmailAttributes ea1 = new EmailAttributes(invoker, attributes);
        Map<String, Object> attrs2 = new HashMap<>(attributes);
        EmailAttributes ea2 = new EmailAttributes(invoker, attrs2);
        assertEquals(ea1, ea2);
    }

    @Test
    void equals_withDifferentImapServer_returnsFalse() {
        EmailAttributes ea1 = new EmailAttributes(invoker, attributes);
        Map<String, Object> attrs2 = new HashMap<>(attributes);
        attrs2.put(EmailAttributes.IMAP_SERVER, "imap.other.com");
        EmailAttributes ea2 = new EmailAttributes(invoker, attrs2);
        assertNotEquals(ea1, ea2);
    }

    @Test
    void equals_withSelf_returnsTrue() {
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertEquals(ea, ea);
    }

    @Test
    void equals_withNull_returnsFalse() {
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertNotEquals(ea, null);
    }

    @Test
    void equals_withDifferentClass_returnsFalse() {
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertNotEquals(ea, "a string");
    }

    @Test
    void hashCode_isConsistent() {
        EmailAttributes ea = new EmailAttributes(invoker, attributes);
        assertEquals(ea.hashCode(), ea.hashCode());
    }


    @Test
    void constants_haveExpectedValues() {
        assertEquals("IMAP Server", EmailAttributes.IMAP_SERVER);
        assertEquals("SMTP Server", EmailAttributes.SMTP_SERVER);
        assertEquals("Email", EmailAttributes.EMAIL);
        assertEquals("Client Id", EmailAttributes.CLIENT_ID);
        assertEquals("Client Secret", EmailAttributes.CLIENT_SECRET);
        assertEquals("Tenant Id", EmailAttributes.TENANT_ID);
    }
}