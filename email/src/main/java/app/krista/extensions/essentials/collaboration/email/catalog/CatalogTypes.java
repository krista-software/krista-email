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

package app.krista.extensions.essentials.collaboration.email.catalog;

import app.krista.extensions.essentials.collaboration.email.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.email.exception.MatchingFailureException;
import app.krista.extensions.essentials.collaboration.email.service.Attachment;
import app.krista.extensions.essentials.collaboration.email.service.Email;
import app.krista.extensions.essentials.collaboration.email.service.EmailAddress;
import app.krista.extensions.essentials.collaboration.email.util.Strings;

import java.util.List;
import java.util.stream.Collectors;

public final class CatalogTypes {
    private CatalogTypes() {
    }

    public static MailDetails fromEmail(Email email) throws MatchingFailureException {
        if (email == null) {
            throw new MatchingFailureException("No email found with matching filter");
        }
        MailDetails mailDetails = new MailDetails();
        mailDetails.from = email.getSenderEmailAddress().getAddress();
        mailDetails.to = email.getToEmailAddresses().stream().map(EmailAddress::getAddress).collect(Collectors.joining(Strings.SEMI_COLON_WITH_SPACE));
        mailDetails.message = email.getContent();
        mailDetails.subject = email.getSubject();
        List<Attachment> fileAttachments = email.getFileAttachments();
        mailDetails.fileAttachment = fileAttachments.isEmpty() ? List.of() : fileAttachments.stream().map(Attachment::download).collect(Collectors.toList());
        List<String> itemAttachments = email.getItemAttachments();
        mailDetails.itemAttachment = itemAttachments.isEmpty() ? List.of() : itemAttachments;
        mailDetails.messageID = email.getMessageId();
        mailDetails.cc = email.getCcEmailAddresses().stream().map(EmailAddress::getAddress).collect(Collectors.joining(Strings.SEMI_COLON_WITH_SPACE));
        mailDetails.isRead = email.getRead();
        List<EmailAddress> replyToEmailAddresses = email.getReplyToEmailAddresses();
        mailDetails.replyTo = replyToEmailAddresses.isEmpty() ? "" : replyToEmailAddresses.get(0).getAddress();
        mailDetails.sendDateAndTime = email.getSendDateAndTime();
        mailDetails.receivedDateAndTime = email.getReceivedDateAndTime();
        return mailDetails;
    }
}
