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

package app.krista.extensions.essentials.collaboration.email.service.impl.authclients;

import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.listener.ConnectionListenerImpl;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.essentials.collaboration.email.service.impl.imap.IMAPStoreImpl;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.essentials.collaboration.email.util.Messages;
import app.krista.extensions.essentials.collaboration.email.util.OAuthUtil;
import app.krista.extensions.essentials.collaboration.email.util.Strings;
import app.krista.ksdk.context.ExecutionMode;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class OutlookAuthClient implements AuthClient {

    private static final Logger logger = LoggerFactory.getLogger(OutlookAuthClient.class);

    private OAuth20Service authService;
    private EmailAttributes emailAttributes;
    private final EmailAttributeStore emailAttributeStore;
    private final ExecutionMode executionMode;

    public OutlookAuthClient(EmailAttributes emailAttributes, EmailAttributeStore emailAttributeStore, ExecutionMode executionMode) {
        this.emailAttributes = emailAttributes;
        this.emailAttributeStore = emailAttributeStore;
        this.executionMode = executionMode;
    }

    /**
     * Creates and returns the OAuth 2.0 service for Outlook/Office 365 authentication.
     * Uses Microsoft Azure AD OAuth 2.0 with client credentials grant.
     *
     * @return Configured OAuth20Service instance for Outlook
     */
    @Override
    public OAuth20Service getAuthService() {
        EmailAttributes tempEmailAttributes = emailAttributeStore.getEmailAttributes();
        if (Objects.isNull(authService) || !Objects.equals(tempEmailAttributes, emailAttributes)) {
            if (!Objects.isNull(tempEmailAttributes)) {
                emailAttributes = tempEmailAttributes;
            }
            authService = new ServiceBuilder(emailAttributes.getClientId())
                    .apiSecret(emailAttributes.getClientSecret())
                    .defaultScope(Constants.OUTLOOK_DEFAULT_SCOPE)
                    .build(MicrosoftAzureActiveDirectory20Api.custom(emailAttributes.getTenantId()));
        }
        return authService;
    }

    /**
     * Retrieves a valid OAuth 2.0 access token for Outlook/Office 365 API access.
     * Uses client credentials grant flow (no refresh token needed).
     *
     * @return Valid access token string
     * @throws IllegalArgumentException if token retrieval fails
     */
    @Override
    public String getAccessToken() {
        try {
            OAuth2AccessToken accessToken = getAuthService().getAccessTokenClientCredentialsGrant();
            return accessToken.getAccessToken();
        } catch (IOException | InterruptedException | ExecutionException cause) {
            String message = Messages.SERVER_ERROR + Strings.SPACE + cause.getMessage();
            if (cause instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                logger.error((cause).getMessage());
            }
            throw new ServerErrorException(message, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public OAuth2AccessToken getRefreshToken(String code) {
        logger.debug("Inside outlook get access token using code");
        return null;
    }

    /**
     * Creates a Jakarta Mail Session configured for Outlook IMAP with OAuth 2.0 authentication.
     *
     * @return Configured Session instance for IMAP operations
     */
    @Override
    public synchronized Session getImapSession() {
        Properties props = getProperties();
        props.put("mail.imap.host", emailAttributes.getImapServer());
        props.put("mail.smtp.host", emailAttributes.getSmtpServer());
        return Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailAttributes.getClientId(), emailAttributes.getClientSecret());
            }
        });
    }

    /**
     * Creates a Jakarta Mail Session configured for Outlook SMTP with OAuth 2.0 authentication.
     *
     * @return Configured Session instance for SMTP operations
     */
    @Override
    public Session getSmtpSession() {
        OAuthUtil.removeCustomProvider();
        Properties props = getProperties();
        props.put("mail.imap.host", emailAttributes.getImapServer());
        props.put("mail.smtp.host", emailAttributes.getSmtpServer());
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                logger.debug("Getting access token to generate Outlook smtp session");
                return new PasswordAuthentication(emailAttributes.getEmail(), getAccessToken());
            }
        });
    }

    /**
     * Establishes and returns an authenticated IMAP Store connection to Outlook/Office 365.
     * Uses OAuth 2.0 XOAUTH2 SASL mechanism for authentication.
     *
     * @return Connected Store instance for IMAP operations
     * @throws IllegalArgumentException if connection fails
     */
    @Override
    public synchronized Store sessionConnect() {
        EmailAttributes tempEmailAttributes = emailAttributeStore.getEmailAttributes();
        if (!Objects.isNull(tempEmailAttributes)) {
            emailAttributes = tempEmailAttributes;
        }
        try {
            OAuthUtil.removeCustomProvider();
            IMAPStoreImpl imapSessionStore = new IMAPStoreImpl(getImapSession(), DEFAULT_PROTOCOL);
            logger.debug("Inside session connect, getting access token to connect to outlook imap store");
            imapSessionStore.connect(emailAttributes.getImapServer(), emailAttributes.getEmail(), getAccessToken());
            imapSessionStore.addConnectionListener(new ConnectionListenerImpl(emailAttributes, emailAttributeStore, executionMode));
            return imapSessionStore;
        } catch (MessagingException cause) {//NOSONAR
            logger.error("Error from server: {}", cause.getMessage());
            String message = "Error during setting up connection with " + emailAttributes.getImapServer() + ", Possible issues:\n" +
                    "1. Exchange username are not supported\n2. If not exchange username then Invalid Credentials, Authentication Failed";
            emailAttributeStore.removeAttributes();
            throw new IllegalStateException(message, cause);
        }
    }

    @Override
    public String getUser() {
        return emailAttributes.getEmail();
    }

    /**
     * Reconnects an existing IMAP Store with a fresh OAuth access token.
     * Used when the connection is lost or token expires.
     *
     * @param store Existing Store instance to reconnect
     * @throws IllegalArgumentException if reconnection fails
     */
    @Override
    public void reconnect(Store store) {
        try {
            logger.debug("Getting access token to reconnect outlook imap store");
            store.connect(emailAttributes.getImapServer(), emailAttributes.getEmail(), getAccessToken());
        } catch (MessagingException cause) {
            cause.printStackTrace();
            throw new IllegalStateException("Error while reconnecting store session: " + cause.getMessage());
        }
    }

    @Override
    public long getMaxAttachmentSize() {
        return Constants.MAX_ATTACHMENT_SIZE_SUPPORTED_BY_OUTLOOK_IN_BYTES;
    }
}
