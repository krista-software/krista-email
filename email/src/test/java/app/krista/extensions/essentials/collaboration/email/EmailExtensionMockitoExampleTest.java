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
import app.krista.extensions.essentials.collaboration.email.service.impl.EmailAttributesChangeDetector;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.util.KeyValueStore;
import app.krista.ksdk.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Example test class demonstrating how to use Mockito to mock dependencies
 * instead of creating manual mock implementations.
 *
 * This approach is preferred because:
 * 1. Mockito automatically handles new interface methods
 * 2. No need to maintain manual mock classes
 * 3. More flexible and easier to configure per test
 * 4. Better IDE support and type safety
 */
@ExtendWith(MockitoExtension.class)
class EmailExtensionMockitoExampleTest {

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
        // Initialize the extension with mocked dependencies
        emailExtension = new EmailExtension(
            emailAttributes,
            invoker,
            emailAttributeStore,
            keyValueStore,
            emailAttributesChangeDetector,
            requestContext
        );

        // Configure default mock behaviors if needed (lenient to avoid unnecessary stubbing errors)
        lenient().when(invoker.getInvokerId()).thenReturn("test-invoker-id");
        lenient().when(invoker.getWorkspaceId()).thenReturn("test-workspace-id");
    }

    @Test
    void testGetCustomTabs_ReturnsDocumentationTab() {
        // Act
        Map<String, String> customTabs = emailExtension.getCustomTabs();

        // Assert
        assertNotNull(customTabs);
        assertEquals(1, customTabs.size());
        assertTrue(customTabs.containsKey("Documentation"));
        assertEquals("static/docs", customTabs.get("Documentation"));
    }

    /**
     * Example of how to mock KeyValueStore for testing storage operations
     */
    @Test
    void testKeyValueStoreMocking() {
        // Arrange
        String key = "test-key";
        String value = "test-value";

        // Configure mock behavior
        when(keyValueStore.get(key)).thenReturn(value);
        doNothing().when(keyValueStore).put(key, value);

        // Act
        keyValueStore.put(key, value);
        Object retrievedValue = keyValueStore.get(key);

        // Assert
        assertEquals(value, retrievedValue);

        // Verify interactions
        verify(keyValueStore, times(1)).put(key, value);
        verify(keyValueStore, times(1)).get(key);
    }

    /**
     * Example of how to mock Invoker for testing catalog requests
     */
    @Test
    void testInvokerMocking() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@gmail.com");
        attributes.put("clientId", "test-client-id");
        
        // Configure mock behavior
        when(invoker.getAttributes()).thenReturn(attributes);
        when(invoker.getInvokerId()).thenReturn("test-invoker-123");

        // Act
        Map<String, Object> retrievedAttributes = invoker.getAttributes();
        String invokerId = invoker.getInvokerId();

        // Assert
        assertNotNull(retrievedAttributes);
        assertEquals("test@gmail.com", retrievedAttributes.get("email"));
        assertEquals("test-invoker-123", invokerId);
        
        // Verify interactions
        verify(invoker, times(1)).getAttributes();
        verify(invoker, times(1)).getInvokerId();
    }

    /**
     * Example of how to use argument matchers with Mockito
     */
    @Test
    void testArgumentMatchers() {
        // Arrange - use any() matcher for flexible matching
        when(keyValueStore.get(anyString())).thenReturn("default-value");

        // Act
        Object value1 = keyValueStore.get("key1");
        Object value2 = keyValueStore.get("key2");

        // Assert
        assertEquals("default-value", value1);
        assertEquals("default-value", value2);

        // Verify with argument matchers
        verify(keyValueStore, times(2)).get(anyString());
        verify(keyValueStore).get(eq("key1"));
        verify(keyValueStore).get(eq("key2"));
    }

    /**
     * Example of how to verify no interactions occurred
     */
    @Test
    void testVerifyNoInteractions() {
        // This test verifies that no methods were called on the mock
        verifyNoInteractions(keyValueStore);
        verifyNoInteractions(invoker);
    }

    /**
     * Example of how to use doThrow for testing exception scenarios
     */
    @Test
    void testExceptionMocking() {
        // Arrange
        String key = "error-key";
        doThrow(new RuntimeException("Storage error"))
            .when(keyValueStore).put(eq(key), anyString());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            keyValueStore.put(key, "value");
        });
        
        verify(keyValueStore).put(key, "value");
    }
}

