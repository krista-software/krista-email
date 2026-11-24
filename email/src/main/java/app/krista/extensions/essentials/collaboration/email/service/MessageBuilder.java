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

import app.krista.extensions.essentials.collaboration.email.service.impl.SMTPMessageImpl;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.essentials.collaboration.email.util.Messages;
import app.krista.model.base.File;
import app.krista.omni.sdk.spi.impl.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MessageBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MessageBuilder.class);
    private static final String HTML_CONTENT_TYPE = "text/html; charset=UTF-8";

    private final Session session;
    private String subject;
    private InternetAddress fromAddress;
    private InternetAddress[] toAddresses;
    private InternetAddress[] ccAddresses;
    private InternetAddress[] bccAddresses;
    private InternetAddress[] replyToAddresses;
    private Multipart multipart;
    private final KristaMediaClient kristaMediaClient;

    public MessageBuilder(Session session, KristaMediaClient kristaMediaClient) {
        this.session = session;
        this.multipart = new MimeMultipart();
        this.kristaMediaClient = kristaMediaClient;
    }

    /**
     * Sets the email subject line.
     *
     * @param subject Email subject text
     * @return MessageBuilder instance for method chaining
     */
    public MessageBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Sets the email message body content (HTML or plain text).
     *
     * @param messageBody Email body content
     * @return MessageBuilder instance for method chaining
     */
    public MessageBuilder messageBody(String messageBody) {
        addBody(messageBody);
        return this;
    }

    /**
     * Sets the sender email address.
     *
     * @param emailAddress Sender's email address
     * @return MessageBuilder instance for method chaining
     */
    public MessageBuilder from(String emailAddress) {
        InternetAddress[] internetAddresses = getInternetAddresses(emailAddress);
        fromAddress = internetAddresses != null ? internetAddresses[0] : null;
        return this;
    }

    /**
     * Sets the primary recipient email addresses.
     *
     * @param toS Comma-separated recipient email addresses
     * @return MessageBuilder instance for method chaining
     */
    public MessageBuilder to(String toS) {
        toAddresses = getInternetAddresses(toS);
        return this;
    }

    /**
     * Sets the carbon copy (CC) recipient email addresses.
     *
     * @param ccS Comma-separated CC email addresses
     * @return MessageBuilder instance for method chaining
     */
    public MessageBuilder cc(String ccS) {
        ccAddresses = getInternetAddresses(ccS);
        return this;
    }

    /**
     * Sets the blind carbon copy (BCC) recipient email addresses.
     *
     * @param bccS Comma-separated BCC email addresses
     * @return MessageBuilder instance for method chaining
     */
    public MessageBuilder bcc(String bccS) {
        bccAddresses = getInternetAddresses(bccS);
        return this;
    }

    /**
     * Sets the reply-to email address.
     *
     * @param replyTos Reply-to email address
     * @return MessageBuilder instance for method chaining
     */
    public MessageBuilder replyTo(String replyTos) {
        replyToAddresses = getInternetAddresses(replyTos);
        return this;
    }

    /**
     * Adds file attachments to the email message.
     * Validates total attachment size against provider limits.
     *
     * @param attachments List of files to attach
     * @param maxAttachmentSize Maximum allowed total attachment size in bytes
     * @return MessageBuilder instance for method chaining
     * @throws IllegalArgumentException if total attachment size exceeds limit
     */
    public MessageBuilder attachments(List<File> attachments, long maxAttachmentSize) {
        if (!Objects.isNull(attachments)) {
            long attachmentsTotalSize = attachments.stream().mapToLong(File::getLength).sum();
            if (attachmentsTotalSize > maxAttachmentSize) {
                throw new IllegalArgumentException("Total attachment size exceeds the maximum limit of " + getSizeInMB(maxAttachmentSize) + "MB. Current size: " + getSizeInMB(attachmentsTotalSize) + "MB. Please reduce the number or size of attachments.");
            }
            for (File attachment : attachments) {
                try {
                    java.io.File file = kristaMediaClient.toJavaFile(attachment);
                    BodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(file.getPath());
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(source.getName());
                    multipart.addBodyPart(attachmentPart);
                } catch (IOException | MessagingException cause) {
                    String errorMessage = Messages.SERVER_ERROR + Constants.COLON + cause.getMessage();
                    logger.error(errorMessage);
                    throw new ServerErrorException(errorMessage, Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        }
        if (!isMultiPartExists(multipart)) {
            multipart = null;
        }
        return this;
    }

    /**
     * Builds and returns the complete MIME message with all configured properties.
     * Assembles subject, recipients, body, and attachments into a Jakarta Mail Message.
     *
     * @return Configured Message instance ready to send
     * @throws IllegalArgumentException if message construction fails
     */
    public Message build() {
        Message message = new SMTPMessageImpl(session);
        try {
            message.setFrom(fromAddress);
            message.setSubject(subject);
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            message.setRecipients(Message.RecipientType.CC, ccAddresses);
            message.setRecipients(Message.RecipientType.BCC, bccAddresses);
            message.setReplyTo(replyToAddresses);
            if (!Objects.isNull(multipart)) {
                message.setContent(multipart);
            }
        } catch (MessagingException cause) {
            throw new ServerErrorException(Messages.SERVER_ERROR + Constants.COLON + cause.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
        return message;
    }

    private InternetAddress[] getInternetAddresses(String emailAddresses) {
        try {
            return emailAddresses != null ? InternetAddress.parse(emailAddresses) : null;
        } catch (AddressException cause) {
            throw new ServerErrorException(Messages.SERVER_ERROR + Constants.COLON + cause.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isMultiPartExists(Multipart multipartPayload) {
        try {
            return multipartPayload.getCount() > 0;
        } catch (MessagingException cause) {
            String errorMsg = Messages.SERVER_ERROR + Strings.COLON + cause.getMessage();
            logger.error(errorMsg);
            throw new ServerErrorException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private void addBody(String messageBody) {
        BodyPart bodyPart = new MimeBodyPart();
        try {
            messageBody = messageBody.replace("\n", "<br/>");
            bodyPart.setContent(messageBody, HTML_CONTENT_TYPE);
            multipart.addBodyPart(bodyPart);
        } catch (MessagingException cause) {
            String errorMsg = Messages.SERVER_ERROR + Strings.COLON + cause.getMessage();
            logger.error(errorMsg);
            throw new ServerErrorException(errorMsg, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private int getSizeInMB(long attachmentsSizeInBytes) {
        return (int) (attachmentsSizeInBytes / Constants.ONE_MB_TO_BYTE);
    }
}
