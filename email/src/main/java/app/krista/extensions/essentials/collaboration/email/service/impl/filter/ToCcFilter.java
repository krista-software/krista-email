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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static app.krista.extensions.essentials.collaboration.email.util.MailFilterUtil.isPatternMatches;

/**
 * Filter to match emails by recipient addresses.
 * <p>
 * This filter checks if an email's To or CC addresses match the configured regex pattern.
 * It can be chained to other filters using the setNextFilter() method.
 */
public class ToCcFilter implements AbstractFilter<Email> {

    private static final Logger logger = LoggerFactory.getLogger(ToCcFilter.class);

    private AbstractFilter<Email> nextFilter;
    private final String toCcRegex;

    /**
     * Creates a new ToCcFilter.
     *
     * @param toCcRegex Regex pattern to match To/CC addresses
     */
    public ToCcFilter(String toCcRegex) {
        this.toCcRegex = toCcRegex;
    }

    @Override
    public void setNextFilter(AbstractFilter<Email> nextFilter) {
        this.nextFilter = nextFilter;
    }

    /**
     * Applies the To/CC filter to the given Email.
     * <p>
     * Checks if the To or CC addresses match the configured regex.
     * If there is a next filter, it is applied after this one.
     *
     * @param email The Email to filter
     * @return The filtered Email or null if no match
     * @throws MatchingFailureException If no address matches the regex
     */
    @Override
    public Email apply(Email email) throws MatchingFailureException {
        Email filteredMail = null;
        if (!Objects.isNull(nextFilter)) {
            filteredMail = nextFilter.apply(email);
        }
        if (!Objects.isNull(filteredMail)) {
            StringBuilder mailAddress = new StringBuilder();
            email.getCcEmailAddresses().forEach(ccAddress -> mailAddress.append(ccAddress.getAddress()).append(Strings.SEMI_COLON));
            email.getToEmailAddresses().forEach(toAddress -> mailAddress.append(toAddress.getAddress()).append(Strings.SEMI_COLON));
            logger.info("toOrCcAddresses: {}", mailAddress);
            if (Strings.isNullOrBlank(toCcRegex) || isPatternMatches(mailAddress.toString(), toCcRegex)) {
                return filteredMail;
            }
            throw new MatchingFailureException(Messages.UNMATCHED_SUBJECT); // when sender doesn't match throw Matching Failure Exception
        }
        return isToOrCcNotNullOrBlank(email) ? email : null;
    }

    private boolean isToOrCcNotNullOrBlank(Email email) throws MatchingFailureException {
        if (Strings.isNullOrBlank(toCcRegex)) {
            return false;
        }
        StringBuilder mailAddress = new StringBuilder();
        email.getCcEmailAddresses().forEach(ccAddress -> mailAddress.append(ccAddress.getAddress()).append(Strings.SEMI_COLON));
        email.getToEmailAddresses().forEach(toAddress -> mailAddress.append(toAddress.getAddress()).append(Strings.SEMI_COLON));
        logger.info("toOrCcAddresses: {}", mailAddress);
        if (!isPatternMatches(mailAddress.toString(), toCcRegex)) {
            throw new MatchingFailureException(Messages.UNMATCHED_SUBJECT);
        }
        return true;
    }
}
