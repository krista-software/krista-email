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

import app.krista.extensions.essentials.collaboration.email.service.Email;
import app.krista.extensions.essentials.collaboration.email.service.Folder;
import app.krista.extensions.essentials.collaboration.email.service.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.imap.IMAPFolderImpl;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.essentials.collaboration.email.util.Messages;
import com.sun.mail.imap.IMAPFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FolderImpl implements Folder {

    private static final Logger logger = LoggerFactory.getLogger(FolderImpl.class);

    private final javax.mail.Folder folder;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE_NUMBER = 1;
    private javax.mail.Folder[] allFolders;

    public FolderImpl(javax.mail.Folder folder) {
        this.folder = folder;
    }

    @Override
    public String getFolderId() {
        if (folder == null) return null;
        try {
            return folder.getURLName().toString();
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get ID of the email folder ", cause);
        }
    }

    @Override
    public String getFolderName() {
        if (folder == null) return null;
        return folder.getName();
    }

    @Override
    public Folder getParent() {
        if (folder == null) return null;
        try {
            javax.mail.Folder parentFolder = folder.getParent();
            return new FolderImpl(parentFolder);
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get parent folder for the email folder ", cause);
        }
    }

    @Override
    public List<String> getFolderPath() {
        if (folder == null) return List.of();
        try {
            return List.of(folder.getURLName().toString());
        } catch (MessagingException cause) {
            String errorMessage = Messages.SERVER_ERROR + Constants.COLON + cause.getMessage();
            throw new ServerErrorException(cause.getClass().getName() + "| " + errorMessage, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<String> getChildFolderNames() {
        throw new NotSupportedException("Get Child Folder Names");
    }

    @Override
    public Folder getChildFolderByName(String childFolderName) {
        if (folder == null) return null;
        try {
            javax.mail.Folder childFolder = folder.getFolder(childFolderName);
            if (childFolder == null) return null;
            return new FolderImpl(childFolder);
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get child folder for the email folder ", cause);
        }
    }

    @Override
    public Folder getChildFolderByName(List<String> childFolderPath) {
        throw new NotSupportedException("Get Child Folder By Name");
    }

    @Override
    public List<String> getChildFolderIds() {
        throw new NotSupportedException("Get Child Folder Ids");
    }

    @Override
    public Folder getChildFolder(String childFolderId) {
        return getChildFolderByName(childFolderId);
    }

    /**
     * Returns 'Unread' emails
     *
     * @param pageNumber
     * @param pageSize
     * @return
     */
    @Override
    public List<Email> getEmails(Double pageNumber, Double pageSize, KristaMediaClient kristaMediaClient) {
        return getEmails(pageNumber, pageSize, null, kristaMediaClient);
    }

    @Override
    public void moveInto(Folder folder) {
        throw new NotSupportedException("Move Folder");
    }

    @Override
    public int getMessageCount() {
        try {
            return folder.getMessageCount();
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get message count for the email folder ", cause);
        }
    }

    @Override
    public int getUnreadMessageCount() {
        try {
            return folder.getUnreadMessageCount();
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get unread message count for the email folder ", cause);
        }
    }

    @Override
    public List<Email> getUnreadEmails(Double pageNumber, Double pageSize, KristaMediaClient kristaMediaClient) {
        return getEmails(pageNumber, pageSize, Flags.Flag.SEEN, kristaMediaClient);
    }

    @Override
    public List<String> moveMessage(Message[] msgs, Folder targetFolder) throws MessagingException {
        List<String> messageIds;
        synchronized (this.folder) {
            IMAPFolder sourceFolder = (IMAPFolder) this.folder;
            openFolder(sourceFolder);
            openFolder(targetFolder.getJavaMailFolder());
            messageIds = getMessageIds(msgs);
            sourceFolder.moveMessages(msgs, targetFolder.getJavaMailFolder());
            sourceFolder.close(true);
            targetFolder.getJavaMailFolder().close(false);
        }
        return messageIds;
    }

    private void openFolder(javax.mail.Folder sourceFolder) throws MessagingException {
        if (!sourceFolder.isOpen())
            sourceFolder.open(javax.mail.Folder.READ_WRITE);
    }

    private List<String> getMessageIds(Message[] msgs) {
        return Arrays.stream(msgs).map(msg -> {
            try {
                return ((MimeMessage) msg).getMessageID();
            } catch (MessagingException cause) {
                logger.error("While getting message id, failure occurred from server: {}", cause.getMessage());
                return Constants.EMPTY_STRING;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public Message[] search(String[] messageIds) throws MessagingException {
        open();
        long[] msgIds = Arrays.stream(messageIds).map(Long::valueOf).mapToLong(Long::longValue).toArray();
        Message[] messageByUID = ((UIDFolder) folder).getMessagesByUID(msgIds);
        return !Objects.isNull(messageByUID) ? messageByUID : null;
    }

    @Override
    public Message search(String messageId) throws MessagingException {
        open();
        long[] msgIds = Stream.of(messageId).map(Long::valueOf).mapToLong(Long::longValue).toArray();
        Message[] messageByUID = ((UIDFolder) folder).getMessagesByUID(msgIds);
        return !Objects.isNull(messageByUID) ? messageByUID[0] : null;
    }

    @Override
    public javax.mail.Folder getJavaMailFolder() {
        return folder;
    }

    @Override
    public void open() {
        try {
            if (!folder.isOpen()) {
                folder.open(javax.mail.Folder.READ_ONLY);
            }
        } catch (MessagingException cause) {
            logger.error(cause.getMessage(), cause);
            logger.error(Messages.SERVER_ERROR + " {}", cause.getMessage());
        }
    }

    @Override
    public synchronized javax.mail.Folder[] listFolders() throws MessagingException {
        if (Objects.isNull(allFolders)) {
            allFolders = list();
        }
        return allFolders;
    }

    @Override
    public Message[] search(long timeInMillis) {
        SearchTerm searchTerm = new ReceivedDateTerm(ComparisonTerm.GE, new Date(timeInMillis));
        open();
        return ((IMAPFolderImpl) folder).search(searchTerm, timeInMillis);
    }

    private javax.mail.Folder[] list() throws MessagingException {
        try {
            return folder.list();
        } catch (StoreClosedException cause) {
            folder.getStore().connect();
            return folder.list();
        }
    }

    private List<Email> getEmails(Double pageNumber, Double pageSize, Flags.Flag flags,
                                  KristaMediaClient kristaMediaClient) {
        if (folder == null) return List.of();
        if (pageNumber == null) {
            pageNumber = (double) DEFAULT_PAGE_NUMBER;
        } else if (pageNumber < 1) {
            throw new IllegalArgumentException("Page Number should be greater than or equal to "
                    + DEFAULT_PAGE_NUMBER);
        }
        if (pageSize == null) {
            pageSize = (double) DEFAULT_PAGE_SIZE;
        }
        try {
            Message[] messages = getMessages(flags);
            List<Email> emails = getEmails(messages, kristaMediaClient);
            return getPaginatedData(pageNumber, pageSize, emails);
        } catch (MessagingException cause) {
            throw new IllegalStateException("Failed to get messages of the email folder ", cause);
        }
    }

    private List<Email> getPaginatedData(Double pageNumber, Double pageSize, List<Email> emails) {
        int start = (int) ((pageNumber - 1) * pageSize);
        int end = (int) (start + pageSize);
        logger.info("Start Index : {} End Index : {} emails.size {}", start, end, emails.size());
        if (emails.size() <= start) {
            return List.of();
        }
        if (emails.size() <= end) {
            return emails.subList(start, emails.size());
        }
        return emails.subList(start, end);
    }

    private List<Email> getEmails(Message[] messages, KristaMediaClient kristaMediaClient) {
        List<Email> emails = new ArrayList<>();
        if (messages == null || messages.length == 0) {
            return emails;
        }
        logger.info("All emails count: {}", messages.length);
        emails = Arrays.stream(messages).map(message -> new EmailImpl(message, kristaMediaClient)).collect(Collectors.toList());
        Collections.reverse(emails);
        return emails;
    }

    private Message[] getMessages(Flags.Flag flags) throws MessagingException {
        Message[] messages = null;
        if (flags != null) {
            messages = folder.search(new FlagTerm(new Flags(flags), false));
        } else {
            messages = folder.getMessages();
        }
        return messages;
    }
}
