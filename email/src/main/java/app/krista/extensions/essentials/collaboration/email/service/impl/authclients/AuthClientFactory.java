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
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.ksdk.context.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Factory class to return Auth client by determining the imap and smtp server attributes.
 */
public class AuthClientFactory {

    private static ExecutionMode currentExecutionMode = ExecutionMode.LIVE;

    private AuthClientFactory() {
        // private constructor to prevent instantiation of this class.
    }

    public static final Logger logger = LoggerFactory.getLogger(AuthClientFactory.class);
    private static AuthClient authClient;

    /**
     * Methods determines and returns the respective auth client as per the setup parameters.
     *
     * @return {@link AuthClient} instance of {@code OutlookAuthClient} if outlook imap server found as setup parameter
     * else returns the {@code GmailAuthClient} instance.
     */
    public static AuthClient getClient(EmailAttributes emailAttributes,
                                       EmailAttributeStore emailAttributeStore, ExecutionMode executionMode) {
        EmailAttributes tempEmailAttributes = emailAttributeStore.getEmailAttributes();
        if (Objects.isNull(authClient) || !Objects.equals(tempEmailAttributes, emailAttributes)
                || !currentExecutionMode.equals(executionMode)) {
            currentExecutionMode = executionMode;
            if (Constants.OUTLOOK_IMAP_SERVER.contains(getImapHost(emailAttributeStore, emailAttributes))) {
                logger.debug("Creating microsoft outlook auth client");
                authClient = new OutlookAuthClient(emailAttributes, emailAttributeStore, executionMode);
            } else {
                logger.debug("Creating google gmail auth client");
                authClient = new GmailAuthClient(emailAttributes, emailAttributeStore, executionMode);
            }
        }
        logger.debug("Return instance of {} log client",
                (authClient instanceof OutlookAuthClient) ? "microsoft outlook" : "google gmail");
        return authClient;
    }

    /**
     * Returns the imap server details if in case new attributes found in {@code EmailAttributeStore} else
     * returns the current imap host present in the current setup.
     *
     * @return {@link String} imap server host details.
     */
    private static String getImapHost(EmailAttributeStore emailAttributeStore, EmailAttributes emailAttributes) {
        EmailAttributes tempEmailAttributes = emailAttributeStore.getEmailAttributes();
        if (!Objects.isNull(tempEmailAttributes)) {
            return tempEmailAttributes.getImapServer();
        }
        return emailAttributes.getImapServer();
    }
}
