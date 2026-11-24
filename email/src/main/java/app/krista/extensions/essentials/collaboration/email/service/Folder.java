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

import app.krista.ksdk.files.FileRepository;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.List;

/**
 * Represents an email folder.
 * <p>
 * Provides methods to get folder metadata, navigate folder hierarchy,
 * retrieve messages, and perform searches.
 */
public interface Folder {

    /**
     * Gets the unique ID of this folder.
     *
     * @return The folder ID as a string
     */
    String getFolderId();

    /**
     * Gets the display name of this folder.
     *
     * @return The folder name
     */
    String getFolderName();

    /**
     * Gets the parent folder that contains this folder.
     *
     * @return The parent Folder object
     */
    Folder getParent();

    /**
     * Gets the hierarchical path of folders to this folder.
     *
     * @return List of folder names in the path
     */
    List<String> getFolderPath();

    /**
     * Gets the names of all child folders under this folder.
     *
     * @return List of child folder names
     */
    List<String> getChildFolderNames();


    Folder getChildFolderByName(String childFolderName);


    Folder getChildFolderByName(List<String> childFolderPath);


    List<String> getChildFolderIds();


    Folder getChildFolder(String childFolderId);


    List<Email> getEmails(Double pageNumber, Double pageSize, KristaMediaClient kristaMediaClient);


    void moveInto(Folder folder);


    int getMessageCount();


    int getUnreadMessageCount();

    List<Email> getUnreadEmails(Double pageNumber, Double pageSize, KristaMediaClient kristaMediaClient);

    /**
     * Move mails to specified folder and returns message id.
     *
     * @param msgs         list of mails to be moved
     * @param targetFolder destination folder
     * @return lit of message ids
     * @throws MessagingException exception thrown from the server while moving messages
     */
    List<String> moveMessage(Message[] msgs, Folder targetFolder) throws MessagingException;

    /**
     * Performs search on the current folder for specified messageId.
     *
     * @param messageIds mail's unique ids
     * @return found mail
     * @throws MessagingException exception thrown from the server while moving messages
     */
    Message[] search(String[] messageIds) throws MessagingException;

    /**
     * Searches for messages with the given message ID.
     *
     * @param messageId the array of message IDs to search for
     * @return Message object matching the given ID
     * @throws MessagingException if there is an error searching the folder
     */
    Message search(String messageId) throws MessagingException;

    /**
     * Returns object of javax mail folder
     *
     * @return object of javax mail folder
     */
    javax.mail.Folder getJavaMailFolder();

    void open();

    javax.mail.Folder[] listFolders() throws MessagingException;

    /**
     * Searches this folder for messages received after the specified time.
     *
     * @param timeInMillis Cutoff time in milliseconds
     * @return Array of matching Message objects
     * @throws MessagingException On search errors
     */
    Message[] search(long timeInMillis);
}