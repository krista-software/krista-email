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

import app.krista.extensions.essentials.collaboration.email.util.Constants;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

/**
 * Class which defines the common methods which needs to be implemented by the child classes. Respective Auth client
 * give the implementation of same methods according to their support.
 */
public interface AuthClient {

    String DEFAULT_PROTOCOL = "imaps";

    /**
     * Returns instance of {@link OAuth20Service} class which is been used to generate OAuthUrl and to get Access token
     * from respective vendors.
     *
     * @return {@link OAuth20Service} class.
     */
    OAuth20Service getAuthService();

    /**
     * Returns the OAuth access token for respective vendor.
     *
     * @return {@link String} access token.
     */
    String getAccessToken();

    /**
     * Gets the {@link OAuth2AccessToken} using the code receive from the respective vendor.
     *
     * @param code {@link String} value used for getting {@link OAuth2AccessToken}.
     * @return {@link OAuth2AccessToken} instance.
     */
    OAuth2AccessToken getRefreshToken(String code);

    /**
     * {@link Session} object for respective vendor to connect to IMAP server.
     *
     * @return {@link Session} object to connect to SMTP server.
     */
    Session getImapSession();

    /**
     * {@link Session} object for respective vendor to connect to SMTP server.
     *
     * @return {@link Session} object to connect to IMAP server.
     */
    Session getSmtpSession();

    /**
     * Returns IMAP store object of respective vendor.
     *
     * @return {@link Store} IMAP store object.
     */
    Store sessionConnect();

    /**
     * Returns user email set in setup parameters.
     *
     * @return {@link String} setup email account.
     */
    String getUser();

    /**
     * Reconnect store object to respective vendor.
     *
     * @param store to be reconnected.
     */
    void reconnect(Store store);

    /**
     * Return the maximum attachment size supported by the email provider.
     *
     * @return {@link Integer} maximum attachment size in bytes.
     */
    long getMaxAttachmentSize();

    /**
     * Common method which returns commonly used property parameters.
     *
     * @return common {@link Properties} instance.
     */
    default Properties getProperties() {
        Properties prop = new Properties();
        prop.setProperty("mail.store.protocol", DEFAULT_PROTOCOL);
        prop.setProperty("mail.imaps.connectionpoolsize", "5");
        prop.setProperty("mail.imaps.statuscachetimeout", "300000");
        prop.put("mail.imaps.ssl.enable", "true");
        prop.put("mail.imaps.sasl.enable", "true");
        prop.put("mail.imaps.sasl.mechanisms", Constants.XOAUTH2);
        prop.put("mail.imap.auth.login.disable", "true");
        prop.put("mail.imap.auth.plain.disable", "true");
        prop.put("mail.smtp.sasl.enable", "true");
        prop.put("mail.smtp.sasl.mechanisms", Constants.XOAUTH2);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.auth.login.disable", "true");
        prop.put("mail.smtp.auth.plain.disable", "true");
        prop.put("mail.imap.port", "993");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.debug", "false");
        prop.put("mail.event.scope", "application");
        prop.put("mail.debug.auth", "true");
        return prop;
    }

    /**
     * Default method used to call sessionConnect of respective child auth client
     *
     * @return {@link Boolean} value to confirm successful call to this method.
     */
    default boolean testConnection() {
        sessionConnect();
        return true;
    }
}
