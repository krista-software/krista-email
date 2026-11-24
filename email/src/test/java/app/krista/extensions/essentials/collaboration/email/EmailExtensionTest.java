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
import app.krista.extensions.essentials.collaboration.email.service.EmailRequestAuthenticator;
import app.krista.extensions.essentials.collaboration.email.service.impl.EmailAttributesChangeDetector;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.util.KeyValueStore;
import app.krista.ksdk.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailExtension covering simple methods.
 */
@ExtendWith(MockitoExtension.class)
class EmailExtensionTest {

    @Mock
    private EmailAttributes emailAttributes;

    @Mock
    private Invoker invoker;

    @Mock
    private EmailAttributeStore emailAttributeStore;

    @Mock
    private KeyValueStore keyValueStore;

    @Mock
    private EmailAttributesChangeDetector emailAttributesChangeDetector;

    @Mock
    private RequestContext requestContext;

    private EmailExtension emailExtension;

    @BeforeEach
    void setUp() {
        emailExtension = new EmailExtension(
                emailAttributes, invoker, emailAttributeStore,
                keyValueStore, emailAttributesChangeDetector, requestContext);
    }

    @Test
    void getCustomTabs_returnsDocumentationTab() {
        Map<String, String> tabs = emailExtension.getCustomTabs();
        assertNotNull(tabs);
        assertEquals("static/docs", tabs.get("Documentation"));
        assertEquals(1, tabs.size());
    }

    @Test
    void postInvokerLoad_removesValidateRequestKeyAndEmailAttributes() {
        emailExtension.postInvokerLoad();
        verify(keyValueStore).remove(anyString());
        verify(emailAttributeStore).removeAttributes();
    }

    @Test
    void getRequestAuthenticator_returnsEmailRequestAuthenticator() {
        when(requestContext.getExecutionMode()).thenReturn(app.krista.ksdk.context.ExecutionMode.TEST);
        RequestAuthenticator auth = emailExtension.getRequestAuthenticator();
        assertNotNull(auth);
        assertInstanceOf(EmailRequestAuthenticator.class, auth);
    }
}
