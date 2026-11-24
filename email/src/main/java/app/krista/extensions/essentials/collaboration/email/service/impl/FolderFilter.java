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

package app.krista.extensions.essentials.collaboration.email.service.impl;

import app.krista.extensions.essentials.collaboration.email.exception.MatchingFailureException;
import app.krista.extensions.essentials.collaboration.email.service.AbstractFilter;
import app.krista.extensions.essentials.collaboration.email.service.Email;
import app.krista.extensions.essentials.collaboration.email.util.Messages;
import app.krista.extensions.essentials.collaboration.email.util.Strings;

import java.util.Objects;

/**
 * Filter to match emails by folder name.
 * <p>
 * This filter checks if an email belongs to the specified folder name.
 * It can be chained to other filters using the setNextFilter() method.
 */
public class FolderFilter implements AbstractFilter<Email> {
    private AbstractFilter<Email> nextFilter;
    private final String folderName;

    /**
     * Creates a new FolderFilter.
     *
     * @param folderName The name of the folder to match
     */
    public FolderFilter(String folderName) {
        this.folderName = folderName;
    }

    @Override
    public void setNextFilter(AbstractFilter<Email> nextFilter) {
        this.nextFilter = nextFilter;
    }

    /**
     * Applies this filter to the given Email.
     * <p>
     * Checks if the email matches the configured folder name. If there is a
     * next filter, it is applied after this one.
     *
     * @param mail The Email to filter
     * @return The filtered Email
     * @throws MatchingFailureException If no match on folder name
     */
    @Override
    public Email apply(Email mail) throws MatchingFailureException {
        Email filteredMail = null;
        if (!Objects.isNull(nextFilter)) {
            filteredMail = nextFilter.apply(mail);
        }
        if (!Objects.isNull(filteredMail)) {
            if (Strings.isNullOrBlank(folderName) || Objects.equals(filteredMail.getFolderName().toLowerCase(), folderName.toLowerCase())) {
                return filteredMail;
            }
            throw new MatchingFailureException(Messages.UNMATCHED_FOLDER + " with " + folderName); // Folder doesn't match throw MatchingFailureException
        }
        return getMatchingMail(mail);
    }

    private Email getMatchingMail(Email mail) throws MatchingFailureException {
        if (Strings.isNullOrBlank(folderName)) {
            return null;
        }
        if (!Objects.equals(mail.getFolderName().toLowerCase(), folderName.toLowerCase())) {
            //NOSONAR When folder doesn't match throw Matching Failure Exception;
            throw new MatchingFailureException(Messages.UNMATCHED_FOLDER + " with " + folderName);
        }
        return mail;
    }
}
