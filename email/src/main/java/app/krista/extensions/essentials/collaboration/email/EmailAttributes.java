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

package app.krista.extensions.essentials.collaboration.email;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extension.util.InvokerAttributeProvider;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.essentials.collaboration.email.util.InvokerAttributeProviderImpl;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class EmailAttributes {

    public static final String IMAP_SERVER = "IMAP Server";
    public static final String SMTP_SERVER = "SMTP Server";
    public static final String EMAIL = "Email";
    public static final String PASSWORD = "Password";
    public static final String CLIENT_ID = "Client Id";
    public static final String CLIENT_SECRET = "Client Secret";
    public static final String TENANT_ID = "Tenant Id";
    private final InvokerAttributeProvider<String> imapServer;
    private final InvokerAttributeProvider<String> smtpServer;
    private final InvokerAttributeProvider<String> emailProvider;
    private final InvokerAttributeProvider<String> clientId;
    private final InvokerAttributeProvider<String> clientSecret;
    private final InvokerAttributeProvider<String> tenantId;
    private final Invoker invoker;

    @Inject
    public EmailAttributes(@Named(IMAP_SERVER) InvokerAttributeProvider<String> imapServer,
                           @Named(SMTP_SERVER) InvokerAttributeProvider<String> smtpServer,
                           @Named(EMAIL) InvokerAttributeProvider<String> emailProvider,
                           @Named(CLIENT_ID) InvokerAttributeProvider<String> clientId,
                           @Named(CLIENT_SECRET) InvokerAttributeProvider<String> clientSecret,
                           @Named(TENANT_ID) InvokerAttributeProvider<String> tenantId, Invoker invoker) {
        this.imapServer = imapServer;
        this.smtpServer = smtpServer;
        this.emailProvider = emailProvider;
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.clientSecret = clientSecret;
        this.invoker = invoker;
    }

    public EmailAttributes(Invoker invoker, Map<String, Object> attributes) {
        this(new InvokerAttributeProviderImpl<>(attributes, IMAP_SERVER),
                new InvokerAttributeProviderImpl<>(attributes, SMTP_SERVER),
                new InvokerAttributeProviderImpl<>(attributes, EMAIL),
                new InvokerAttributeProviderImpl<>(attributes, CLIENT_ID),
                new InvokerAttributeProviderImpl<>(attributes, CLIENT_SECRET),
                new InvokerAttributeProviderImpl<>(attributes, TENANT_ID), invoker);
    }

    public String getImapServer() {
        return imapServer.get();
    }

    public String getSmtpServer() {
        return smtpServer.get();
    }

    public String getEmail() {
        return emailProvider.get();
    }

    public String getClientId() {
        return clientId.get();
    }

    public String getTenantId() {
        return tenantId.get();
    }

    public String getClientSecret() {
        return clientSecret.get();
    }

    public void validateAttributes() throws IllegalArgumentException {
        String host = getImapServer();
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("IMAP Server hostname is required. Please provide a valid IMAP server address (e.g., imap.gmail.com).");
        }
        String user = getEmail();
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("Email address is required. Please provide the email account to connect to.");
        }

        if (!isEmailAddress(user)) {
            throw new IllegalArgumentException("Invalid email address format. Please provide a valid email address (e.g., user@example.com).");
        }
        String password = getClientSecret();
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Client Secret is required. Please provide the OAuth 2.0 Client Secret from your email provider's app configuration.");
        }
    }

    private boolean isEmailAddress(String userName) {
        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        return Pattern.compile(emailRegex)
                .matcher(userName)
                .matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailAttributes that = (EmailAttributes) o;
        return Objects.equals(imapServer.get(), that.imapServer.get())
                && Objects.equals(smtpServer.get(), that.smtpServer.get())
                && Objects.equals(emailProvider.get(), that.emailProvider.get())
                && Objects.equals(clientId.get(), that.clientId.get())
                && Objects.equals(clientSecret.get(), that.clientSecret.get())
                && Objects.equals(tenantId.get(), that.tenantId.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(imapServer, smtpServer, emailProvider, clientId, clientSecret, tenantId);
    }

    public String getRoutingURL() {
        return invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
    }

    @Override
    public String toString() {
        return Constants.GSON_JSON_MAPPER.toString(this);
    }
}
