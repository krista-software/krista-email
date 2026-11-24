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

package app.krista.extensions.essentials.collaboration.email.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

class OAuthUtilTest {

    @BeforeEach
    void cleanUpBefore() {
        Security.removeProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME);
    }

    @AfterEach
    void cleanUpAfter() {
        Security.removeProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME);
    }

    @Test
    void initializeCustomProvider_addsProviderWhenNotPresent() {
        assertNull(Security.getProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME));
        OAuthUtil.initializeCustomProvider();
        assertNotNull(Security.getProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME));
    }

    @Test
    void initializeCustomProvider_doesNotAddDuplicateProvider() {
        OAuthUtil.initializeCustomProvider();
        OAuthUtil.initializeCustomProvider(); // second call should be a no-op
        // Count occurrences in provider list
        long count = java.util.Arrays.stream(Security.getProviders())
                .filter(p -> Constants.GOOGLE_OAUTH2_PROVIDER_NAME.equals(p.getName()))
                .count();
        assertEquals(1, count);
    }

    @Test
    void removeCustomProvider_removesProviderWhenPresent() {
        OAuthUtil.initializeCustomProvider();
        assertNotNull(Security.getProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME));

        OAuthUtil.removeCustomProvider();
        assertNull(Security.getProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME));
    }

    @Test
    void removeCustomProvider_doesNotThrowWhenProviderAbsent() {
        assertNull(Security.getProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME));
        assertDoesNotThrow(OAuthUtil::removeCustomProvider);
    }

    @Test
    void initializeAndRemove_workTogether() {
        OAuthUtil.initializeCustomProvider();
        assertNotNull(Security.getProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME));

        OAuthUtil.removeCustomProvider();
        assertNull(Security.getProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME));

        OAuthUtil.initializeCustomProvider();
        assertNotNull(Security.getProvider(Constants.GOOGLE_OAUTH2_PROVIDER_NAME));
    }
}