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

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.*;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.email.listener.InvokerLoader;
import app.krista.extensions.essentials.collaboration.email.service.EmailRequestAuthenticator;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.essentials.collaboration.email.service.impl.EmailAttributesChangeDetector;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClientFactory;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.essentials.collaboration.email.util.Strings;
import app.krista.extensions.util.KeyValueStore;
import app.krista.ksdk.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

@Java(version = Java.Version.JAVA_21)
@Field.Text(EmailAttributes.IMAP_SERVER)
@Field.Text(EmailAttributes.SMTP_SERVER)
@Field.Text(EmailAttributes.EMAIL)
@Field.Text(EmailAttributes.CLIENT_ID)
@Field.Text(value = EmailAttributes.TENANT_ID, required = false)
@Field.Text(value = EmailAttributes.CLIENT_SECRET, isSecured = true)
@Extension(version = "1.0.6" , name="Email")
@StaticResource(path = "docs", file = "docs")
public final class EmailExtension {

    private static final Logger logger = LoggerFactory.getLogger(EmailExtension.class);

    private final EmailAttributes emailAttributes;
    private final Invoker invoker;
    private final EmailAttributeStore emailAttributeStore;
    private final KeyValueStore keyValueStore;
    private final EmailAttributesChangeDetector emailAttributesChangeDetector;
    private final RequestContext requestContext;

    @Inject
    public EmailExtension(EmailAttributes emailAttributes, Invoker invoker,
                          EmailAttributeStore emailAttributeStore, KeyValueStore keyValueStore,
                          EmailAttributesChangeDetector emailAttributesChangeDetector, RequestContext requestContext) {
        this.emailAttributes = emailAttributes;
        this.invoker = invoker;
        this.emailAttributeStore = emailAttributeStore;
        this.keyValueStore = keyValueStore;
        this.emailAttributesChangeDetector = emailAttributesChangeDetector;
        this.requestContext = requestContext;
    }

    /**
     * Provides custom tabs for the extension UI.
     * Returns a map of tab names to their corresponding static resource paths.
     *
     * @return Map containing tab name "Documentation" mapped to "static/docs" path
     */
    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> getCustomTabs() {
        return Map.of("Documentation", "static/docs");
    }

    /**
     * Tests the OAuth 2.0 connection to the email provider (Gmail or Outlook).
     * Initiates the OAuth authorization flow and validates that the extension can successfully
     * authenticate with the configured email service using the provided credentials.
     *
     * @throws IllegalArgumentException if connection test fails due to invalid credentials,
     *                                  network issues, or OAuth configuration errors
     */
    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        try {
            logger.debug("Getting auth client in test connection workflow.");
            AuthClientFactory.getClient(emailAttributes, emailAttributeStore, requestContext.getExecutionMode()).testConnection();
        } catch (Exception cause) {
            throw new IllegalArgumentException("Failed to test connection. " + cause.getMessage(), cause);
        }

    }

    /**
     * Validates extension configuration attributes before saving.
     * Performs comprehensive validation including:
     * - IMAP and SMTP server hostname validation (Gmail or Outlook only)
     * - Vendor consistency check (cannot mix Gmail and Outlook servers)
     * - Tenant ID requirement for Outlook/Office 365
     * - OAuth connection test
     *
     * @param attributes Map of configuration attributes to validate
     * @throws IllegalArgumentException if validation fails with specific error messages:
     *                                  - "unsupported imap or smtp host" if servers are not Gmail or Outlook
     *                                  - "Invalid domains for imap and smtp protocol" if mixing Gmail/Outlook
     *                                  - "Tenant Id cannot be null or empty" if Outlook without Tenant ID
     *                                  - "Failed to test connection" if OAuth connection fails
     */
    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(Map<String, Object> attributes) {
        emailAttributeStore.put(attributes);
        emailAttributesChangeDetector.isChangeDetected(attributes, invoker.getAttributes());
        EmailAttributes setupAttributes = new EmailAttributes(invoker, attributes);
        String imapHostname = setupAttributes.getImapServer();
        String smtpHostname = setupAttributes.getSmtpServer();
        if (!(Constants.GMAIL_IMAP_SERVER.equalsIgnoreCase(imapHostname) || Constants.OUTLOOK_IMAP_SERVER.contains(imapHostname)
                || Constants.GMAIL_SMTP_SERVER.equalsIgnoreCase(smtpHostname) || Constants.OUTLOOK_SMTP_SERVER.equalsIgnoreCase(smtpHostname))) {
            throw new IllegalArgumentException("Unsupported email server. Only Gmail (imap.gmail.com / smtp.gmail.com) and Outlook (outlook.office365.com / smtp.office365.com) are supported.");
        }
        if ((Constants.GMAIL_IMAP_SERVER.equalsIgnoreCase(imapHostname) && Constants.OUTLOOK_SMTP_SERVER.equalsIgnoreCase(smtpHostname))
                || (Constants.OUTLOOK_IMAP_SERVER.contains(imapHostname) && Constants.GMAIL_SMTP_SERVER.equalsIgnoreCase(smtpHostname))) {
            throw new IllegalArgumentException("IMAP and SMTP servers must be from the same email provider. You cannot mix Gmail and Outlook servers.");
        }
        if (Constants.OUTLOOK_IMAP_SERVER.contains(imapHostname) && Strings.isNullOrBlank(setupAttributes.getTenantId())) {
            throw new IllegalArgumentException("Tenant ID is required for Outlook/Office 365 configuration. Please provide your Microsoft Tenant ID.");
        }
        logger.debug("Getting auth client to test connection from validate attributes workflow");
        AuthClientFactory.getClient(emailAttributes, emailAttributeStore, requestContext.getExecutionMode()).testConnection();
        logger.debug("Removing saved email attributes after successfully validating email attributes");
        emailAttributeStore.removeAttributes();
    }

    /**
     * Cleanup operations after invoker is loaded.
     * Removes temporary validation data and email attributes from storage
     * that were used during the configuration validation process.
     */
    @InvokerRequest(InvokerRequest.Type.INVOKER_LOADED)
    public void postInvokerLoad() {
        keyValueStore.remove(Constants.VALIDATE_REQUEST_KEY);
        emailAttributeStore.removeAttributes();
    }

    /**
     * Cleanup operations before invoker is unloaded.
     * Removes email attributes from storage and initiates asynchronous invoker reload
     * to ensure clean state for next execution.
     */
    @InvokerRequest(InvokerRequest.Type.INVOKER_UNLOADED)
    public void preInvokerUnload() {
        emailAttributeStore.removeAttributes();
        logger.debug("Invoker execution is going to be terminated: {}", invoker.getInvokerId());
        InvokerLoader invokerLoader = new InvokerLoader(invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE));
        Thread daemonThread = new Thread(invokerLoader);
        daemonThread.setName("Invoker Loader");
        daemonThread.start();
        try {
            Thread.sleep(2000);
            logger.info("Current invoker thread getting terminated, reloading invoker.");
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            logger.error("Interruption error occurred while current was in sleep: {}", cause.getMessage());
        }
    }

    /**
     * Provides the OAuth 2.0 request authenticator for catalog requests.
     * Creates an authenticator that handles OAuth token refresh and authentication
     * for all email operations (IMAP and SMTP).
     *
     * @return EmailRequestAuthenticator instance configured with current email attributes
     */
    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return new EmailRequestAuthenticator(emailAttributes, emailAttributeStore, requestContext.getExecutionMode());
    }
}
