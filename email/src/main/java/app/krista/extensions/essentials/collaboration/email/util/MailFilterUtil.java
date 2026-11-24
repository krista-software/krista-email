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

import app.krista.extensions.essentials.collaboration.email.exception.MatchingFailureException;
import app.krista.extensions.essentials.collaboration.email.service.AbstractFilter;
import app.krista.extensions.essentials.collaboration.email.service.Email;
import app.krista.extensions.essentials.collaboration.email.service.FilterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MailFilterUtil {

    private static final Logger logger = LoggerFactory.getLogger(MailFilterUtil.class);

    private MailFilterUtil() {
    }

    public static Email filterMail(Email email, String folderName, String sender, String subjectRegEx, String toOrCcRegEx) {
        if (areProvidedParametersNull(folderName, sender, subjectRegEx, toOrCcRegEx)) {
            return null;
        }
        AbstractFilter<Email> filter = FilterProvider.get(folderName, sender, subjectRegEx, toOrCcRegEx);
        try {
            return filter.apply(email);
        } catch (MatchingFailureException cause) {
            logger.error(cause.getMessage());
            return null;
        }
    }

    private static boolean areProvidedParametersNull(String folderName, String sender, String subjectRegEx, String toOrCcRegEx) {
        return Objects.isNull(folderName) && Objects.isNull(sender) && Objects.isNull(subjectRegEx) && Objects.isNull(toOrCcRegEx);
    }

    public static boolean isPatternMatches(String string, String regEx) {
        String[] values = regEx.split(Strings.SEMI_COLON);
        boolean matched = false;
        for (String value : values) {
            Pattern pattern = Pattern.compile(value.toLowerCase().trim());
            Matcher matcher = pattern.matcher(string.toLowerCase().trim());
            matched = matcher.find();
            if (matched) {
                break;
            }
        }
        return matched;
    }
}
