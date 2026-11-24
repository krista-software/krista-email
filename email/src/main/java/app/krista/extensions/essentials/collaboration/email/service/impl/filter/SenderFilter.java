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

package app.krista.extensions.essentials.collaboration.email.service.impl.filter;

import app.krista.extensions.essentials.collaboration.email.exception.MatchingFailureException;
import app.krista.extensions.essentials.collaboration.email.service.AbstractFilter;
import app.krista.extensions.essentials.collaboration.email.service.Email;
import app.krista.extensions.essentials.collaboration.email.util.Messages;
import app.krista.extensions.essentials.collaboration.email.util.Strings;

import java.util.Objects;

import static app.krista.extensions.essentials.collaboration.email.util.MailFilterUtil.isPatternMatches;

/**
 * Filter to match emails by sender address.
 * <p>
 * This filter checks if an email's sender matches the configured regex pattern.
 * It can be chained to other filters using the setNextFilter() method.
 */
public class SenderFilter implements AbstractFilter<Email> {
    private AbstractFilter<Email> nextFilter;
    private final String senderRegEx;

    /**
     * Creates a new SenderFilter.
     *
     * @param senderRegEx Regex pattern to match sender address
     */
    public SenderFilter(String senderRegEx) {
        this.senderRegEx = senderRegEx;
    }

    @Override
    public void setNextFilter(AbstractFilter<Email> nextFilter) {
        this.nextFilter = nextFilter;
    }

    /**
     * Applies the sender filter to the given Email.
     * <p>
     * Checks if the email sender matches the configured regex.
     * If there is a next filter, it is applied after.
     *
     * @param mail The Email to filter
     * @return The filtered Email
     * @throws MatchingFailureException If sender does not match
     */
    @Override
    public Email apply(Email mail) throws MatchingFailureException {
        Email filteredMail = null;
        if (!Objects.isNull(nextFilter)) {
            filteredMail = nextFilter.apply(mail);
        }
        if (!Objects.isNull(filteredMail)) {
            if (Strings.isNullOrBlank(senderRegEx) || isPatternMatches(mail.getSenderEmailAddress().getAddress(), senderRegEx)) {
                return filteredMail;
            }
            throw new MatchingFailureException(Messages.UNMATCHED_SENDER); // when sender doesn't match throw Matching Failure Exception
        }
        return getMatchingMail(mail);
    }

    private Email getMatchingMail(Email mail) throws MatchingFailureException {
        if (Strings.isNullOrBlank(senderRegEx)) {
            return null;
        }
        if (!isPatternMatches(mail.getSenderEmailAddress().getAddress(), senderRegEx)) {
            //NOSONAR When sender doesn't match throw Matching Failure Exception;
            throw new MatchingFailureException(Messages.UNMATCHED_SENDER);
        }
        return mail;
    }
}
