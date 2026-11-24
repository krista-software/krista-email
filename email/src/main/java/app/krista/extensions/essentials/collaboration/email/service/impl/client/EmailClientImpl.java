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

import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.service.EmailClient;
import app.krista.extensions.essentials.collaboration.email.service.FolderClient;
import app.krista.extensions.essentials.collaboration.email.service.MessageClient;
import app.krista.extensions.essentials.collaboration.email.service.SmtpClient;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.essentials.collaboration.email.service.impl.EmailAttributesChangeDetector;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClientFactory;
import app.krista.ksdk.context.RequestContext;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.Session;

@ContractsProvided(EmailClient.class)
@Service
public class EmailClientImpl implements EmailClient {
    private static final Logger logger = LoggerFactory.getLogger(EmailClientImpl.class);
    private FolderClient folderClient;
    private MessageClient messageClient;
    private SmtpClient smtpClient;
    private String lastSmtpToken;
    private final EmailAttributesChangeDetector attributesChangeDetector;
    private final EmailAttributes emailAttributes;
    private final EmailAttributeStore emailAttributeStore;
    private final RequestContext requestContext;

    @Inject
    public EmailClientImpl(EmailAttributesChangeDetector attributesChangeDetector, EmailAttributes emailAttributes, EmailAttributeStore emailAttributeStore, RequestContext requestContext) {
        this.attributesChangeDetector = attributesChangeDetector;
        this.emailAttributes = emailAttributes;
        this.emailAttributeStore = emailAttributeStore;
        this.requestContext = requestContext;
    }

    @Override
    public FolderClient getFolderClient() {
        boolean changeDetected = attributesChangeDetector.isToUpdateFolderClient();
        logger.debug("attributes change detected: {}", changeDetected);
        if (folderClient == null || changeDetected) {
            if (changeDetected) {
                attributesChangeDetector.setToUpdateFolderClient(false);
            }
            AuthClient client = AuthClientFactory.getClient(emailAttributes, emailAttributeStore, requestContext.getExecutionMode());
            folderClient = new FolderClientImpl(client);
        }
        return folderClient;
    }

    @Override
    public MessageClient getImapClient() {
        boolean changeDetected = attributesChangeDetector.isToUpdateMessageClient();
        logger.debug("attributes change detected: {}", changeDetected);
        if (messageClient == null || changeDetected) {
            if (changeDetected) {
                attributesChangeDetector.setToUpdateMessageClient(false);
            }
            if (messageClient != null) {
                MessageClientImpl.resetSchedulerExecutorService();
            }
            AuthClient client = AuthClientFactory.getClient(emailAttributes, emailAttributeStore, requestContext.getExecutionMode());
            messageClient = new MessageClientImpl(client);
        }
        return messageClient;
    }

    @Override
    public synchronized SmtpClient getSmtpClient() {
        AuthClient client = AuthClientFactory.getClient(emailAttributes, emailAttributeStore, requestContext.getExecutionMode());
        String currentToken = client.getAccessToken();

        // Recreate SmtpClient if: (1) not yet created, (2) config changed, or (3) token refreshed
        boolean tokenChanged = hasTokenChanged(currentToken);
        if (smtpClient == null || attributesChangeDetector.isToUpdateSmtpClient() || tokenChanged) {
            if (smtpClient != null) {
                smtpClient.close();
                String reason = tokenChanged ? "token refresh" : "configuration change";
                logger.debug("Closed existing SMTP client due to {}", reason);
                attributesChangeDetector.setToUpdateSmtpClient(false);
            }
            smtpClient = new SmtpClientImpl(client.getSmtpSession());
            lastSmtpToken = currentToken;
            logger.debug("Created new SMTP client with fresh session");
        }
        return smtpClient;
    }

    /**
     * Checks if the OAuth access token has changed since the last SmtpClient creation.
     * This is critical for Gmail OAuth where tokens expire after 30 minutes.
     *
     * @param currentToken The current access token from AuthClient
     * @return true if token has changed, false otherwise
     */
    private boolean hasTokenChanged(String currentToken) {
        if (lastSmtpToken == null) {
            return false; // First time, no previous token to compare
        }
        boolean changed = !lastSmtpToken.equals(currentToken);
        if (changed) {
            logger.info("OAuth token has been refreshed, will recreate SMTP client");
        }
        return changed;
    }

    @Override
    public Session getSession() {
        return AuthClientFactory.getClient(emailAttributes, emailAttributeStore, requestContext.getExecutionMode()).getImapSession();
    }

    @Override
    public String me() {
        return AuthClientFactory.getClient(emailAttributes, emailAttributeStore, requestContext.getExecutionMode()).getUser();
    }

    @Override
    public long getMaxAttachmentSize() {
        return AuthClientFactory.getClient(emailAttributes, emailAttributeStore, requestContext.getExecutionMode()).getMaxAttachmentSize();
    }

    @Override
    public EmailAttributeStore getEmailStore() {
        return emailAttributeStore;
    }
}
