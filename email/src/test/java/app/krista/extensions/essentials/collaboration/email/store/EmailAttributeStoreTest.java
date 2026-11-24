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

package app.krista.extensions.essentials.collaboration.email.store;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.email.mock.KeyValueStoreMock;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailAttributeStoreTest {

    @Mock
    private Invoker invoker;

    private KeyValueStore keyValueStore;
    private EmailAttributeStore store;

    @BeforeEach
    void setUp() {
        keyValueStore = mock(KeyValueStore.class);
        store = new EmailAttributeStore(invoker, keyValueStore);
    }

    @Test
    void put_storesAttributesAsJson() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("key", "value");
        store.put(attrs);
        verify(keyValueStore).put(eq(Constants.EMAIL_ATTRIBUTES_KEY), anyString());
    }

    @Test
    void saveAccessToken_storesTokenWithUserSuffix() {
        store.saveAccessToken("user@example.com", "refresh-token-123");
        verify(keyValueStore).put("user@example.com" + Constants.REFRESH_TOKEN_SUFFIX, "refresh-token-123");
    }

    @Test
    void getRefreshToken_returnsTokenFromStore() {
        when(keyValueStore.get("user@example.com" + Constants.REFRESH_TOKEN_SUFFIX))
                .thenReturn("my-refresh-token");
        Object token = store.getRefreshToken("user@example.com");
        assertEquals("my-refresh-token", token);
    }

    @Test
    void getRefreshToken_whenNotPresent_returnsNull() {
        when(keyValueStore.get(anyString())).thenReturn(null);
        Object token = store.getRefreshToken("nobody@example.com");
        assertNull(token);
    }

    @Test
    void removeAttributes_removesFromStore() {
        store.removeAttributes();
        verify(keyValueStore).remove(Constants.EMAIL_ATTRIBUTES_KEY);
    }

    @Test
    void removeAccessToken_removesWithUserSuffix() {
        store.removeAccessToken("user@example.com");
        verify(keyValueStore).remove("user@example.com" + Constants.REFRESH_TOKEN_SUFFIX);
    }

    @Test
    void saveLastFetchMailsRequestTime_storesTime() {
        long timeMs = 1700000000000L;
        store.saveLastFetchMailsRequestTime(timeMs);
        verify(keyValueStore).put(Constants.LAST_FETCH_MAILS_REQUEST_TIME_KEY, timeMs);
    }

    @Test
    void getLastFetchMailsRequestTime_whenStored_returnsParsedLong() {
        when(keyValueStore.get(Constants.LAST_FETCH_MAILS_REQUEST_TIME_KEY))
                .thenReturn("1700000000000");
        long result = store.getLastFetchMailsRequestTime();
        assertEquals(1700000000000L, result);
    }

    @Test
    void getLastFetchMailsRequestTime_whenNotStored_returnsCurrentTime() {
        when(keyValueStore.get(Constants.LAST_FETCH_MAILS_REQUEST_TIME_KEY)).thenReturn(null);
        long before = System.currentTimeMillis();
        long result = store.getLastFetchMailsRequestTime();
        long after = System.currentTimeMillis();
        assertTrue(result >= before && result <= after);
    }

    @Test
    void getEmailAttributes_whenNothing_returnsNull() {
        when(keyValueStore.get(Constants.EMAIL_ATTRIBUTES_KEY)).thenReturn("null");
        assertNull(store.getEmailAttributes());
    }
}