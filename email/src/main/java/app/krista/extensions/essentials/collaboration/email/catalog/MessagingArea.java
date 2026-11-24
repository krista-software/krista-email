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

import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.email.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.email.impl.EmailImpl;
import app.krista.extensions.essentials.collaboration.email.service.EmailClient;
import app.krista.extensions.essentials.collaboration.email.service.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.email.service.MessageBuilder;
import app.krista.extensions.essentials.collaboration.email.service.SmtpClient;
import app.krista.extensions.essentials.collaboration.email.store.EmailAttributeStore;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.essentials.collaboration.email.util.MailFilterUtil;
import app.krista.extensions.essentials.collaboration.email.util.Messages;
import app.krista.model.base.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.Message;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "b94af183-4891-4b54-a9b0-d6096b361fc7")
public class MessagingArea {

    private static final Logger logger = LoggerFactory.getLogger(MessagingArea.class);

    private final EmailClient emailClient;
    private final KristaMediaClient kristaMediaClient;

    @Inject
    public MessagingArea(EmailClient emailClient, KristaMediaClient kristaMediaClient) {
        this.emailClient = emailClient;
        this.kristaMediaClient = kristaMediaClient;
    }

    /**
     * Moves an email message from its current folder to a specified destination folder.
     * Uses IMAP COPY and DELETE operations to move the message. The operation is idempotent
     * and automatically detects the source folder.
     *
     * @param messageID Unique message identifier (from Mail Details entity)
     * @param folderName Destination folder name (case-sensitive, must exist)
     * @return Success message "Message moved successfully to [folder]"
     * @throws IllegalArgumentException if message ID is invalid or folder doesn't exist
     */
    @CatalogRequest(
            id = "localDomainRequest_95edb739-f511-453c-b0ec-647daf0df206",
            name = "Move Message",
            description = "Accepts message ID, and folder name as input and move one message from source folder to another folder and returns response message.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message ID", type = "Text", required = false)
    public String moveMessage(
            @Field(name = "Message ID", type = "Text") String messageID,
            @Field(name = "Folder Name", type = "Text") String folderName) {
        logger.info("Move message triggered");
        return emailClient.getImapClient().enqueueMessageForMove(folderName, Messages.decodeMessageId(messageID));
    }

    /**
     * Sends an email message via SMTP with support for attachments, CC, BCC, and Reply-To.
     * Automatically converts semicolon-separated email addresses to comma-separated format.
     * Supports HTML message content and multiple file attachments.
     *
     * @param subject Email subject line (required)
     * @param message Email body content in HTML or plain text (required)
     * @param attachments List of file attachments (optional, max 25MB for Gmail, 35MB for Outlook)
     * @param to Primary recipient email addresses, comma or semicolon-separated (required)
     * @param bcc Blind carbon copy recipients, comma or semicolon-separated (optional)
     * @param cc Carbon copy recipients, comma or semicolon-separated (optional)
     * @param replyTo Reply-to email address (optional)
     * @return Success message "Mail sent successfully."
     * @throws IllegalArgumentException if required fields are missing or attachment size exceeds limit
     */
    @CatalogRequest(
            id = "localDomainRequest_6d34be22-e420-4087-b55d-0659f899b140",
            name = "Send Mail",
            description = "Send a mail",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Message", required = false, attributes = {}, options = {})
    public String sendMail(
            @Field.Text(name = "Subject", required = true, attributes = {}, options = {}) String subject,
            @Field(name = "Message", type = "RichText", required = true, attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) List<File> attachments,
            @Field.Text(name = "To", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String to,
            @Field.Text(name = "Bcc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bcc,
            @Field.Text(name = "Cc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String cc,
            @Field.Text(name = "Reply To", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String replyTo) {
        to = !Objects.isNull(to) ? to.replace(Constants.SEMI_COLON, Constants.COMMA) : null;
        bcc = !Objects.isNull(bcc) ? bcc.replace(Constants.SEMI_COLON, Constants.COMMA) : null;
        cc = !Objects.isNull(cc) ? cc.replace(Constants.SEMI_COLON, Constants.COMMA) : null;
        replyTo = !Objects.isNull(replyTo) ? replyTo.replace(Constants.SEMI_COLON, Constants.COMMA) : null;
        SmtpClient smtpClient = emailClient.getSmtpClient();
        MessageBuilder messageBuilder = new MessageBuilder(smtpClient.getSession(),kristaMediaClient);
        Message mailMessage = messageBuilder.subject(subject)
                .messageBody(message)
                .from(emailClient.me())
                .to(to)
                .bcc(bcc)
                .cc(cc)
                .replyTo(replyTo)
                .attachments(attachments, emailClient.getMaxAttachmentSize())
                .build();
        smtpClient.send(mailMessage);
        return "Mail sent successfully.";
    }

    /**
     * Fetches emails from a specified folder with time-based filtering and optional search criteria.
     * Designed for scheduled execution - only retrieves emails received since the last fetch time.
     * Tracks last fetch time automatically to avoid duplicate processing.
     *
     * @param folder Folder name to fetch emails from (required, case-sensitive)
     * @param sender Filter by sender email address (optional, partial match supported)
     * @param subject Filter by subject text (optional, partial match supported)
     * @param toOrCc Filter by To or Cc email address (optional, partial match supported)
     * @return List of Mail Details entities matching the criteria
     * @throws IllegalArgumentException if folder doesn't exist or IMAP connection fails
     */
    @CatalogRequest(
            id = "localDomainRequest_79de3277-f92a-4db1-abd2-add68053e7a4",
            name = "Fetch mails",
            description = "Request to fetch mails when scheduler runs",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    public List<MailDetails> fetchMails(
            @Field.Text(name = "Folder", required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Mails will be fetch from this folder'")}, options = {}) String folder,
            @Field.Text(name = "Sender", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String sender,
            @Field.Text(name = "Subject", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String subject,
            @Field.Text(name = "To or Cc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String toOrCc) {
        long currentRequestTime = System.currentTimeMillis();
        EmailAttributeStore emailStore = emailClient.getEmailStore();
        long lastRequestProcessedTime = emailStore.getLastFetchMailsRequestTime();
        emailStore.saveLastFetchMailsRequestTime(currentRequestTime);
        Message[] foundMails = emailClient.getFolderClient().getFolder(folder).search(lastRequestProcessedTime);
        logger.info("Found mails: {}", foundMails.length);
        return Arrays
                .stream(foundMails)
                .map(foundMail -> new EmailImpl(foundMail, kristaMediaClient))
                .map(mail -> MailFilterUtil.filterMail(mail, folder, sender, subject, toOrCc))
                .filter(mail -> !Objects.isNull(mail))
                .map(CatalogTypes::fromEmail).collect(Collectors.toList());
    }

}