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

package app.krista.extensions.essentials.collaboration.email.catalog.entities;

import app.krista.extension.impl.anno.*;
import app.krista.model.base.File;

import java.util.List;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "442034bc-7967-4349-9b66-6b13682ca806")
@Entity(name = "Mail Details", id = "localDomainEntity_0fb99723-377c-419e-b24e-0ab0ce948e8c", primaryKey = "Message ID", supportStore = false, options = {})
public class MailDetails {

    @Field.Text(name = "From", required = false, attributes = {}, options = {})
    public String from;

    @Field.Text(name = "To", required = true, attributes = {}, options = {})
    public String to;

    @Field(name = "Message", type = "RichText", required = true, attributes = {}, options = {})
    public String message;

    @Searchable
    @Field.Text(name = "Subject", required = true, attributes = {}, options = {})
    public String subject;

    @Field.File(name = "File Attachment", multipleFileUpload = true, required = false, attributes = {}, options = {})
    public List<File> fileAttachment;

    @Field.Desc(name = "Item attachment", type = "[ Label ]", required = false)
    public List<String> itemAttachment;

    @Field.File(name = "Reference attachment", multipleFileUpload = false, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public File referenceAttachment;

    @Field.Text(name = "Message ID", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String messageID;

    @Field.Text(name = "Cc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String cc;

    @Field.Text(name = "Bcc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String bcc;

    @Field.Boolean(name = "Is Read", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Boolean isRead;

    @Field.Text(name = "Reply To", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String replyTo;

    @Field.Date(name = "Send Date and Time", required = false, includeTimeOfDay = true, showHowManyDaysInViewer = 60, allowPast = false, allowToday = false, allowFuture = false, defaultTimeSpan = 1, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Long sendDateAndTime;

    @Field.Date(name = "Received Date and Time", required = false, includeTimeOfDay = true, showHowManyDaysInViewer = 60, allowPast = false, allowToday = false, allowFuture = false, defaultTimeSpan = 1, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Long receivedDateAndTime;

    @Field.Desc(name = "Categories", type = "[ Text ]", required = false)
    public List<String> categories;
}
