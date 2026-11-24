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

import app.krista.extensions.essentials.collaboration.email.service.impl.FolderFilter;
import app.krista.extensions.essentials.collaboration.email.service.impl.filter.SenderFilter;
import app.krista.extensions.essentials.collaboration.email.service.impl.filter.SubjectFilter;
import app.krista.extensions.essentials.collaboration.email.service.impl.filter.ToCcFilter;

public class FilterProvider {
    private FilterProvider() {
    }

    public static AbstractFilter<Email> get(String folderName, String sender, String subjectRegEx, String toOrCcRegEx) {
        AbstractFilter<Email> toOrCcFilter = new ToCcFilter(toOrCcRegEx);
        AbstractFilter<Email> subjectFilter = new SubjectFilter(subjectRegEx);
        AbstractFilter<Email> senderFilter = new SenderFilter(sender);
        AbstractFilter<Email> filter = new FolderFilter(folderName);
        subjectFilter.setNextFilter(toOrCcFilter);
        senderFilter.setNextFilter(subjectFilter);
        filter.setNextFilter(senderFilter);
        return filter;
    }
}
