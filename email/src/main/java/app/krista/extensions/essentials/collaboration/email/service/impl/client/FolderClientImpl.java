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

import app.krista.extensions.essentials.collaboration.email.impl.FolderImpl;
import app.krista.extensions.essentials.collaboration.email.service.FolderClient;
import app.krista.extensions.essentials.collaboration.email.service.impl.authclients.AuthClient;
import app.krista.extensions.essentials.collaboration.email.util.Constants;
import app.krista.extensions.essentials.collaboration.email.util.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FolderClientImpl implements FolderClient {
    private static final Logger logger = LoggerFactory.getLogger(FolderClientImpl.class);
    private static final String INBOX = "inbox";
    private static final String SENT = "sent items";
    private Store store;
    private final AuthClient authClient;
    private static Map<String, Folder> folderMap = new HashMap<>();

    public FolderClientImpl(AuthClient client) {
        this.authClient = client;
        store = getValidStore(client);
    }

    private Store getValidStore(AuthClient client) {
        if (store == null) {
            store = client.sessionConnect();
            resetFolderMap();
        }
        return store;
    }

    private void resetFolderMap() {
        folderMap.clear();
    }

    @Override
    public app.krista.extensions.essentials.collaboration.email.service.Folder getSentFolder() {
        return getFolder(SENT);
    }

    @Override
    public app.krista.extensions.essentials.collaboration.email.service.Folder getInboxFolder() {
        return getFolder(INBOX);
    }

    @Override
    public app.krista.extensions.essentials.collaboration.email.service.Folder getFolder(String name) {
        if (!store.isConnected()) {
            logger.info("store has disconnected, reconnecting store");
            store = authClient.sessionConnect();
            Map<String, Folder> tempFolderMap = folderMap;
            for(String folderName : tempFolderMap.keySet()) {
                folderMap.put(folderName, getFolderByName(folderName));
            }
        }
        Folder folder = folderMap.get(name);
        if (Objects.isNull(folder)) {
            folder = getFolderByName(name);
            folderMap.put(name, folder);
        }
        return new FolderImpl(folder);
    }

    private Folder getFolderByName(String name) {
        try {
            Folder folder = store.getFolder(name);
            if(!folder.exists()) {
                throw new IllegalArgumentException("Folder '" + name + "' does not exist in your mailbox. Please verify the folder name is correct and the folder exists in your email account.");
            }
            return folder;
        } catch (MessagingException cause) {
            throw new ServerErrorException(Messages.SERVER_ERROR + Constants.COLON + cause.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
