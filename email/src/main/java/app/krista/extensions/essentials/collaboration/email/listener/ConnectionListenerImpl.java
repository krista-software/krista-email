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

package app.krista.extensions.essentials.collaboration.email.listener;


import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClientFactory;
import app.krista.extensions.essentials.collaboration.email.service.impl.imap.IMAPStoreImpl;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.ksdk.context.ExecutionMode;
import com.sun.mail.imap.IMAPSSLStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.event.ConnectionEvent;
import java.util.Objects;

public class ConnectionListenerImpl implements javax.mail.event.ConnectionListener {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionListenerImpl.class);
    private final EmailAttributes emailAttributes;
    private final EmailAttributeStore emailAttributeStore;
    private final ExecutionMode executionMode;

    public ConnectionListenerImpl(EmailAttributes emailAttributes, EmailAttributeStore emailAttributeStore, ExecutionMode executionMode) {
        this.emailAttributes = emailAttributes;
        this.emailAttributeStore = emailAttributeStore;
        this.executionMode = executionMode;
    }

    @Override
    public void opened(ConnectionEvent connectionEvent) {
        logger.info("connection opened: {} and the source: {}", connectionEvent.getType(), connectionEvent.getSource());
    }

    @Override
    public void disconnected(ConnectionEvent connectionEvent) {
        logger.info("store has disconnected for {}", connectionEvent.getSource());
        reconnectStore(connectionEvent.getSource());
    }

    @Override
    public void closed(ConnectionEvent connectionEvent) {
        logger.info("connection closed for {}", connectionEvent.getSource());
        reconnectStore(connectionEvent.getSource());
    }

    private void reconnectStore(Object source) {
        if (!Objects.isNull(source) && source instanceof IMAPSSLStore) {
            IMAPStoreImpl store = (IMAPStoreImpl) source;
            try {
                store.connect(store.getHost(), store.getUser(), AuthClientFactory.getClient(emailAttributes,
                        emailAttributeStore, executionMode).getAccessToken());
            } catch (MessagingException cause) {
                logger.error("error while reestablishing connection: {}", cause.getMessage());
            }
            logger.info("connection reestablished for {}", store);
        }
    }

}

