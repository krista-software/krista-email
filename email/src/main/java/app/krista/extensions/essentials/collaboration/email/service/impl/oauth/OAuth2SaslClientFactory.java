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

package app.krista.extensions.essentials.collaboration.email.service.impl.oauth;

import app.krista.extensions.essentials.collaboration.email.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;

/**
 * A SaslClientFactory that returns instances of OAuth2SaslClient.
 *
 * <p>Only the "XOAUTH2" mechanism is supported. The {@code callbackHandler} is
 * passed to the OAuth2SaslClient. Other parameters are ignored.
 */
public class OAuth2SaslClientFactory implements SaslClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2SaslClientFactory.class);

    public static final String OAUTH_TOKEN_PROP = "mail.imaps.sasl.mechanisms.oauth2.oauthToken";

    @Override
    public SaslClient createSaslClient(String[] mechanisms,
                                       String authorizationId,
                                       String protocol,
                                       String serverName,
                                       Map<String, ?> props,
                                       CallbackHandler callbackHandler) {
        boolean matchedMechanism = false;
        for (int i = 0; i < mechanisms.length; ++i) {
            if (Constants.XOAUTH2.equalsIgnoreCase(mechanisms[i])) {
                matchedMechanism = true;
                break;
            }
        }
        if (!matchedMechanism) {
            logger.debug("Failed to match any mechanisms");
            return null;
        }
        return new OAuth2SaslClient((String) props.get(OAUTH_TOKEN_PROP),
                callbackHandler);
    }

    @Override
    public String[] getMechanismNames(Map<String, ?> props) {
        return new String[]{Constants.XOAUTH2};
    }
}
