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

package app.krista.extensions.essentials.collaboration.email.service.impl.client;

import app.krista.extensions.essentials.collaboration.email.service.MessageClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.imap.IMAPStoreImpl;
import com.sun.mail.imap.IMAPFolder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.search.HeaderTerm;
import javax.mail.search.OrTerm;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MessageClientImpl implements MessageClient {

    private static final Logger logger = LoggerFactory.getLogger(MessageClientImpl.class);

    private final Store store;
    private static final LinkedBlockingQueue<Map.Entry<String, String>> messageIdQueue = new LinkedBlockingQueue<>();
    private static ScheduledThreadPoolExecutor executorService = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);

    public MessageClientImpl(AuthClient authClient) {
        this.store = authClient.sessionConnect();
        executorService.scheduleWithFixedDelay(this::moveMessage, 0, 1L, TimeUnit.MINUTES);
    }

    private void openFolder(javax.mail.Folder folder) throws MessagingException {
        if (!folder.isOpen()) {
            folder.open(Folder.READ_WRITE);
        }
    }

    @Override
    public String enqueueMessageForMove(String folderName, String messageId) {
        try {
            // Validate message ID exists
            Map<String, String> messageIdsWithTargetFolder = Map.of(messageId, folderName);
            Map<String, List<Message>> targetFolderWithMessages = getAllMessages(messageIdsWithTargetFolder);
            if (targetFolderWithMessages.isEmpty()) {
                throw new IllegalArgumentException("Message with ID '" + messageId + "' not found");
            }

            // Validate target folder exists
            ensureStoreIsConnected();
            Folder targetFolder = store.getFolder(folderName);
            if (!targetFolder.exists()) {
                throw new IllegalArgumentException("Folder '" + folderName + "' not found");
            }

            messageIdQueue.put(Map.entry(messageId, folderName));
            logger.info("Message is enqueued for movement and will be process shortly");
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            logger.info(cause.getMessage(), cause);
        } catch (MessagingException cause) {
            throw new IllegalArgumentException(cause.getMessage(), cause);
        }
        return messageId;
    }

    /**
     * Shuts down the current scheduler thread pool and creates a new one.
     * <p>
     * This method allows forcibly shutting down the {@link ScheduledThreadPoolExecutor}
     * used for running recurring message move tasks. It waits for termination,
     * then re-initializes the thread pool.
     * <p>
     * This can be used to reset the scheduler if needed, such as during testing
     * or after errors.
     */
    @SuppressWarnings("all")
    public static synchronized void resetSchedulerExecutorService() {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException cause) {
            logger.error("failed to terminate thread", cause);
            Thread.currentThread().interrupt();
        }
        if (executorService.isTerminating() || executorService.isTerminated()) {
            executorService = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        }
    }

    /**
     * This operation used by {@link #executorService} to move message ids.
     */
    private void moveMessage() {
        Map<String, String> messageIdsWithTargetFolder = new HashMap<>();
        while (!messageIdQueue.isEmpty()) {
            Map.Entry<String, String> entry = messageIdQueue.poll();
            messageIdsWithTargetFolder.put(entry.getKey(), entry.getValue());
        }
        try {
            moveMessage(messageIdsWithTargetFolder);
        } catch (MessagingException cause) {
            logger.error(cause.getMessage(), cause);
        }
    }

    private void moveMessage(Map<String, String> messageIdsWithTargetFolder) throws MessagingException {
        Map<String, List<Message>> targetFolderWithMessages = getAllMessages(messageIdsWithTargetFolder);
        if (targetFolderWithMessages.isEmpty()) {
            return;
        }
        for (Map.Entry<String, List<Message>> messages : targetFolderWithMessages.entrySet()) {
            IMAPFolder sourceFolder = (IMAPFolder) messages.getValue().get(0).getFolder();
            Folder targetFolder = null;
            try {
                targetFolder = store.getFolder(messages.getKey());
                openFolder(sourceFolder);
                logger.info("copying messages: {}", messages.getValue());
                openFolder(targetFolder);
                sourceFolder.copyMessages(messages.getValue().toArray(new Message[0]), targetFolder);
                messages.getValue().forEach(msg -> {
                    try {
                        msg.setFlag(Flags.Flag.DELETED, true);
                    } catch (MessagingException e) {
                        logger.error(e.getMessage(), e);
                    }
                });
            } catch (FolderNotFoundException cause) {
                logger.error("Folder: {}", cause.getMessage());
            } catch (MessagingException cause) {
                logger.error("MessageIds are failed to move to the targeted folder due to failure occurred in the process. It will be retried in some time");
                pushBackToQueue(messageIdsWithTargetFolder);
                logger.error(cause.getMessage());
            } finally {
                sourceFolder.expunge();
                sourceFolder.close(true);
                assert targetFolder != null;
                targetFolder.close(false);
            }
        }
    }

    private void pushBackToQueue(Map<String, String> messageIdsWithTargetFolder) {
        messageIdsWithTargetFolder.forEach((key, value) -> {
            try {
                messageIdQueue.put(Map.entry(key, value));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private Map<String, List<Message>> getAllMessages(Map<String, String> messageIdsWithTargetFolder) throws MessagingException {
        Map<String, List<Message>> messagesWithTargetFolder = new HashMap<>();
        Map<String, List<String>> targetFoldersWithMessageIds = messageIdsWithTargetFolder.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,                 // Key becomes the new key
                        entry -> Collections.singletonList(entry.getKey()),  // Create a List with the key as the value
                        (list1, list2) -> {                 // Merge lists if values collide
                            List<String> merged = new ArrayList<>(list1);
                            merged.addAll(list2);
                            return merged;
                        }
                ));

        for (Map.Entry<String, List<String>> targetFolderWithMessageId : targetFoldersWithMessageIds.entrySet()) {
            List<Message> messages = searchMessages(targetFolderWithMessageId.getValue());
            if (!messages.isEmpty()) {
                messagesWithTargetFolder.put(targetFolderWithMessageId.getKey(), messages);
            }
        }
        return messagesWithTargetFolder;
    }

    private List<Message> searchMessages(List<String> keySet) throws MessagingException {
        List<List<String>> setsOfFive = breakKeySetIntoSetOfFive(keySet);
        /**
         * To prevent a "BAD Request" error when performing IMAP searches,
         * we split the list of message IDs into sets of five to apply the search in smaller, manageable batches.
         */
        List<List<HeaderTerm>> searchTerms = setsOfFive
                .stream()
                .map(setOfFive -> setOfFive
                        .stream()
                        .map(msg -> new HeaderTerm("Message-ID", msg))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        ensureStoreIsConnected();
        javax.mail.Folder[] list = ((IMAPStoreImpl) store).listAllFolders();
        return findMessages(searchTerms, list);
    }

    @NotNull
    private List<Message> findMessages(List<List<HeaderTerm>> searchTerms, Folder[] list) throws MessagingException {
        List<Message> messages = new ArrayList<>();
        for (Folder folder : list) {
            openFolder(folder);
            for (List<HeaderTerm> terms : searchTerms) {
                OrTerm term = new OrTerm(terms.toArray(new HeaderTerm[0]));
                Message[] searchResults = folder.search(term);
                if (!Objects.isNull(searchResults) && searchResults.length > 0) {
                    messages.addAll(Arrays.asList(searchResults));
                }
            }
            if (!messages.isEmpty()) {
                folder.close(false);
                return messages;
            }
            folder.close(false);
        }
        return List.of();
    }

    private List<List<String>> breakKeySetIntoSetOfFive(List<String> keySet) {
        List<List<String>> setOfFives = new ArrayList<>();
        List<String> currentSet = new LinkedList<>();
        for (String key : keySet) {
            if (currentSet.size() == 4) {
                currentSet.add(key);
                setOfFives.add(new ArrayList<>(currentSet));
                currentSet = new LinkedList<>();
            } else {
                currentSet.add(key);
            }
        }
        if (!currentSet.isEmpty()) {
            setOfFives.add(currentSet);
        }
        return setOfFives;
    }

    private void ensureStoreIsConnected() throws MessagingException {
        logger.debug("store is connected {}", store.isConnected());
        if (!store.isConnected()) {
            store.connect();
            logger.debug("store is connected {}", store.isConnected());
        }
    }
}
