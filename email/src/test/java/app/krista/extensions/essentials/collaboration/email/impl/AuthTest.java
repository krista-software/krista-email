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
import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.mock.KeyValueStoreMock;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClientFactory;
import app.krista.ksdk.context.ExecutionMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class AuthTest {
    private AuthClient authClient;
    private Invoker invoker;

    @Before
    public void setup() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(EmailAttributes.CLIENT_ID, "cdc47494-718e-4aeb-9be9-5b2140fc0fff");
        attributes.put(EmailAttributes.CLIENT_SECRET, "REDACTED_SECRET");
        attributes.put(EmailAttributes.TENANT_ID, "c4d5be5b-6f10-488d-b7db-c0676c352975");
        attributes.put(EmailAttributes.EMAIL, "ankush.garg@kristasoft.info");
        attributes.put(EmailAttributes.IMAP_SERVER, "outlook.office365.com");
        attributes.put(EmailAttributes.SMTP_SERVER, "smtp.office365.com");
        invoker = Mockito.mock(Invoker.class);
        EmailAttributeStore emailAttributeStore = new EmailAttributeStore(invoker, new KeyValueStoreMock());
        EmailAttributes emailAttributes = new EmailAttributes(invoker, attributes);
        authClient = AuthClientFactory.getClient(emailAttributes, emailAttributeStore, ExecutionMode.LIVE);
    }

    @Test
    public void testAuthClient() {
        Assert.assertNotNull(authClient);
    }

}
