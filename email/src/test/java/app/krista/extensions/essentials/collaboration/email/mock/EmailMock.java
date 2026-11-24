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

package app.krista.extensions.essentials.collaboration.email.mock;

import app.krista.extensions.essentials.collaboration.email.service.Attachment;
import app.krista.extensions.essentials.collaboration.email.service.Email;
import app.krista.extensions.essentials.collaboration.email.service.EmailAddress;
import app.krista.extensions.essentials.collaboration.email.service.Folder;

import java.util.List;

public class EmailMock implements Email {
    private final String folderName;
    private final String senderEmailAddress;
    private final String subject;
    private final List<EmailAddress> toAddresses;
    private final List<EmailAddress> ccAddresses;

    public EmailMock(String sender, String subject, String folder, List<EmailAddress> toAddresses, List<EmailAddress> ccAddresses) {
        this.folderName=folder;
        this.senderEmailAddress=sender;
        this.subject=subject;
        this.toAddresses = toAddresses;
        this.ccAddresses = ccAddresses;
    }

    @Override
    public String getMessageId() {
        return "<DM8P3BC38910AA@DM8PR16MB4584.namprd16.prod.outlook.com>";
    }

    @Override
    public String getUID() {
        return null;
    }

    @Override
    public String getEmailId() {
        return null;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public EmailAddress getSenderEmailAddress() {
        return new EmailAddress("abc", senderEmailAddress);
    }

    @Override
    public List<EmailAddress> getToEmailAddresses() {
        return toAddresses;
    }

    @Override
    public List<EmailAddress> getReplyToEmailAddresses() {
        return List.of();
    }

    @Override
    public List<EmailAddress> getCcEmailAddresses() {
        return ccAddresses;
    }

    @Override
    public List<EmailAddress> getBccEmailAddresses() {
        return List.of();
    }

    @Override
    public Boolean getRead() {
        return Boolean.FALSE;
    }

    @Override
    public Long getSendDateAndTime() {
        return System.currentTimeMillis();
    }

    @Override
    public Long getReceivedDateAndTime() {
        return System.currentTimeMillis();
    }

    @Override
    public String getContentType() {
        return "html";
    }

    @Override
    public String getContent() {
        return "testing a mail";
    }

    @Override
    public void markAsRead() {

    }

    @Override
    public void markAsUnread() {

    }

    @Override
    public String moveToFolder(Folder folder) {
        return getMessageId();
    }

    @Override
    public Email replyText(String message, List<Attachment> attachments) {
        return null;
    }

    @Override
    public List<Attachment> getFileAttachments() {
        return List.of();
    }

    @Override
    public List<String> getItemAttachments() {
        return List.of();
    }

    @Override
    public String getFolderName() {
        return folderName;
    }
}
