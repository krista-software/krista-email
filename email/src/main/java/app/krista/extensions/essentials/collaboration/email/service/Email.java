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


import app.krista.extensions.essentials.collaboration.email.util.Messages;

import java.util.List;

/**
 * Class has methods for getting information about the mail such as subject, message id, sender's address etc.
 */
public interface Email {
    /**
     * Returns message id of the mail
     *
     * @return message id of the mail
     */
    String getMessageId();
    String getUID();

    String getEmailId();


    String getSubject();


    EmailAddress getSenderEmailAddress();


    List<EmailAddress> getToEmailAddresses();


    List<EmailAddress> getReplyToEmailAddresses();


    List<EmailAddress> getCcEmailAddresses();


    List<EmailAddress> getBccEmailAddresses();


    Boolean getRead();


    Long getSendDateAndTime();


    Long getReceivedDateAndTime();


    String getContentType();


    String getContent();


    void markAsRead();


    void markAsUnread();

    /**
     * Move email to specified folder
     *
     * @param folder destination folder
     * @return message defined in {@link Messages} for movement.
     */
    String moveToFolder(Folder folder);


    Email replyText(String message, List<Attachment> attachments);


     List<Attachment> getFileAttachments();


    List<String> getItemAttachments();
    String getFolderName();

}