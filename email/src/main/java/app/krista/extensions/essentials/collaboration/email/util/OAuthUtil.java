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

package app.krista.extensions.essentials.collaboration.email.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Provider;
import java.security.Security;
import java.util.Objects;

/**
 * Utility class used to add or remove google OAuth provider into the java security parameter for connecting to IMAP
 * and SMTP host of respective providers. In case of {@code GmailAuthClient} it adds the custom security provider whereas
 * in case of other auth clients it checks if custom google OAuth provider is present. If present then remove it and uses
 * default security provider to connect to IMAP and SMTP hosts.
 */
public final class OAuthUtil {

    private static final Logger logger = LoggerFactory.getLogger(OAuthUtil.class);

    private OAuthUtil() {
        throw new IllegalStateException("Invalid access to OAuth");
    }

    /**
     * Custom Attribute class which helps in setting up custom security provider in case of Google OAuth.
     */
    private static final class OAuth2Provider extends Provider {
        private static final long serialVersionUID = 1L;

        public OAuth2Provider() {
            super(Constants.GOOGLE_OAUTH2_PROVIDER_NAME, "1.0", "Provides the XOAUTH2 SASL Mechanism");
            put("SaslClientFactory.XOAUTH2",
                    "app.krista.extensions.essentials.collaboration.email.service.impl.oauth.OAuth2SaslClientFactory");
        }
    }

    /**
     * Checks and helps in setting the custom OAuth provider into the {@code Security}.
     */
    public static void initializeCustomProvider() {
        logger.debug("Method call to initialize security parameter: {}", Constants.GOOGLE_OAUTH2_PROVIDER_NAME);
        if(Objects.isNull(Security.getProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME))) {
            logger.debug("Adding security parameter: {}", Constants.GOOGLE_OAUTH2_PROVIDER_NAME);
            Security.insertProviderAt(new OAuth2Provider(), 1);
        }
    }

    /**
     * Checks and helps in removing the custom OAuth provider from the {@code Security}.
     */
    public static void removeCustomProvider() {
        logger.debug("Method call to remove security parameter: {}", Constants.GOOGLE_OAUTH2_PROVIDER_NAME);
        if(!Objects.isNull(Security.getProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME))) {
            logger.debug("Removing security parameter: {}", Constants.GOOGLE_OAUTH2_PROVIDER_NAME);
            Security.removeProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME);
        }
    }
}
