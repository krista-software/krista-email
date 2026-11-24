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

package app.krista.extensions.essentials.collaboration.email.service.impl.imap;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.SearchTerm;
import javax.ws.rs.NotFoundException;
import java.util.Arrays;

/**
 * The IMAPFolderImpl class is a subclass of the IMAPFolder class and provides additional functionality for searching
 * messages in an IMAP folder based on a search term and a cutoff time
 */
public class IMAPFolderImpl extends IMAPFolder {

    /**
     * Create an instance of IMAPFolderImpl
     *
     * @param folder
     * @throws MessagingException
     */
    public IMAPFolderImpl(Folder folder) throws MessagingException {
        super(folder.getFullName(), folder.getSeparator(), (IMAPStore) folder.getStore(), null);
    }

    /**
     * Searches this IMAP folder for messages matching the given search term
     * that were received after the specified time.
     *
     * @param term         the search term to match messages against
     * @param timeInMillis the cutoff time - only messages received after this
     *                     time will be returned
     * @return an array of Message objects matching the criteria.
     * @throws MessagingException if there is an error searching the folder
     * @throws NotFoundException  if there is an error getting a message's
     *                            received date
     */
    public synchronized Message[] search(SearchTerm term, long timeInMillis) {
        try {
            Message[] messages = super.search(term);
            return Arrays.stream(messages).filter(msg -> {
                try {
                    return msg.getReceivedDate().getTime() >= timeInMillis;
                } catch (MessagingException cause) {
                    throw new NotFoundException(cause);
                }
            }).toArray(Message[]::new);
        } catch (MessagingException cause) {
            throw new NotFoundException(cause);
        }
    }
}
