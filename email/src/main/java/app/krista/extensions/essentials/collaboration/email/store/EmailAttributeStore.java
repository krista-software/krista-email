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
import app.krista.extensions.essentials.collaboration.email.EmailAttributes;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import app.krista.model.field.util.JavaTypes;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;

@Service
public class EmailAttributeStore {

    private final KeyValueStore keyValueStore;
    private final Invoker invoker;

    @Inject
    public EmailAttributeStore(Invoker invoker, KeyValueStore keyValueStore) {
        this.invoker = invoker;
        this.keyValueStore = keyValueStore;
    }

    public void put(Map<String, Object> attributes) {
        keyValueStore.put(Constants.EMAIL_ATTRIBUTES_KEY,
                Constants.GSON_JSON_MAPPER.toString(attributes));
    }

    private EmailAttributes get() {
        Map<String, Object> attributes = Constants.GSON_JSON_MAPPER.fromString(
                String.valueOf(keyValueStore.get(Constants.EMAIL_ATTRIBUTES_KEY)),
                JavaTypes.getTypedClass(Map.class, String.class, Object.class));
        if (!Objects.isNull(attributes)) {
            return new EmailAttributes(invoker, attributes);
        }
        return null;
    }

    public Object getRefreshToken(String username) {
        return this.keyValueStore.get(toKey(username));
    }


    /**
     * This method returns the saved email attributes if present in case of validate attributes and helps in determining
     * the correct auth client to be used for set parameters.
     *
     * @return {@link EmailAttributes} if exist any in the key value store.
     */
    public EmailAttributes getEmailAttributes() {
        EmailAttributes tempEmailAttributes = get();
        if (!Objects.isNull(tempEmailAttributes)) {
            return tempEmailAttributes;
        }
        return null;
    }

    public void saveAccessToken(String username, String refreshToken) {
        this.keyValueStore.put(toKey(username), refreshToken);
    }

    public void removeAttributes() {
        keyValueStore.remove(Constants.EMAIL_ATTRIBUTES_KEY);
    }

    public void removeAccessToken(String username) {
        this.keyValueStore.remove(toKey(username));
    }

    private String toKey(String user) {
        return user + Constants.REFRESH_TOKEN_SUFFIX;
    }

    public long getLastFetchMailsRequestTime() {
        String time = (String) keyValueStore.get(Constants.LAST_FETCH_MAILS_REQUEST_TIME_KEY);
        return !Objects.isNull(time) ? Long.parseLong(time) : System.currentTimeMillis();
    }

    public void saveLastFetchMailsRequestTime(long timeInMillis) {
        keyValueStore.put(Constants.LAST_FETCH_MAILS_REQUEST_TIME_KEY, timeInMillis);
    }
}
