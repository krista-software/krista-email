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

package app.krista.extensions.essentials.collaboration.email.service;

import app.krista.extensions.essentials.collaboration.email.exception.MatchingFailureException;

/**
 * Abstract filter interface.
 * <p>
 * Defines a filter that can be applied to an object of type T,
 * with optional chaining to a next filter.
 *
 * @param <T> The type of object this filter will operate on
 */
public interface AbstractFilter<T> {

    /**
     * Applies this filter to the given object.
     *
     * @param t The object to filter
     * @return The filtered object
     * @throws MatchingFailureException If the filter does not match
     */
    T apply(T t) throws MatchingFailureException;

    /**
     * Sets the next filter to chain to.
     *
     * @param nextFilter The filter to run after this one
     */
    void setNextFilter(AbstractFilter<T> nextFilter);
}
