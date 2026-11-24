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

package app.krista.extensions.essentials.collaboration.email.service.impl.authclients;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.ksdk.context.ExecutionMode;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Real-world scenario tests for GmailAuthClient token caching.
 * Validates that OAuth access tokens are cached in a ConcurrentHashMap
 * to prevent Gmail rate limiting (100 requests per 100 seconds per user).
 */
@ExtendWith(MockitoExtension.class)
class GmailAuthClientTest {

    private static final String USER_EMAIL = "user@gmail.com";
    private static final String REFRESH_TOKEN = "valid-refresh-token";
    private static final String ACCESS_TOKEN = "ya29.access-token-abc123";

    @Mock
    private EmailAttributes emailAttributes;

    @Mock
    private EmailAttributeStore emailAttributeStore;

    @Mock
    private OAuth20Service oAuth20Service;

    private GmailAuthClient gmailAuthClient;

    @BeforeEach
    void setUp() throws Exception {
        clearTokenCacheMap();
        lenient().when(emailAttributes.getEmail()).thenReturn(USER_EMAIL);
        gmailAuthClient = spy(new GmailAuthClient(emailAttributes, emailAttributeStore, ExecutionMode.LIVE));
        lenient().doReturn(oAuth20Service).when(gmailAuthClient).getAuthService();
    }

    /**
     * Scenario: User sends first email — no cached token exists.
     * Expected: OAuth service is called to fetch a new token.
     */
    @Test
    void getAccessToken_firstCall_fetchesNewTokenFromOAuthService() throws Exception {
        // Arrange
        when(emailAttributeStore.getRefreshToken(USER_EMAIL)).thenReturn(REFRESH_TOKEN);
        OAuth2AccessToken oAuth2AccessToken = mock(OAuth2AccessToken.class);
        when(oAuth2AccessToken.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(oAuth20Service.refreshAccessToken(REFRESH_TOKEN)).thenReturn(oAuth2AccessToken);

        // Act
        String result = gmailAuthClient.getAccessToken();

        // Assert
        assertEquals(ACCESS_TOKEN, result);
        verify(oAuth20Service, times(1)).refreshAccessToken(REFRESH_TOKEN);
    }

    /**
     * Scenario: User sends 50 emails in a loop within 30 minutes.
     * Expected: Only the first call hits OAuth service; subsequent calls reuse cached token.
     */
    @Test
    void getAccessToken_multipleCalls_reusesCachedToken() throws Exception {
        // Arrange
        when(emailAttributeStore.getRefreshToken(USER_EMAIL)).thenReturn(REFRESH_TOKEN);
        OAuth2AccessToken oAuth2AccessToken = mock(OAuth2AccessToken.class);
        when(oAuth2AccessToken.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(oAuth20Service.refreshAccessToken(REFRESH_TOKEN)).thenReturn(oAuth2AccessToken);

        // Act — simulate sending 10 emails rapidly
        for (int i = 0; i < 10; i++) {
            String result = gmailAuthClient.getAccessToken();
            assertEquals(ACCESS_TOKEN, result);
        }

        // Assert — OAuth service called only ONCE, not 10 times
        verify(oAuth20Service, times(1)).refreshAccessToken(REFRESH_TOKEN);
    }

    /**
     * Scenario: Token was cached 31 minutes ago — cache has expired.
     * Expected: A new token is fetched from OAuth service.
     */
    @Test
    void getAccessToken_cacheExpired_fetchesNewToken() throws Exception {
        // Arrange — pre-populate cache with an expired entry
        long expiredTimestamp = System.currentTimeMillis() - (31 * 60 * 1000L);
        putTokenInCache(USER_EMAIL, "old-expired-token", expiredTimestamp);

        when(emailAttributeStore.getRefreshToken(USER_EMAIL)).thenReturn(REFRESH_TOKEN);
        OAuth2AccessToken oAuth2AccessToken = mock(OAuth2AccessToken.class);
        when(oAuth2AccessToken.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(oAuth20Service.refreshAccessToken(REFRESH_TOKEN)).thenReturn(oAuth2AccessToken);

        // Act
        String result = gmailAuthClient.getAccessToken();

        // Assert — new token fetched, not the expired one
        assertEquals(ACCESS_TOKEN, result);
        verify(oAuth20Service, times(1)).refreshAccessToken(REFRESH_TOKEN);
    }

    /**
     * Scenario: OAuth returns invalid_grant (user revoked access).
     * Expected: Cache is cleared and MustAuthorizeException is thrown.
     */
    @Test
    void getAccessToken_invalidGrant_clearsCacheAndThrowsMustAuthorize() throws Exception {
        // Arrange — pre-populate cache with a token
        putTokenInCache(USER_EMAIL, ACCESS_TOKEN, System.currentTimeMillis() - (31 * 60 * 1000L));

        when(emailAttributeStore.getRefreshToken(USER_EMAIL)).thenReturn(REFRESH_TOKEN);
        when(oAuth20Service.refreshAccessToken(REFRESH_TOKEN))
                .thenThrow(new ExecutionException("invalid_grant", new RuntimeException()));

        // Act & Assert
        assertThrows(MustAuthorizeException.class, () -> gmailAuthClient.getAccessToken());
        verify(emailAttributeStore).removeAccessToken(USER_EMAIL);
        assertNull(getTokenCacheMap().get(USER_EMAIL));
    }

    /**
     * Scenario: User has not authorized yet — no refresh token stored.
     * Expected: MustAuthorizeException is thrown immediately.
     */
    @Test
    void getAccessToken_noRefreshToken_throwsMustAuthorize() {
        // Arrange
        when(emailAttributeStore.getRefreshToken(USER_EMAIL)).thenReturn(null);

        // Act & Assert
        assertThrows(MustAuthorizeException.class, () -> gmailAuthClient.getAccessToken());
        verifyNoInteractions(oAuth20Service);
    }


    /**
     * Scenario: Two different Gmail accounts configured — each gets its own cached token.
     * Expected: Tokens are stored separately in the map keyed by email.
     */
    @Test
    void getAccessToken_differentUsers_separateCacheEntries() throws Exception {
        // Arrange — first user
        String secondEmail = "another@gmail.com";
        String secondToken = "REDACTED_SECRET";

        when(emailAttributeStore.getRefreshToken(USER_EMAIL)).thenReturn(REFRESH_TOKEN);
        OAuth2AccessToken token1 = mock(OAuth2AccessToken.class);
        when(token1.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(oAuth20Service.refreshAccessToken(REFRESH_TOKEN)).thenReturn(token1);

        // Act — first user fetches token
        String result1 = gmailAuthClient.getAccessToken();
        assertEquals(ACCESS_TOKEN, result1);

        // Arrange — switch to second user
        EmailAttributes secondAttributes = mock(EmailAttributes.class);
        when(secondAttributes.getEmail()).thenReturn(secondEmail);
        GmailAuthClient secondClient = spy(new GmailAuthClient(secondAttributes, emailAttributeStore, ExecutionMode.LIVE));
        doReturn(oAuth20Service).when(secondClient).getAuthService();

        when(emailAttributeStore.getRefreshToken(secondEmail)).thenReturn("second-refresh-token");
        OAuth2AccessToken token2 = mock(OAuth2AccessToken.class);
        when(token2.getAccessToken()).thenReturn(secondToken);
        when(oAuth20Service.refreshAccessToken("second-refresh-token")).thenReturn(token2);

        // Act — second user fetches token
        String result2 = secondClient.getAccessToken();

        // Assert — both tokens cached separately
        assertEquals(secondToken, result2);
        Map<String, ?> cache = getTokenCacheMap();
        assertEquals(2, cache.size());
        assertTrue(cache.containsKey(USER_EMAIL));
        assertTrue(cache.containsKey(secondEmail));
    }

    /**
     * Scenario: IOException during token refresh (network failure).
     * Expected: IllegalStateException is thrown with descriptive message.
     */
    @Test
    void getAccessToken_ioException_throwsIllegalState() throws Exception {
        // Arrange
        when(emailAttributeStore.getRefreshToken(USER_EMAIL)).thenReturn(REFRESH_TOKEN);
        when(oAuth20Service.refreshAccessToken(REFRESH_TOKEN))
                .thenThrow(new IOException("Connection timed out"));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> gmailAuthClient.getAccessToken());
        assertTrue(ex.getMessage().contains("Connection timed out"));
    }

    // ---- Helper methods to access private static tokenCacheMap via reflection ----

    @SuppressWarnings("unchecked")
    private Map<String, Object> getTokenCacheMap() throws Exception {
        Field field = GmailAuthClient.class.getDeclaredField("tokenCacheMap");
        field.setAccessible(true);
        return (Map<String, Object>) field.get(null);
    }

    private void clearTokenCacheMap() throws Exception {
        getTokenCacheMap().clear();
    }

    private void putTokenInCache(String email, String accessToken, long timestamp) throws Exception {
        Map<String, Object> cacheMap = getTokenCacheMap();
        Class<?> tokenCacheClass = Class.forName(
                GmailAuthClient.class.getName() + "$TokenCache");
        var constructor = tokenCacheClass.getDeclaredConstructor(String.class, long.class);
        constructor.setAccessible(true);
        Object tokenCache = constructor.newInstance(accessToken, timestamp);
        cacheMap.put(email, tokenCache);
    }
}