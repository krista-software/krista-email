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

package app.krista.extensions.essentials.collaboration.email.impl;

import app.krista.extensions.essentials.collaboration.email.service.KristaMediaClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttachmentImpl.
 */
@ExtendWith(MockitoExtension.class)
class AttachmentImplTest {

    @Mock
    private BodyPart mockBodyPart;

    @Mock
    private KristaMediaClient mockKristaMediaClient;

    private AttachmentImpl attachmentImpl;

    @BeforeEach
    void setUp() {
        attachmentImpl = new AttachmentImpl(mockBodyPart, mockKristaMediaClient);
    }

    @Test
    void getName_returnsFileName() throws MessagingException {
        when(mockBodyPart.getFileName()).thenReturn("report.pdf");
        assertEquals("report.pdf", attachmentImpl.getName());
    }

    @Test
    void getPath_returnsNull() {
        assertNull(attachmentImpl.getPath());
    }

    @Test
    void getSize_withNonZeroSize_returnsSize() throws MessagingException {
        when(mockBodyPart.getSize()).thenReturn(1024);
        assertEquals(1024, attachmentImpl.getSize());
    }

    @Test
    void getSize_withZeroSize_returnsZero() throws MessagingException {
        when(mockBodyPart.getSize()).thenReturn(0);
        assertEquals(0, attachmentImpl.getSize());
    }

    @Test
    void getMimeType_returnsContentType() throws MessagingException {
        when(mockBodyPart.getContentType()).thenReturn("application/pdf");
        assertEquals("application/pdf", attachmentImpl.getMimeType());
    }
}