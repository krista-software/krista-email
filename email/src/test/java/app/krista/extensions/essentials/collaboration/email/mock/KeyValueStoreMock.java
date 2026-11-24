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

package app.krista.extensions.essentials.collaboration.email.mock;

import app.krista.extensions.util.KeyValueStore;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class KeyValueStoreMock implements KeyValueStore {
    private static final Map<String, Object> map = new HashMap<>();

    @Override
    public Object get(String s) {
        return map.get(s);
    }

    @Override
    public void remove(String s) {
        map.remove(s);
    }

    @Override
    public boolean contains(String s) {
        return map.containsKey(s);
    }

    @Override
    public void put(String s, Object o) {
        map.put(s, o);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }

    @Override
    public Set<String> getKeys() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return map.entrySet().iterator();
    }
}
