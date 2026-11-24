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

import org.junit.jupiter.api.Test;

import javax.mail.Session;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SMTPMessageImpl.
 * Verifies that the constructor works and that updateMessageID() is a no-op
 * (message ID generation is intentionally delegated to the SMTP server).
 */
class SMTPMessageImplTest {

    @Test
    void constructor_createsInstance() {
        Session session = Session.getInstance(new Properties());
        SMTPMessageImpl msg = new SMTPMessageImpl(session);
        assertNotNull(msg);
    }

    @Test
    void updateMessageID_doesNotThrow() {
        // SMTPMessageImpl overrides updateMessageID() to do nothing so that
        // the SMTP server assigns the Message-ID header. Calling send() internally
        // invokes updateMessageID() — here we verify the override does not throw.
        Session session = Session.getInstance(new Properties());
        SMTPMessageImpl msg = new SMTPMessageImpl(session);
        assertDoesNotThrow(() -> {
            // Invoke via reflection since updateMessageID() is protected
            java.lang.reflect.Method method =
                    SMTPMessageImpl.class.getDeclaredMethod("updateMessageID");
            method.setAccessible(true);
            method.invoke(msg);
        });
    }

    @Test
    void updateMessageID_doesNotSetMessageIdHeader() throws Exception {
        // After calling updateMessageID(), no Message-ID header should be set
        // because the override is intentionally empty.
        Session session = Session.getInstance(new Properties());
        SMTPMessageImpl msg = new SMTPMessageImpl(session);

        java.lang.reflect.Method method =
                SMTPMessageImpl.class.getDeclaredMethod("updateMessageID");
        method.setAccessible(true);
        method.invoke(msg);

        String[] headers = msg.getHeader("Message-ID");
        assertNull(headers, "updateMessageID() override must not set Message-ID header");
    }
}