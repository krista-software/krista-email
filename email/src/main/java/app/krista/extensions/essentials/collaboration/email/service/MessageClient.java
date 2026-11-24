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
 * Interface for a message client to manage email messages.
 * <p>
 * Provides methods for moving messages between folders
 * and resetting the scheduler service.
 */
public interface MessageClient {

    /**
     * Enqueues a message to be moved to the specified folder.
     *
     * @param folderName the name of the folder to move the message to
     * @param messageId  the ID of the message to move
     * @return a task ID for the move operation
     */
    String enqueueMessageForMove(String folderName, String messageId);

}
