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

package app.krista.extensions.essentials.collaboration.email.service.impl.client;

import app.krista.extension.authorization.MustAuthorizeException;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ClientErrorException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MediaClient {

    private static final Logger logger = LoggerFactory.getLogger(MediaClient.class);

    public static final String TEMP_DIR = "/tmp/krista/extension/email/attachments/";
    private static final String MEDIA_HOST = "http://platform:9991";
    private final OkHttpClient client;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public MediaClient() {
        client = new OkHttpClient();
        new File(TEMP_DIR).mkdirs();
    }

    public File download(String url, String fileName) throws IOException {
        Request request = new Request.Builder()
                .url(MEDIA_HOST + url)
                .build();
        Response response = client.newCall(request).execute();
        if (handleResponse(response)) {
            return handleFileDownload(response, fileName);
        }
        response.close();
        return null;
    }

    private static boolean handleResponse(Response response) throws IOException {
        if (response.isSuccessful())
            return true;
        else if (response.code() == javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode()) {
            logger.error("Got error for Authentication. So sending for re authentication.");
            throw new MustAuthorizeException("AuthException : Unauthorized exception");
        } else if (response.code() == javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode()) {
            return false;
        } else {
            assert response.body() != null;
            throw new ClientErrorException(javax.ws.rs.core.Response.Status.valueOf("ClientError: " + response.body().string()));
        }
    }

    private static File handleFileDownload(Response response, String fileName) throws IOException {
        assert response.body() != null;
        InputStream is = response.body().byteStream();
        File file = new File(TEMP_DIR + fileName);
        Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        is.close();
        return file;
    }
}
