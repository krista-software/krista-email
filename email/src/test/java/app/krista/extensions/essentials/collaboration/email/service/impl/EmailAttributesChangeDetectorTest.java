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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmailAttributesChangeDetectorTest {

    private EmailAttributesChangeDetector detector;

    @BeforeEach
    void setUp() {
        detector = new EmailAttributesChangeDetector();
    }

    @Test
    void isChangeDetected_whenAttributeValueChanges_setsAllThreeFlags() {
        Map<String, Object> current = new HashMap<>();
        current.put("imapServer", "imap.newserver.com");

        Map<String, Object> saved = new HashMap<>();
        saved.put("imapServer", "imap.oldserver.com");

        detector.isChangeDetected(current, saved);

        assertTrue(detector.isToUpdateFolderClient());
        assertTrue(detector.isToUpdateMessageClient());
        assertTrue(detector.isToUpdateSmtpClient());
    }

    @Test
    void isChangeDetected_whenAttributesUnchanged_leavesAllFlagsFalse() {
        Map<String, Object> current = new HashMap<>();
        current.put("imapServer", "imap.server.com");

        Map<String, Object> saved = new HashMap<>();
        saved.put("imapServer", "imap.server.com");

        detector.isChangeDetected(current, saved);

        assertFalse(detector.isToUpdateFolderClient());
        assertFalse(detector.isToUpdateMessageClient());
        assertFalse(detector.isToUpdateSmtpClient());
    }

    @Test
    void isChangeDetected_whenSavedHasKeyMissingInCurrent_setsAllFlags() {
        Map<String, Object> current = new HashMap<>();

        Map<String, Object> saved = new HashMap<>();
        saved.put("smtpServer", "smtp.server.com");

        detector.isChangeDetected(current, saved);

        assertTrue(detector.isToUpdateFolderClient());
        assertTrue(detector.isToUpdateMessageClient());
        assertTrue(detector.isToUpdateSmtpClient());
    }

    @Test
    void isChangeDetected_whenCurrentHasNullForExistingKey_setsAllFlags() {
        Map<String, Object> current = new HashMap<>();
        current.put("smtpServer", null);

        Map<String, Object> saved = new HashMap<>();
        saved.put("smtpServer", "smtp.server.com");

        detector.isChangeDetected(current, saved);

        assertTrue(detector.isToUpdateFolderClient());
        assertTrue(detector.isToUpdateMessageClient());
        assertTrue(detector.isToUpdateSmtpClient());
    }

    @Test
    void isChangeDetected_withMultipleChangedAttributes_setsAllFlags() {
        Map<String, Object> current = new HashMap<>();
        current.put("imapServer", "imap.new.com");
        current.put("smtpServer", "smtp.new.com");
        current.put("email", "new@example.com");

        Map<String, Object> saved = new HashMap<>();
        saved.put("imapServer", "imap.old.com");
        saved.put("smtpServer", "smtp.old.com");
        saved.put("email", "old@example.com");

        detector.isChangeDetected(current, saved);

        assertTrue(detector.isToUpdateFolderClient());
        assertTrue(detector.isToUpdateMessageClient());
        assertTrue(detector.isToUpdateSmtpClient());
    }

    @Test
    void isChangeDetected_whenSavedIsEmpty_leavesAllFlagsFalse() {
        Map<String, Object> current = new HashMap<>();
        current.put("imapServer", "imap.server.com");

        Map<String, Object> saved = new HashMap<>();

        detector.isChangeDetected(current, saved);

        assertFalse(detector.isToUpdateFolderClient());
        assertFalse(detector.isToUpdateMessageClient());
        assertFalse(detector.isToUpdateSmtpClient());
    }

    @Test
    void isToUpdateSmtpClient_defaultIsFalse() {
        assertFalse(detector.isToUpdateSmtpClient());
    }

    @Test
    void isToUpdateFolderClient_defaultIsFalse() {
        assertFalse(detector.isToUpdateFolderClient());
    }

    @Test
    void isToUpdateMessageClient_defaultIsFalse() {
        assertFalse(detector.isToUpdateMessageClient());
    }

    @Test
    void setToUpdateSmtpClient_true_returnsTrue() {
        detector.setToUpdateSmtpClient(true);
        assertTrue(detector.isToUpdateSmtpClient());
    }

    @Test
    void setToUpdateSmtpClient_false_returnsFalse() {
        detector.setToUpdateSmtpClient(true);
        detector.setToUpdateSmtpClient(false);
        assertFalse(detector.isToUpdateSmtpClient());
    }

    @Test
    void setToUpdateFolderClient_true_returnsTrue() {
        detector.setToUpdateFolderClient(true);
        assertTrue(detector.isToUpdateFolderClient());
    }

    @Test
    void setToUpdateFolderClient_false_returnsFalse() {
        detector.setToUpdateFolderClient(true);
        detector.setToUpdateFolderClient(false);
        assertFalse(detector.isToUpdateFolderClient());
    }

    @Test
    void setToUpdateMessageClient_true_returnsTrue() {
        detector.setToUpdateMessageClient(true);
        assertTrue(detector.isToUpdateMessageClient());
    }

    @Test
    void setToUpdateMessageClient_false_returnsFalse() {
        detector.setToUpdateMessageClient(true);
        detector.setToUpdateMessageClient(false);
        assertFalse(detector.isToUpdateMessageClient());
    }

    @Test
    void flagsCanBeResetIndependentlyAfterChangeDetected() {
        Map<String, Object> current = new HashMap<>();
        current.put("key", "newValue");

        Map<String, Object> saved = new HashMap<>();
        saved.put("key", "oldValue");

        detector.isChangeDetected(current, saved);

        assertTrue(detector.isToUpdateFolderClient());
        assertTrue(detector.isToUpdateMessageClient());
        assertTrue(detector.isToUpdateSmtpClient());

        detector.setToUpdateFolderClient(false);
        assertFalse(detector.isToUpdateFolderClient());
        assertTrue(detector.isToUpdateMessageClient());
        assertTrue(detector.isToUpdateSmtpClient());

        detector.setToUpdateSmtpClient(false);
        assertFalse(detector.isToUpdateSmtpClient());
        assertTrue(detector.isToUpdateMessageClient());

        detector.setToUpdateMessageClient(false);
        assertFalse(detector.isToUpdateMessageClient());
    }
}