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

import app.krista.extensions.essentials.collaboration.email.service.Attachment;
import app.krista.extensions.essentials.collaboration.email.service.KristaMediaClient;
import com.sun.mail.imap.IMAPBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;

public class AttachmentImpl implements Attachment {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentImpl.class);

    private final BodyPart attachment;
    private final KristaMediaClient kristaMediaClient;

    public AttachmentImpl(BodyPart attachment, KristaMediaClient kristaMediaClient) {
        this.attachment = attachment;
        this.kristaMediaClient = kristaMediaClient;
    }

    @Override
    public String getName() throws MessagingException {
        return attachment.getFileName();
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public int getSize() throws MessagingException {
        return attachment.getSize() != 0 ? attachment.getSize() : 0;
    }

    @Override
    public String getMimeType() throws MessagingException {
        return attachment.getContentType();
    }

    @Override
    public app.krista.model.base.File download() {
        try {
            File fileToBeUploaded = new File(getName());
            ((IMAPBodyPart) attachment).saveFile(getName());
            System.out.println("fileName:" + fileToBeUploaded.getName());
            return kristaMediaClient.toKristaFile(fileToBeUploaded);
        } catch (IOException | MessagingException cause) {
            logger.error("Error while downloading file: {}", cause.getMessage());
            return null;
        }
    }
}
