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

package app.krista.extensions.essentials.collaboration.email.catalog;

import app.krista.extension.executor.Invoker;
import app.krista.extension.util.ServiceRegistry;
import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.ServiceRegistryProvider;
import app.krista.extensions.essentials.collaboration.email.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.email.mock.KeyValueStoreMock;
import app.krista.extensions.essentials.collaboration.email.service.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.EmailAttributesChangeDetector;
import app.krista.extensions.essentials.collaboration.email.service.impl.client.EmailClientImpl;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.ksdk.context.RequestContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImapAreaTest {

    private ImapArea imapArea;
    private final static String USER_NAME = "ankush.garg@kristasoft.info";
    private final static String IMAP_SERVER = "outlook.office365.com";
    private final static String SMTP_SERVER = "smtp.office365.com";

    @Before
    public void setup() throws MessagingException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(EmailAttributes.IMAP_SERVER, IMAP_SERVER);
        attributes.put(EmailAttributes.SMTP_SERVER, SMTP_SERVER);
        attributes.put(EmailAttributes.EMAIL, USER_NAME);
        attributes.put(EmailAttributes.CLIENT_ID, "cdc47494-718e-4aeb-9be9-5b2140fc0fff");
        attributes.put(EmailAttributes.CLIENT_SECRET, "REDACTED_SECRET");
        attributes.put(EmailAttributes.TENANT_ID, "c4d5be5b-6f10-488d-b7db-c0676c352975");
        Invoker invoker = Mockito.mock(Invoker.class);
        EmailAttributes emailAttributes = new EmailAttributes(invoker, attributes);
        ServiceRegistry serviceRegistry = ServiceRegistryProvider.getServiceRegistry();
        KristaMediaClient kristaMediaClient = serviceRegistry.lookupService(KristaMediaClient.class);
        EmailAttributeStore emailAttributeStore = new EmailAttributeStore(invoker, new KeyValueStoreMock());
        ServiceLocator invokerServiceLocator = ServiceLocatorFactory.getInstance().create(null);
        RequestContext requestContext = serviceRegistry.lookupService(RequestContext.class);
        ServiceLocatorUtilities.addOneConstant(invokerServiceLocator, requestContext, null, RequestContext.class);
        imapArea = new ImapArea(new EmailClientImpl(new EmailAttributesChangeDetector(), emailAttributes, emailAttributeStore, requestContext), kristaMediaClient);
    }

    @Test
    public void testRetrieveUnreadEmailsFromFolder() {
        List<MailDetails> imapMailDetailsList = imapArea.retrieveUnreadEmailsFromFolder("inbox", 1.0, 1.0);
        Assert.assertNotNull(imapMailDetailsList);
    }
}
