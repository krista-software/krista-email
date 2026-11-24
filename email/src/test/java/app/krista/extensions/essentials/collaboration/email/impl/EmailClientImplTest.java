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

package app.krista.extensions.essentials.collaboration.email.impl;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.catalog.MessagingArea;
import app.krista.extensions.essentials.collaboration.email.mock.KeyValueStoreMock;
import app.krista.extensions.essentials.collaboration.email.service.EmailClient;
import app.krista.extensions.essentials.collaboration.email.service.Folder;
import app.krista.extensions.essentials.collaboration.email.service.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.email.service.SmtpClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.EmailAttributesChangeDetector;
import app.krista.extensions.essentials.collaboration.email.service.impl.client.EmailClientImpl;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.ksdk.context.ExecutionMode;
import app.krista.ksdk.context.RequestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmailClientImplTest {
    private EmailClient emailClient;
    private EmailAttributesChangeDetector attributesChangeDetector;
    private final static String USER_NAME = "ankush.garg@kristasoft.info";
    private final static String IMAP_SERVER = "outlook.office365.com";
    private final static String SMTP_SERVER = "smtp.office365.com";
    private KristaMediaClient kristaMediaClient;
    private EmailAttributes emailAttributes;
    private EmailAttributeStore emailAttributeStore;
    private KeyValueStoreMock keyValueStore;

    @Before
    public void setup() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(EmailAttributes.IMAP_SERVER, IMAP_SERVER);
        attributes.put(EmailAttributes.SMTP_SERVER, SMTP_SERVER);
        attributes.put(EmailAttributes.EMAIL, USER_NAME);
        attributes.put(EmailAttributes.CLIENT_ID, "cdc47494-718e-4aeb-9be9-5b2140fc0fff");
        attributes.put(EmailAttributes.CLIENT_SECRET, "REDACTED_SECRET");
        attributes.put(EmailAttributes.TENANT_ID, "c4d5be5b-6f10-488d-b7db-c0676c352975");

/*attributes.put(EmailAttributes.IMAP_SERVER, "imap.gmail.com"); //NOSONAR
        attributes.put(EmailAttributes.SMTP_SERVER, "smtp.gmail.com");//NOSONAR
        attributes.put(EmailAttributes.EMAIL, "core.engineering.test.krista@gmail.com");//NOSONAR
        attributes.put(EmailAttributes.CLIENT_ID, "149406009600-n1i2r48jnmh00gpbtqn9ip9ppa3kquab.apps.googleusercontent.com");//NOSONAR
        attributes.put(EmailAttributes.CLIENT_SECRET, "REDACTED_SECRET");*/
        //NOSONAR

        Invoker invoker = Mockito.mock(Invoker.class);
        Mockito.when(invoker.getRoutingInfo()).thenReturn(Mockito.mock(RoutingInfo.class));
        emailAttributes = new EmailAttributes(invoker, attributes);
        keyValueStore = new KeyValueStoreMock();
        emailAttributeStore = new EmailAttributeStore(invoker, keyValueStore);
        emailAttributeStore.put(attributes);
        //emailAttributeStore.saveAccessToken(emailAttributes.getEmail(), "REDACTED_SECRET");
        attributesChangeDetector = new EmailAttributesChangeDetector();
        RequestContext requestContext = Mockito.mock(RequestContext.class);
        Mockito.when(requestContext.getExecutionMode()).thenReturn(ExecutionMode.LIVE);
        emailClient = new EmailClientImpl(attributesChangeDetector, emailAttributes, emailAttributeStore, requestContext);
        this.kristaMediaClient = Mockito.mock(KristaMediaClient.class);
    }


    @Test
    public void testGetFolderByName() {
        Folder inbox = emailClient.getFolderClient().getFolder("inbox");
        Assert.assertNotNull(inbox);
    }

    @Test
    public void testGetSentFolder() {
        Folder sentFolder = emailClient.getFolderClient().getSentFolder();
        Assert.assertNotNull(sentFolder);
    }

    @Test
    public void testGetInboxFolder() {
        Folder inbox = emailClient.getFolderClient().getInboxFolder();
        Assert.assertNotNull(inbox);
    }

    @Test
    public void testGetFolder() {
        Folder inbox = emailClient.getFolderClient().getFolder("inbox");
        Assert.assertNotNull(inbox);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFolderByNameWhenFolderDoesNotExist() {
        emailClient.getFolderClient().getFolder("dasfew");
    }

    @Test
    public void testMessageMove() {
        MessagingArea area = new MessagingArea(emailClient, kristaMediaClient);
        List<String> msgIDs = new ArrayList<>();
        msgIDs.add("<");
        msgIDs.add(">");
        msgIDs.add("<>");
        msgIDs.add("<@>");
        msgIDs.add("@");
        msgIDs.add(".");
        msgIDs.add("<.");
        msgIDs.add("<.>");
        msgIDs.add("<*>");
        msgIDs.add("<$");
        msgIDs.add("#");
        msgIDs.add("$>");
        msgIDs.add("<,>");
        List<String> movedMessageIds = msgIDs.stream().map(msgId -> area.moveMessage(msgId, "Test")).collect(Collectors.toList());
        Assert.assertNotNull(movedMessageIds);
    }

    /**
     * Test that SmtpClient is cached and the same instance is returned on multiple calls.
     */
    @Test
    public void testSmtpClientCaching() {
        SmtpClient client1 = emailClient.getSmtpClient();
        SmtpClient client2 = emailClient.getSmtpClient();

        Assert.assertSame("SmtpClient should be cached and return same instance", client1, client2);
    }

    /**
     * Test that SmtpClient is recreated when configuration changes are detected.
     */
    @Test
    public void testSmtpClientRecreationOnConfigChange() {
        SmtpClient client1 = emailClient.getSmtpClient();

        // Simulate configuration change
        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(EmailAttributes.IMAP_SERVER, "imap.newserver.com");
        newAttributes.put(EmailAttributes.SMTP_SERVER, "smtp.newserver.com");

        Map<String, Object> oldAttributes = new HashMap<>();
        oldAttributes.put(EmailAttributes.IMAP_SERVER, IMAP_SERVER);
        oldAttributes.put(EmailAttributes.SMTP_SERVER, SMTP_SERVER);

        attributesChangeDetector.isChangeDetected(newAttributes, oldAttributes);

        SmtpClient client2 = emailClient.getSmtpClient();

        Assert.assertNotSame("SmtpClient should be recreated after config change", client1, client2);
    }

}
