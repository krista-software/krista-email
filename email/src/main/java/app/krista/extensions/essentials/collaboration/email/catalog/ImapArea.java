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

import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.email.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.email.exception.MatchingFailureException;
import app.krista.extensions.essentials.collaboration.email.service.Email;
import app.krista.extensions.essentials.collaboration.email.service.EmailClient;
import app.krista.extensions.essentials.collaboration.email.service.Folder;
import app.krista.extensions.essentials.collaboration.email.service.KristaMediaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "479e7ef4-8a1c-46a0-afbe-f360d6388fda")
public class ImapArea {

    private static final Logger logger = LoggerFactory.getLogger(ImapArea.class);

    private final EmailClient emailClient;
    private final KristaMediaClient kristaMediaClient;
    app.krista.model.field.Field textField = new app.krista.model.field.Field("com.krista.fields.Text",
            Map.of(), Map.of());

    @Inject
    public ImapArea(EmailClient emailClient, KristaMediaClient kristaMediaClient) {
        this.emailClient = emailClient;
        this.kristaMediaClient = kristaMediaClient;
    }

    /**
     * Retrieves unread emails from a specified folder with pagination support.
     * Only returns emails with "Is Read" status = false. Supports page-based retrieval
     * for large result sets. Does not track last fetch time (unlike Fetch Mails).
     *
     * @param folderName Folder name to retrieve unread emails from (required, case-sensitive)
     * @param pageIndex Zero-based page index for pagination (optional, default: 0)
     * @param pageSize Number of emails per page (optional, default: 50)
     * @return List of Mail Details entities for unread emails, or empty list if none found
     * @throws IllegalArgumentException if folder doesn't exist or page parameters are invalid
     * @throws NoSuchElementException if email parsing fails
     */
    @CatalogRequest(
            id = "localDomainRequest_9e46d509-b337-41aa-b29f-dc57b2440396",
            name = "Retrieve unread emails from folder",
            description = "Get the details of unread emails from folders like Inbox, Sent, etc.",
            area = "Imap",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mail Details", type = "[ Entity(Mail Details) ]", required = false)
    public List<MailDetails> retrieveUnreadEmailsFromFolder(
            @Field(name = "Folder name", type = "Text", required = true, attributes = {}, options = {}) String folderName,
            @Field(name = "Page Index", type = "Number", required = false, attributes = {}, options = {}) Double pageIndex,
            @Field(name = "Page Size", type = "Number", required = false, attributes = {}, options = {}) Double pageSize) {
        logger.debug("Fetching data from folder: {}", folderName);
        Folder folder = emailClient.getFolderClient().getFolder(folderName);
        if (folder == null || folder.getMessageCount() == 0) return List.of();
        logger.info("Getting Unread emails FolderName: {} PageIndex: {} PageSize: {}",
                folderName, pageIndex, pageSize);
        folder.open();
        List<Email> emails = folder.getUnreadEmails(pageIndex, pageSize, kristaMediaClient);
        if (emails == null || emails.isEmpty()) return List.of();
        return emails.stream().map(email -> {
            try {
                return CatalogTypes.fromEmail(email);
            } catch (MatchingFailureException cause) {
                throw new NoSuchElementException("No Mail Found");
            }
        }).collect(Collectors.toList());
    }
}