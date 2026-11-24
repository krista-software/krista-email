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

import app.krista.extension.executor.Invoker;
import app.krista.extension.util.InvokerAttributeProvider;

import java.util.Map;

public class InvokerAttributeProviderImpl<T> extends InvokerAttributeProvider<T> {
    private Map<String, Object> attributes;
    private String attributeName;

    public InvokerAttributeProviderImpl(Invoker invoker, String attributeName) {
        super(invoker, attributeName);
    }

    public InvokerAttributeProviderImpl(Map<String, Object> attributes, String attributeName) {
        super(null, attributeName);
        this.attributes = attributes;
        this.attributeName = attributeName;
    }


    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        return (T) attributes.get(attributeName);
    }
}
