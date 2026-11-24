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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.listener.ConnectionListenerImpl;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.essentials.collaboration.email.service.impl.imap.IMAPStoreImpl;
import app.krista.extensions.essentials.collaboration.email.service.impl.oauth.OAuth2SaslClientFactory;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.essentials.collaboration.email.util.Messages;
import app.krista.extensions.essentials.collaboration.email.util.OAuthUtil;
import app.krista.extensions.essentials.collaboration.email.util.Strings;
import app.krista.ksdk.context.ExecutionMode;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class GmailAuthClient implements AuthClient {
    private static final Logger logger = LoggerFactory.getLogger(GmailAuthClient.class);
    private static final long TOKEN_CACHE_TTL_MILLIS = 30 * 60 * 1000L; // 30 minutes
    private static final Map<String, TokenCache> tokenCacheMap = new ConcurrentHashMap<>();
    private OAuth20Service authService;
    private EmailAttributes emailAttributes;
    private final EmailAttributeStore emailAttributeStore;
    private final ExecutionMode executionMode;

    public GmailAuthClient(EmailAttributes emailAttributes,
                           EmailAttributeStore emailAttributeStore, ExecutionMode executionMode) {
        this.emailAttributes = emailAttributes;
        this.emailAttributeStore = emailAttributeStore;
        this.executionMode = executionMode;
    }

    /**
     * Creates and returns the OAuth 2.0 service for Gmail authentication.
     * Uses ScribeJava library with Google OAuth 2.0 API.
     *
     * @return Configured OAuth20Service instance for Gmail
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
                    .defaultScope(Constants.GMAIL_DEFAULT_SCOPE)
                    .callback(getCallbackURL())
                    .build(GoogleApi20.instance());
        }
        return authService;
    }

    private String getCallbackURL() {
        return emailAttributes.getRoutingURL() + Constants.CALLBACK_URI;
    }

    /**
     * Retrieves a valid OAuth 2.0 access token for Gmail API access.
     * Caches the token in a map keyed by user email and reuses it if less than 30 minutes old
     * to avoid hitting Gmail's OAuth token endpoint rate limit (100 requests per 100 seconds per user).
     *
     * @return Valid access token string
     * @throws IllegalArgumentException if refresh token is missing or token refresh fails
     */
    @Override
    public synchronized String getAccessToken() {
        String userEmail = emailAttributes.getEmail();
        TokenCache cached = tokenCacheMap.get(userEmail);
        if (cached != null && (System.currentTimeMillis() - cached.timestamp) < TOKEN_CACHE_TTL_MILLIS) {

            logger.info("Reusing cached token for user: {}", userEmail);
            return cached.accessToken;
        }

        Object refreshToken = emailAttributeStore.getRefreshToken(userEmail);
        if (Strings.isObjectNullOrBlank(refreshToken)) {
            throw getMustAuthorizeException();
        }
        try {
            logger.info("Cache miss for user: {} — fetching new token from OAuth", userEmail);
            OAuth2AccessToken oAuth2AccessToken = getAuthService().refreshAccessToken(String.valueOf(refreshToken));
            String accessToken = oAuth2AccessToken.getAccessToken();
            tokenCacheMap.put(userEmail, new TokenCache(accessToken, System.currentTimeMillis()));
            logger.info("New token fetched and cached for user: {}", userEmail);
            return accessToken;
        } catch (IOException cause) {
            throw new IllegalStateException("Error while getting accessToken: " + cause.getMessage());
        } catch (Exception cause) {
            if (cause.getMessage().contains("invalid_grant")) {
                tokenCacheMap.remove(userEmail);
                emailAttributeStore.removeAccessToken(userEmail);
                throw getMustAuthorizeException();
            }
            logger.error("Error while getting access token: {}", cause.getMessage());
            Thread.currentThread().interrupt();
        }
        return null;
    }

    /**
     * Exchanges OAuth authorization code for access and refresh tokens.
     * Called during initial OAuth flow after user grants permissions.
     *
     * @param code Authorization code from OAuth callback
     * @return OAuth2AccessToken containing access token and refresh token
     * @throws IllegalArgumentException if code exchange fails
     */
    @Override
    public OAuth2AccessToken getRefreshToken(String code) {
        try {
            OAuth2AccessToken accessToken = getAuthService().getAccessToken(code);
            logger.debug("accessToken: {}", accessToken);
            logger.debug("refreshToken: {}", accessToken.getRefreshToken());
            return accessToken;
        } catch (IOException | InterruptedException | ExecutionException cause) {
            String message = Messages.SERVER_ERROR + Strings.SPACE + cause.getMessage();
            if (cause instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ServerErrorException(message, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a Jakarta Mail Session configured for Gmail IMAP with OAuth 2.0 authentication.
     *
     * @return Configured Session instance for IMAP operations
     */
    @Override
    public synchronized Session getImapSession() {
        Properties props = getProperties();
        props.put("mail.imap.host", emailAttributes.getImapServer());
        props.put("mail.smtp.host", emailAttributes.getSmtpServer());
        props.put("mail.imaps.ssl.trust", "*");
        props.put("mail.imaps.auth.login.disable", "true");
        logger.debug("getting access token to create gmail imap session.");
        props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, getAccessToken());
        return Session.getInstance(props);
    }

    /**
     * Creates a Jakarta Mail Session configured for Gmail SMTP with OAuth 2.0 authentication.
     *
     * @return Configured Session instance for SMTP operations
     */
    @Override
    public Session getSmtpSession() {
        OAuthUtil.initializeCustomProvider();
        Properties props = getProperties();
        props.put("mail.imap.host", emailAttributes.getImapServer());
        props.put("mail.smtp.host", emailAttributes.getSmtpServer());
        logger.debug("Getting Access token to create gmail smtp session");
        props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, getAccessToken());
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailAttributes.getEmail(), Constants.EMPTY_STRING);
            }
        });
    }

    /**
     * Establishes and returns an authenticated IMAP Store connection to Gmail.
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
            OAuthUtil.initializeCustomProvider();
            logger.debug("Inside session connect getting access token to connect to gmail imap store");
            String oAuthToken = getAccessToken();
            IMAPStoreImpl imapSessionStore = new IMAPStoreImpl(getImapSession(), DEFAULT_PROTOCOL);
            imapSessionStore.connect(emailAttributes.getImapServer(), Constants.IMAP_PORT, emailAttributes.getEmail(),
                    oAuthToken);
            imapSessionStore.addConnectionListener(new ConnectionListenerImpl(emailAttributes, emailAttributeStore, executionMode));
            return imapSessionStore;
        } catch (MessagingException cause) {//NOSONAR
            logger.error("Error while getting imap store object for user: {}", emailAttributes.getEmail());
            if (cause.getMessage().equals("[AUTHENTICATIONFAILED] Invalid credentials (Failure)")) {
                throw getMustAuthorizeException();
            }
            logger.error("Error from server: {}", cause.getMessage());
            String message = "Error during setting up connection with " +
                    emailAttributes.getImapServer() + ": Invalid Credentials, Authentication Failed";
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
            logger.debug("Getting access token to reconnect to gmail imap store");
            store.connect(emailAttributes.getImapServer(), emailAttributes.getEmail(), getAccessToken());
        } catch (MessagingException cause) {
            cause.printStackTrace();
            NamedValuedField usernameField = new NamedValuedField(EmailAttributes.EMAIL, Constants.TEXT,
                    emailAttributes.getEmail(), Map.of(), Map.of());
            throw new MustAuthorizeException("Error while reconnecting", List.of(usernameField));
        }
    }

    private MustAuthorizeException getMustAuthorizeException() {
        logger.debug("Null or Blank access token. Throwing MustAuthorization Exception");
        NamedValuedField usernameField = new NamedValuedField(EmailAttributes.EMAIL, Constants.TEXT,
                emailAttributes.getEmail(), Map.of(), Map.of());
        return new MustAuthorizeException("Error while fetching access token to make IMAP or SMTP connection",
                List.of(usernameField));
    }

    @Override
    public long getMaxAttachmentSize() {
        return Constants.MAX_ATTACHMENT_SIZE_SUPPORTED_BY_GMAIL_IN_BYTES;
    }

    /**
     * Holds a cached access token along with the timestamp when it was fetched.
     */
    private static class TokenCache {
        final String accessToken;
        final long timestamp;

        TokenCache(String accessToken, long timestamp) {
            this.accessToken = accessToken;
            this.timestamp = timestamp;
        }
    }
}
