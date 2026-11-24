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

package app.krista.extensions.essentials.collaboration.email.service.impl.client;

import app.krista.extensions.essentials.collaboration.email.service.SmtpClient;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.essentials.collaboration.email.util.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;

/**
 * SMTP client implementation that reuses Transport connections to avoid rate limiting.
 * Maintains a persistent SMTP connection and reconnects automatically if needed.
 */
public class SmtpClientImpl implements SmtpClient {

    private static final Logger logger = LoggerFactory.getLogger(SmtpClientImpl.class);
    private final Session session;
    private Transport transport;

    public SmtpClientImpl(Session session) {
        this.session = session;
    }

    @Override
    public Session getSession() {
        return session;
    }

    /**
     * Sends an email message using a persistent SMTP Transport connection.
     * Reuses the existing connection if available, or creates a new one if needed.
     * Automatically reconnects if the connection is closed.
     *
     * @param mailMessage the Message to send
     * @throws IllegalArgumentException if no recipients are specified
     * @throws ServerErrorException if sending fails
     */
    @Override
    public synchronized void send(Message mailMessage) {
        try {
            // Validate recipients before attempting to send
            Address[] recipients = mailMessage.getAllRecipients();
            if (recipients == null || recipients.length == 0) {
                throw new IllegalArgumentException("Cannot send email: no recipients specified. Please provide at least one recipient in To, Cc, or Bcc fields.");
            }

            ensureConnected();
            transport.sendMessage(mailMessage, recipients);
            logger.info("Mail sent successfully.");
        } catch (MessagingException cause) {
            // Connection may have been closed by server - try reconnecting once
            logger.warn("SMTP send failed, attempting reconnect: {}", cause.getMessage());
            try {
                closeTransport();
                ensureConnected();
                Address[] recipients = mailMessage.getAllRecipients();
                transport.sendMessage(mailMessage, recipients);
                logger.info("Mail sent successfully after reconnect.");
            } catch (MessagingException retryException) {
                String errorMessage = Messages.SERVER_ERROR + Constants.COLON + retryException.getMessage();
                logger.error("error message: {}", errorMessage);
                closeTransport();
                throw new ServerErrorException(retryException.getClass().getName() + "| " + errorMessage, Response.Status.INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * Ensures the SMTP Transport is connected. Creates and connects a new Transport if needed.
     *
     * @throws MessagingException if connection fails
     */
    private void ensureConnected() throws MessagingException {
        if (transport == null || !transport.isConnected()) {
            if (transport != null) {
                logger.debug("Transport disconnected, creating new connection");
            }
            transport = session.getTransport("smtp");
            transport.connect();
            logger.debug("SMTP Transport connected to {}", session.getProperty("mail.smtp.host"));
        }
    }

    /**
     * Closes the Transport connection if open.
     */
    private void closeTransport() {
        if (transport != null) {
            try {
                transport.close();
                logger.debug("SMTP Transport closed");
            } catch (MessagingException e) {
                logger.warn("Error closing transport: {}", e.getMessage());
            } finally {
                transport = null;
            }
        }
    }

    /**
     * Closes the persistent SMTP connection when the client is no longer needed.
     * Should be called when the SmtpClient is being discarded.
     */
    public void close() {
        closeTransport();
    }
}
