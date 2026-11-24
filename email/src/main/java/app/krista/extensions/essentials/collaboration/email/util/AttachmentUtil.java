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

import java.util.UUID;

/**
 * Helper class to upload and download media files received and sent via the email extension.
 */
public final class AttachmentUtil {

    private AttachmentUtil() {
        throw new IllegalStateException("Invalid access to AttachmentUtil");
    }

    /**
     * Returns the file name by appending predefined prefix, followed by random UUID and
     * at last file extension. e.g.:- doc_binary_random-UUID.txt
     * @param fileName actual media file name used to get file extension.
     *                 If not present default extension .txt is appended
     * @return {@link String} media id with predefined prefix, followed by random UUID and file extension.
     */
    public static String getMediaId(String fileName) {
        String fileExtension = getFileExtension(fileName);

        // when file does not contain any extension at the end
        if (fileExtension.equals(fileName)) {
            fileExtension = Constants.DEFAULT_FILE_EXTENSION;
        }
        return Constants.MEDIA_FILE_PREFIX + UUID.randomUUID() + Constants.DOT + fileExtension;
    }

    /**
     * Helper method to get file extension.
     *
     * @param fileName to get file extension if present
     * @return {@link String} file extension suffix e.g.:- jpeg.
     */
    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(Constants.DOT) + 1);
    }

    /**
     * Helps in creating media url to upload media files attached to email sent via email extension.
     *
     * @param mediaId used to append with predefined MEDIA_URI
     * @return {@link String} media URI.
     */
    public static String createMediaUrl(String mediaId) {
        return Constants.MEDIA_URI_PREFIX + mediaId;
    }
}
