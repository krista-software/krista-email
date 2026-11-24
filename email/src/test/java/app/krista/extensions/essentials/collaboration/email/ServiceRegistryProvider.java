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

import app.krista.extension.util.ServiceRegistry;
import app.krista.extensions.essentials.collaboration.email.service.KristaMediaClient;
import app.krista.ksdk.context.ExecutionMode;
import app.krista.ksdk.context.RequestContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.mockito.Mockito;

/**
 * Service registry locator class to mock commonly used objects.
 */
public class ServiceRegistryProvider {

    private static ServiceLocator serviceLocator;

    private ServiceRegistryProvider() {

    }

    /**
     * Method to get instance of custom {@link ServiceRegistry} class.
     *
     * @return {@link ServiceRegistry} instance.
     */
    public static ServiceRegistry getServiceRegistry() {
        initializeServiceLocator();
        return ServiceRegistry.initialize(serviceLocator);
    }

    private static void initializeServiceLocator() {
        serviceLocator = ServiceLocatorFactory.getInstance().create(null);

        RequestContext requestContext = Mockito.mock(RequestContext.class);
        Mockito.when(requestContext.getExecutionMode()).thenReturn(ExecutionMode.LIVE);
        ServiceLocatorUtilities.addOneConstant(serviceLocator, requestContext, null, RequestContext.class);

        KristaMediaClient kristaMediaClient = Mockito.mock(KristaMediaClient.class);
        ServiceLocatorUtilities.addOneConstant(serviceLocator, kristaMediaClient, null, KristaMediaClient.class);
    }
}
