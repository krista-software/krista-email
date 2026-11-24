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

package app.krista.extensions.essentials.collaboration.email.service;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClientFactory;
import app.krista.ksdk.context.ExecutionMode;
import app.krista.model.field.NamedField;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmailRequestAuthenticator implements RequestAuthenticator {
    private final EmailAttributes emailAttributes;
    private final EmailAttributeStore emailAttributeStore;
    private final ExecutionMode executionMode;

    public EmailRequestAuthenticator(EmailAttributes emailAttributes, EmailAttributeStore emailAttributeStore, ExecutionMode executionMode) {
        this.emailAttributes = emailAttributes;
        this.emailAttributeStore = emailAttributeStore;
        this.executionMode = executionMode;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public Set<String> getSupportedProtocols() {
        return Set.of();
    }

    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        return null;
    }

    @Override
    public boolean setServiceAuthorization(String s) {
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        return Map.of();
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException e, ProtoRequest protoRequest) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException e) {
        return null;
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException e, ProtoRequest protoRequest) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException e) {
        if (e.getDetails().isEmpty()) {
            return null;
        }
        String oAuthUrl = AuthClientFactory.getClient(emailAttributes, emailAttributeStore, executionMode)
                .getAuthService().getAuthorizationUrl() + "&access_type=offline";
        return new AuthorizationResponse(oAuthUrl, List.of());
    }
}
