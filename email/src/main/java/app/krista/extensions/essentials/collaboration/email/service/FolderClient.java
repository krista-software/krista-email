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

/**
 * Interface for a folder client that provides access to email folders.
 * <p>
 * This interface exposes methods to get common folders like Inbox and Sent,
 * as well as get any folder by name.
 */
public interface FolderClient {
    /**
     * Gets the Sent folder.
     *
     * @return the Sent Folder object
     */
    Folder getSentFolder();

    /**
     * Gets the Inbox folder.
     *
     * @return the Inbox Folder object
     */
    Folder getInboxFolder();

    /**
     * Gets a specific folder by name.
     *
     * @param name the name of the folder to retrieve
     * @return the Folder object for the given name
     */
    Folder getFolder(String name);
}
