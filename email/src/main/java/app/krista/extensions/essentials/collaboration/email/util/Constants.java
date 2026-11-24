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

import app.krista.model.field.util.GsonJsonMapper;

import java.util.List;

public class Constants {

    public static final String LAST_FETCH_MAILS_REQUEST_TIME_KEY = "LastFetchMailsRequestTime";

    private Constants() {
    }

    public static final String COMMA = ",";
    public static final String SEMI_COLON = ";";
    public static final String FOLDER_NAME = "Folder Name";
    public static final String MESSAGE_ID = "messageId";
    public static final String FOLDER_TO_MONITOR = "folderToMonitor";
    public static final String TEXT = "Text";
    public static final String MAIL_RECEIVED = "mailReceived";
    public static final String MAIL_RECEIVED_EVENT_NAME = "Mail Received Alert";
    public static final String EMPTY_STRING = "";
    public static final String COLON = ": ";
    public static final String GMAIL_IMAP_SERVER = "imap.gmail.com";
    public static final String GMAIL_SMTP_SERVER = "smtp.gmail.com";
    public static final String GMAIL_DEFAULT_SCOPE = "https://mail.google.com";
    public static final List<String> OUTLOOK_IMAP_SERVER = List.of("outlook.office365.com", "imap-legacy.office365.com");
    public static final String OUTLOOK_SMTP_SERVER = "smtp.office365.com";
    public static final String OUTLOOK_DEFAULT_SCOPE = "https://outlook.office365.com/.default";
    public static final String CALLBACK_URI = "/rest/email/callback";
    public static final String REFRESH_TOKEN_SUFFIX = "_RefreshToken";
    public static final int IMAP_PORT = 993;
    public static final String XOAUTH2 = "XOAUTH2";
    public static final GsonJsonMapper GSON_JSON_MAPPER = GsonJsonMapper.create();
    public static final String EMAIL_ATTRIBUTES_KEY = "EmailAttributes";
    public static final String VALIDATE_REQUEST_KEY = "isFirstRequest";
    public static final String DOT = ".";
    public static final String MEDIA_FILE_PREFIX = "doc_binary_";
    public static final String DEFAULT_FILE_EXTENSION = "txt";
    public static final String MEDIA_URI_PREFIX = "/media/public/";
    public static final String GOOGLE_OAUTH2_PROVIDER_NAME = "Google OAuth2 Provider";
    public static final long MAX_ATTACHMENT_SIZE_SUPPORTED_BY_GMAIL_IN_BYTES = 26214400L;
    public static final long MAX_ATTACHMENT_SIZE_SUPPORTED_BY_OUTLOOK_IN_BYTES = 20971520L;
    public static final long ONE_MB_TO_BYTE = 1048576L;
}
