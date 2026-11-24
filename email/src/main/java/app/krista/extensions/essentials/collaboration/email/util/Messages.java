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

package app.krista.extensions.essentials.collaboration.email.util;

/**
 * Util class represents the messages defined for different use-cases.
 */
public class Messages {
    /**
     * private constructor to prevent creating object of this class.
     */
    private Messages() {

    }

    /**
     * value of FOLDER_DOES_NOT_EXISTS is {@value}
     */
    public static final String FOLDER_DOES_NOT_EXISTS = "The specified folder does not exist. Please verify the folder name and try again.";
    /**
     * value of UNMATCHED_SUBJECT is {@value}
     */
    public static final String UNMATCHED_SUBJECT = "No emails found matching the specified subject filter.";
    /**
     * value of UNMATCHED_FOLDER is {@value}
     */
    public static final String UNMATCHED_FOLDER = "No emails found in the specified folder";
    /**
     * value of UNMATCHED_SENDER is {@value}
     */
    public static final String UNMATCHED_SENDER = "No emails found from the specified sender.";

    /**
     * value of SERVER_ERROR is {@value}
     */
    public static final String SERVER_ERROR = "Server error occurred";
    /**
     * value of SERVER_MONITORING_STOPPED is {@value}
     */
    public static final String SERVER_MONITORING_STOPPED = "Email monitoring has stopped due to: ";

    /**
     * value of MAIL_MOVED_SUCCESSFULLY is {@value}
     */
    public static final String MAIL_MOVED_SUCCESSFULLY = "Email moved to desired folder.";
    /**
     * value of MAIL_MOVEMENT_FAILED is {@value}
     */
    public static final String MAIL_MOVEMENT_FAILED = "Failed to move email: ";
    /**
     * value of CONNECTION_RESET is {@value}
     */
    public static final String CONNECTION_RESET = "Connection reset";

    public static String decodeMessageId(String messageID) {
        if (messageID.startsWith("&lt;") && messageID.endsWith("&gt;")) {
            return messageID.replace("&lt;", "<").replace("&gt;", ">");
        }
        return messageID;
    }

    public static String encodeMessageId(String messageID) {
        if (messageID.startsWith("<") && messageID.endsWith(">")) {
            return messageID.replace("<", "&lt;").replace(">", "&gt;");
        }
        return messageID;
    }
}
