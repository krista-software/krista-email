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

import app.krista.model.base.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.Message;
import javax.mail.Session;
import javax.ws.rs.ServerErrorException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Additional tests for MessageBuilder covering attachment paths.
 */
@ExtendWith(MockitoExtension.class)
class MessageBuilderAdditionalTest {

    @Mock
    private KristaMediaClient mockKristaMediaClient;

    @Mock
    private File mockFile;

    private Session session;

    @BeforeEach
    void setUp() {
        session = Session.getDefaultInstance(new Properties());
    }

    @Test
    void attachments_whenTotalSizeExceedsLimit_throwsIllegalArgumentException() {
        when(mockFile.getLength()).thenReturn(50 * 1024 * 1024L); // 50MB - exceeds 25MB limit
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        assertThrows(IllegalArgumentException.class,
                () -> builder.attachments(List.of(mockFile), 26214400L));
    }

    @Test
    void attachments_whenToJavaFileThrowsIOException_throwsServerErrorException() throws Exception {
        when(mockFile.getLength()).thenReturn(100L); // small size, within limit
        when(mockKristaMediaClient.toJavaFile(mockFile)).thenThrow(new IOException("IO error"));
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        assertThrows(ServerErrorException.class,
                () -> builder.attachments(List.of(mockFile), 26214400L));
    }

    @Test
    void attachments_withEmptyList_setsMultipartToNull_whenNoBody() {
        // When no body and empty attachment list, multipart becomes null → build() sets no content
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        Message msg = builder
                .subject("Test")
                .from("from@example.com")
                .to("to@example.com")
                .attachments(List.of(), 26214400L)
                .build();
        assertNotNull(msg);
    }

    @Test
    void messageBody_thenBuild_setsContent() throws Exception {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        Message msg = builder
                .subject("Test")
                .from("from@example.com")
                .to("to@example.com")
                .messageBody("Hello\nWorld")
                .attachments(null, 0L)
                .build();
        assertNotNull(msg);
        // Content is set (multipart with body part)
        assertNotNull(msg.getContent());
    }

    @Test
    void attachments_withNullList_andBody_multipartContainsBody() throws Exception {
        MessageBuilder builder = new MessageBuilder(session, mockKristaMediaClient);
        Message msg = builder
                .subject("Test")
                .from("from@example.com")
                .to("to@example.com")
                .messageBody("Body content")
                .attachments(null, 26214400L)
                .build();
        assertNotNull(msg);
        assertNotNull(msg.getContent());
    }
}
