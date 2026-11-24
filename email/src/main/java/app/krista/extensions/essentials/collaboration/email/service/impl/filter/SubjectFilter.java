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

public class SubjectFilter implements AbstractFilter<Email> {
    private AbstractFilter<Email> nextFilter;
    private final String subjectRegEx;

    public SubjectFilter(String subjectRegEx) {
        this.subjectRegEx = subjectRegEx;
    }

    @Override
    public void setNextFilter(AbstractFilter<Email> nextFilter) {
        this.nextFilter = nextFilter;
    }

    @Override
    public Email apply(Email mail) throws MatchingFailureException {
        Email filteredMail = null;
        if (!Objects.isNull(nextFilter)) {
            filteredMail = nextFilter.apply(mail);
        }
        if (!Objects.isNull(filteredMail)) {
            if (Strings.isNullOrBlank(subjectRegEx) || isPatternMatches(mail.getSubject(), subjectRegEx)) {
                return filteredMail;
            }
            throw new MatchingFailureException(Messages.UNMATCHED_SUBJECT); // when sender doesn't match throw Matching Failure Exception
        }
        return isSubjectNotNullOrBlank(mail) ? mail : null;
    }

    private boolean isSubjectNotNullOrBlank(Email mail) throws MatchingFailureException {
        if (Strings.isNullOrBlank(subjectRegEx)) {
            return false;
        }
        if (!isPatternMatches(mail.getSubject(), subjectRegEx)) {
            throw new MatchingFailureException(Messages.UNMATCHED_SUBJECT);
        }
        return true;
    }
}
