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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;


/**
 * An OAuth2 implementation of SaslClient.
 */
class OAuth2SaslClient implements SaslClient {

    private final String oauthToken;
    private final CallbackHandler callbackHandler;

    private boolean isComplete = false;

    /**
     * Creates a new instance of the OAuth2SaslClient. This will ordinarily only
     * be called from OAuth2SaslClientFactory.
     */
    public OAuth2SaslClient(String oauthToken,
                            CallbackHandler callbackHandler) {
        this.oauthToken = oauthToken;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public String getMechanismName() {
        return Constants.XOAUTH2;
    }

    @Override
    public boolean hasInitialResponse() {
        return true;
    }

    @Override
    public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
        if (isComplete) {
            // Empty final response from server, just ignore it.
            return new byte[]{};
        }

        NameCallback nameCallback = new NameCallback("Enter name");
        Callback[] callbacks = new Callback[]{nameCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (UnsupportedCallbackException e) {
            throw new SaslException("Unsupported callback: " + e);
        } catch (IOException e) {
            throw new SaslException("Failed to execute callback: " + e);
        }
        String email = nameCallback.getName();

        byte[] response = String.format("user=%s\1auth=Bearer %s\1\1", email,
                oauthToken).getBytes();
        isComplete = true;
        return response;
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    @Override
    public byte[] unwrap(byte[] incoming, int offset, int len)
            throws SaslException {
        throw new IllegalStateException();
    }

    @Override
    public byte[] wrap(byte[] outgoing, int offset, int len)
            throws SaslException {
        throw new IllegalStateException();
    }

    @Override
    public Object getNegotiatedProperty(String propName) {
        if (!isComplete()) {
            throw new IllegalStateException();
        }
        return null;
    }

    @Override
    public void dispose() {//NOSONAR
        // Do nothing as no resources to clean up
    }
}
