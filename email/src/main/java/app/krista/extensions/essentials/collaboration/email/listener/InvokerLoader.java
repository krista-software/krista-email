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

package app.krista.extensions.essentials.collaboration.email.listener;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Purpose of this class is to restart invoker if in case it got killed via process monitoring.
 */
public class InvokerLoader implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(InvokerLoader.class);

    private final String routingUrl;

    public InvokerLoader(String routingUrl) {
        this.routingUrl = routingUrl;
    }

    @Override
    public void run() {
        try {
            logger.debug("Started request to restart invoker");
            OkHttpClient okHttpClient = new OkHttpClient();
            String url = routingUrl + "/rest/email/reloadInvoker";
            Request request = new Request.Builder().url(url).build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                logger.debug("Successfully hit the invoker reload request with response code: {}", response.code());
            } else {
                logger.error("Some error occurred while hitting the request with response code: {}", response.code());
            }
        } catch (Exception cause) {
            logger.error("Error while invoking invoker: {}", cause.getMessage());
        }
    }
}
