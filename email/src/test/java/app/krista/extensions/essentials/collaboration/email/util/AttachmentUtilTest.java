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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class AttachmentUtilTest {

    @Test
    void getFileExtension_withSimpleExtension_returnsExtension() {
        assertEquals("pdf", AttachmentUtil.getFileExtension("document.pdf"));
    }

    @Test
    void getFileExtension_withMultipleDots_returnsLastExtension() {
        assertEquals("gz", AttachmentUtil.getFileExtension("archive.tar.gz"));
    }

    @Test
    void getFileExtension_withJpeg_returnsJpeg() {
        assertEquals("jpeg", AttachmentUtil.getFileExtension("photo.jpeg"));
    }

    @Test
    void getFileExtension_withTxtExtension_returnsTxt() {
        assertEquals("txt", AttachmentUtil.getFileExtension("notes.txt"));
    }

    @Test
    void getMediaId_withExtension_startsWithPrefixAndEndsWithExtension() {
        String mediaId = AttachmentUtil.getMediaId("report.pdf");
        assertTrue(mediaId.startsWith(Constants.MEDIA_FILE_PREFIX));
        assertTrue(mediaId.endsWith(".pdf"));
    }

    @Test
    void getMediaId_withExtension_containsUuidSegment() {
        String mediaId = AttachmentUtil.getMediaId("file.docx");
        // Format: doc_binary_<UUID>.docx
        String withoutPrefix = mediaId.substring(Constants.MEDIA_FILE_PREFIX.length());
        assertTrue(withoutPrefix.contains("-")); // UUID has dashes
    }

    @Test
    void getMediaId_withNoExtension_usesDefaultExtension() {
        // filename has no dot -> fileExtension equals fileName -> use DEFAULT_FILE_EXTENSION
        String mediaId = AttachmentUtil.getMediaId("filename");
        assertTrue(mediaId.endsWith("." + Constants.DEFAULT_FILE_EXTENSION));
    }

    @Test
    void getMediaId_twoCallsProduceDifferentIds() {
        String id1 = AttachmentUtil.getMediaId("file.txt");
        String id2 = AttachmentUtil.getMediaId("file.txt");
        assertNotEquals(id1, id2);
    }

    @Test
    void createMediaUrl_returnsCorrectUrl() {
        String url = AttachmentUtil.createMediaUrl("doc_binary_abc.pdf");
        assertEquals(Constants.MEDIA_URI_PREFIX + "doc_binary_abc.pdf", url);
    }

    @Test
    void createMediaUrl_withArbitraryId_prependsPrefix() {
        String mediaId = "someId";
        assertEquals(Constants.MEDIA_URI_PREFIX + mediaId, AttachmentUtil.createMediaUrl(mediaId));
    }

    @Test
    void constructor_throwsIllegalStateException() throws Exception {
        Constructor<AttachmentUtil> constructor = AttachmentUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(IllegalStateException.class, ex.getCause());
    }
}