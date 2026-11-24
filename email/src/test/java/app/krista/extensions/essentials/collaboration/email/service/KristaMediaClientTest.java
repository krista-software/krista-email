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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the pure/static methods of KristaMediaClient that don't require
 * the injected FileRepository.
 */
class KristaMediaClientTest {

    // ---- createMediaUrl ----

    @Test
    void createMediaUrl_returnsCorrectPath() {
        KristaMediaClient client = new KristaMediaClient();
        String url = client.createMediaUrl("ws-123", "media-456");
        assertEquals("/media/workspaces/ws-123/media-456", url);
    }

    @Test
    void createMediaUrl_withEmptyWorkspaceId_returnsPathWithEmptySegment() {
        KristaMediaClient client = new KristaMediaClient();
        String url = client.createMediaUrl("", "media-id");
        assertEquals("/media/workspaces//media-id", url);
    }

    // ---- compressFile (static) ----

    @Test
    void compressFile_createsZipFileAtGivenPath(@TempDir Path tempDir) throws IOException {
        // Create a real source file to compress
        Path sourceFile = tempDir.resolve("test.txt");
        Files.writeString(sourceFile, "Hello, compression!");

        Path zipFile = tempDir.resolve("output.zip");

        KristaMediaClient.compressFile(zipFile.toString(), sourceFile.toString());

        assertTrue(Files.exists(zipFile), "Zip file should be created");
        assertTrue(Files.size(zipFile) > 0, "Zip file should not be empty");
    }

    @Test
    void compressFile_zipContainsEntry(@TempDir Path tempDir) throws IOException {
        Path sourceFile = tempDir.resolve("data.csv");
        Files.writeString(sourceFile, "id,name\n1,test");
        Path zipPath = tempDir.resolve("data.zip");

        KristaMediaClient.compressFile(zipPath.toString(), sourceFile.toString());

        // Verify it's a valid zip by reading its header bytes (PK magic bytes)
        byte[] header = Files.readAllBytes(zipPath);
        assertEquals(0x50, header[0] & 0xFF, "First byte should be 'P'");
        assertEquals(0x4B, header[1] & 0xFF, "Second byte should be 'K'");
    }

    @Test
    void compressFile_nonExistentSource_throwsException(@TempDir Path tempDir) {
        Path zipFile = tempDir.resolve("out.zip");
        assertThrows(IOException.class,
                () -> KristaMediaClient.compressFile(zipFile.toString(), "/nonexistent/file.txt"));
    }
}
