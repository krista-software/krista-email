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

import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;

import javax.mail.Session;

/**
 * Interface for an email client.
 * <p>
 * Provides access to the various email clients and configuration.
 */
public interface EmailClient {

    /**
     * Gets the folder client for accessing email folders.
     *
     * @return The FolderClient
     */
    FolderClient getFolderClient();

    /**
     * Gets the IMAP client for managing messages.
     *
     * @return The MessageClient
     */
    MessageClient getImapClient();

    /**
     * Gets the SMTP client for sending messages.
     *
     * @return The SmtpClient
     */
    SmtpClient getSmtpClient();

    /**
     * Gets the shared JavaMail Session.
     *
     * @return The Session object
     */
    Session getSession();

    /**
     * Gets the email address of the current user.
     *
     * @return The user's email address
     */
    String me();

    /**
     * Gets the maximum allowed attachment size.
     *
     * @return The max size in bytes
     */
    long getMaxAttachmentSize();

    /**
     * Gets the email attribute store.
     *
     * @return The EmailAttributeStore
     */
    EmailAttributeStore getEmailStore();
}
