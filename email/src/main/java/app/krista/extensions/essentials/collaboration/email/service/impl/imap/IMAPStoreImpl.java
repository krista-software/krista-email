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

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.imap.protocol.IMAPProtocol;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Objects;

public class IMAPStoreImpl extends IMAPSSLStore {
    private Folder[] folders;

    /**
     * Custom IMAP store implementation that extends the IMAPSSLStore.
     * <p>
     * Provides additional functionality:
     * </p>
     * - Overrides newIMAPFolder() to return a custom IMAPFolderImpl
     * - Overrides newIMAPProtocol() to return a custom IMAPProtocolImpl
     * - Maintains a queue of folder names to monitor
     *
     * @param session         the JavaMail Session
     * @param defaultProtocol the default protocol name
     * @throws NoSuchProviderException if the protocol is not found
     */
    public IMAPStoreImpl(Session session, String defaultProtocol) throws NoSuchProviderException {
        super(session, session.getStore(defaultProtocol).getURLName());
    }

    @Override
    protected IMAPFolder newIMAPFolder(String fullName, char separator, Boolean isNamespace) {
        IMAPFolder folder = super.newIMAPFolder(fullName, separator, isNamespace);
        try {
            return new IMAPFolderImpl(folder);
        } catch (MessagingException cause) {
            throw new ServerErrorException("Error creating IMAPFolderImpl", Response.Status.INTERNAL_SERVER_ERROR, cause.getCause());
        }
    }

    @Override
    protected IMAPProtocol newIMAPProtocol(String host, int port) throws IOException, ProtocolException {
        return new IMAPProtocolImpl(name, host, port,
                session.getProperties(),
                isSSL,
                logger);
    }

    public Session getCurrentSession() {
        return session;
    }

    /**
     * Gets the host name used by this store.
     *
     * @return the host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the user name (email) used by this store.
     *
     * @return the user name
     */
    public String getUser() {
        return user;
    }

    /**
     * Lists all available folders for this store.
     *
     * @return array of Folder objects representing the folder hierarchy
     * @throws MessagingException if there is an error listing folders
     */
    public Folder[] listAllFolders() throws MessagingException {
        Folder defaultFolder = getDefaultFolder();
        if (Objects.isNull(folders)) {
            folders = defaultFolder.list();
        }
        return folders;
    }
}
