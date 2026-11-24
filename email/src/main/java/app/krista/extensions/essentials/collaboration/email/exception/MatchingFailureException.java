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

package app.krista.extensions.essentials.collaboration.email.exception;

/**
 * MatchingFailureException is a custom exception class that extends Exception.
 * It is thrown when there is a failure in matching some criteria.
 * Typical usage:
 * throw new MatchingFailureException("No matches found for given criteria");
 *
 * @author Gaurav Baranwal
 * @version 1.0
 */
public class MatchingFailureException extends RuntimeException {
    /**
     * Constructs a MatchingFailureException with no detail message.
     */
    public MatchingFailureException() {
    }

    /**
     * Constructs a MatchingFailureException with the specified detail message.
     *
     * @param message The detail message
     */
    public MatchingFailureException(String message) {
        super(message);
    }
}
