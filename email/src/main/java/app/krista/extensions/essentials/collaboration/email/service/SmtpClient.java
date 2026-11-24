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

import javax.mail.Message;
import javax.mail.Session;

/**
 * Interface for an SMTP client to send email messages.
 * <p>
 * Provides methods to get the email session and send messages.
 */
public interface SmtpClient {

    /**
     * Gets the JavaMail Session used by this client.
     *
     * @return the Session object
     */
    Session getSession();

    /**
     * Sends the given email message using SMTP.
     *
     * @param mailMessage the Message object to send
     */
    void send(Message mailMessage);

    /**
     * Closes the SMTP client and releases any resources.
     * <p>
     * This method should be called when the client is no longer needed,
     * such as when configuration changes or the client is being discarded.
     * Implementations should close any persistent SMTP connections.
     */
    void close();

}
