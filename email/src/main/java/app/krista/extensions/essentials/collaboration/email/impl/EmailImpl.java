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

package app.krista.extensions.essentials.collaboration.email.impl;

import app.krista.extensions.essentials.collaboration.email.service.Folder;
import app.krista.extensions.essentials.collaboration.email.service.*;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.essentials.collaboration.email.util.Messages;
import app.krista.omni.sdk.spi.impl.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmailImpl implements Email {

    private static final Logger logger = LoggerFactory.getLogger(EmailImpl.class);

    private final Message message;

    private final KristaMediaClient kristaMediaClient;

    public EmailImpl(Message message, KristaMediaClient kristaMediaClient) {
        this.message = message;
        this.kristaMediaClient = kristaMediaClient;
    }

    /**
     * Retrieves the encoded message ID from the email.
     *
     * @return Encoded message ID string
     * @throws ServerErrorException if message ID retrieval fails
     */
    @Override
    public String getMessageId() {
        try {
            return Messages.encodeMessageId(((MimeMessage) message).getMessageID());
        } catch (MessagingException cause) {
            String errorMessage = Messages.SERVER_ERROR + Constants.COLON + cause.getMessage();
            throw new ServerErrorException(cause.getClass().getName() + "| " + errorMessage, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves the IMAP UID (Unique Identifier) for the message.
     *
     * @return UID as string, or empty string if retrieval fails
     */
    @Override
    public String getUID() {
        UIDFolder folder = (UIDFolder) message.getFolder();
        try {
            return String.valueOf(folder.getUID(message));
        } catch (MessagingException cause) {
            logger.error("Failed to get message of id: {}", cause.getMessage());
        }
        return Constants.EMPTY_STRING;
    }

    /**
     * Retrieves the raw email ID from the message headers.
     *
     * @return Email ID string
     * @throws IllegalStateException if email ID retrieval fails
     */
    @Override
    public String getEmailId() {
        try {
            MimeMessage im = new MimeMessage(message.getSession(), message.getInputStream());
            return im.getMessageID();
        } catch (MessagingException | IOException cause) {
            throw new IllegalStateException("Failed to get email address ", cause);
        }
    }

    /**
     * Retrieves the email subject line.
     *
     * @return Subject text
     * @throws IllegalStateException if subject retrieval fails
     */
    @Override
    public String getSubject() {
        try {
            return message.getSubject();
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get email subject ", cause);
        }
    }

    @Override
    public EmailAddress getSenderEmailAddress() {
        try {
            String email = ((InternetAddress) message.getFrom()[0]).getAddress();
            return new EmailAddress("", email);
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get sender email address ", cause);
        }
    }

    @Override
    public List<EmailAddress> getToEmailAddresses() {
        try {
            Address[] allRecipients = message.getRecipients(Message.RecipientType.TO);
            List<EmailAddress> emailAddresses = new ArrayList<>();
            if (allRecipients == null || allRecipients.length == 0) return emailAddresses;
            for (Address address : allRecipients) {
                String email = ((InternetAddress) address).getAddress();
                String name = ((InternetAddress) address).getPersonal();
                emailAddresses.add(new EmailAddress(name, email));
            }
            return emailAddresses;
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get email address ", cause);
        }
    }

    @Override
    public List<EmailAddress> getReplyToEmailAddresses() {
        try {
            Address[] replyTo = message.getReplyTo();
            if (replyTo == null || replyTo.length == 0) return List.of();
            List<EmailAddress> emailAddresses = new ArrayList<>();
            for (Address address : replyTo) {
                String email = ((InternetAddress) address).getAddress();
                emailAddresses.add(new EmailAddress(null, email));
            }
            return emailAddresses;
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get reply to email address ", cause);
        }
    }

    @Override
    public List<EmailAddress> getCcEmailAddresses() {
        try {
            Address[] ccTypeRecipients = message.getRecipients(Message.RecipientType.CC);
            if (ccTypeRecipients == null || ccTypeRecipients.length == 0) return List.of();
            List<EmailAddress> emailAddresses = new ArrayList<>();
            for (Address address : ccTypeRecipients) {
                String email = ((InternetAddress) address).getAddress();
                emailAddresses.add(new EmailAddress(null, email));
            }
            return emailAddresses;
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get CC email address ", cause);
        }
    }

    @Override
    public List<EmailAddress> getBccEmailAddresses() {
        try {
            Address[] ccTypeRecipients = message.getRecipients(Message.RecipientType.BCC);
            if (ccTypeRecipients == null || ccTypeRecipients.length == 0) return List.of();
            List<EmailAddress> emailAddresses = new ArrayList<>();
            for (Address address : ccTypeRecipients) {
                String email = ((InternetAddress) address).getAddress();
                emailAddresses.add(new EmailAddress(null, email));
            }
            return emailAddresses;
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get BCC email address ", cause);
        }
    }

    @Override
    public Boolean getRead() {
        try {
            return message.isSet(Flags.Flag.ANSWERED) || message.isSet(Flags.Flag.SEEN);
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get Is Read flag for the email message ", cause);
        }
    }

    @Override
    public Long getSendDateAndTime() {
        try {
            return message.getSentDate().getTime();
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get sent date for the email ", cause);
        }
    }

    @Override
    public Long getReceivedDateAndTime() {
        try {
            return message.getReceivedDate().getTime();
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get received date for the email ", cause);
        }
    }

    @Override
    public String getContentType() {
        try {
            return message.getContentType();
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get content type for the email ", cause);
        }
    }

    @Override
    public String getContent() {
        try {
            return getTextFromMessage();
        } catch (MessagingException | IOException cause) {
            throw new IllegalStateException("Failed to get content for the email ", cause);
        }
    }

    @Override
    public void markAsRead() {
        try {
            message.setFlag(Flags.Flag.SEEN, true);
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to mark read for the email ", cause);
        }
    }

    @Override
    public void markAsUnread() {
        try {
            message.setFlag(Flags.Flag.RECENT, true);
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to mark unread for the email ", cause);
        }
    }

    @Override
    public String moveToFolder(Folder folder) {
        try {
            FolderImpl sourceFolder = new FolderImpl(message.getFolder());
            logger.info("source folder: {} and message with subject: {}", sourceFolder.getFolderName(), message.getSubject());
            List<String> messageId = sourceFolder.moveMessage(new Message[]{message}, folder);
            logger.info(Messages.MAIL_MOVED_SUCCESSFULLY + " {}", folder.getFolderName());
            return !messageId.isEmpty() ? messageId.get(0) : Constants.EMPTY_STRING;
        } catch (MessagingException cause) {
            logger.error(Messages.MAIL_MOVEMENT_FAILED + " exception caught: {}", cause.getMessage());
            return Messages.MAIL_MOVEMENT_FAILED + cause.getMessage();
        }
    }

    @Override
    public Email replyText(String message, List<Attachment> attachments) {
        throw new NotSupportedException("Reply Text");
    }

    @Override
    public List<Attachment> getFileAttachments() {
        try {
            List<Attachment> attachments = new ArrayList<>();
            Object content = message.getContent();
            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                for (int index = 0; index < multipart.getCount(); index++) {
                    BodyPart bodyPart = multipart.getBodyPart(index);
                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        attachments.add(new AttachmentImpl(bodyPart,kristaMediaClient));
                    }
                }
            }
            return attachments;
        } catch (IOException | MessagingException cause) {
            String errorMessage = Messages.SERVER_ERROR + Strings.COLON + cause.getMessage();
            throw new ServerErrorException(errorMessage, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<String> getItemAttachments() {
        return List.of();
    }

    @Override
    public String getFolderName() {
        return message.getFolder().getName();
    }

    private String getTextFromMessage() throws IOException, MessagingException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws IOException, MessagingException {
        int count = mimeMultipart.getCount();
        if (count == 0)
            throw new MessagingException("Multipart with no body parts not supported.");
        boolean multipartAlt = new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");
        if (multipartAlt)
            // alternatives appear in an order of increasing
            // faithfulness to the original content. Customize as req'd.
            return getTextFromBodyPart(mimeMultipart.getBodyPart(count - 1));
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            result.append(getTextFromBodyPart(bodyPart));
        }
        return result.toString();
    }

    private String getTextFromBodyPart(BodyPart bodyPart) throws IOException, MessagingException {
        String result = "";
        if (bodyPart.isMimeType("text/plain")) {
            result = (String) bodyPart.getContent();
        } else if (bodyPart.isMimeType("text/html")) {
            String html = (String) bodyPart.getContent();
            result = org.jsoup.Jsoup.parse(html).text();
        } else if (bodyPart.getContent() instanceof MimeMultipart) {
            result = getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
        }
        return result;
    }
}
