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

package app.krista.extensions.essentials.collaboration.email.service.impl;

import org.jvnet.hk2.annotations.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class EmailAttributesChangeDetector {
    private boolean isToUpdateFolderClient;
    private boolean isToUpdateMessageClient;
    private boolean isToUpdateSmtpClient;

    public void isChangeDetected(Map<String, Object> attributes, Map<String, Object> savedInvokerAttributes) {
        if (savedInvokerAttributes.keySet().stream().anyMatch(key -> !Objects.equals(attributes.get(key), savedInvokerAttributes.get(key)))) {
            isToUpdateFolderClient = true;
            isToUpdateMessageClient = true;
            isToUpdateSmtpClient = true;
        }
    }

    public boolean isToUpdateFolderClient() {
        return isToUpdateFolderClient;
    }

    public boolean isToUpdateMessageClient() {
        return isToUpdateMessageClient;
    }

    public void setToUpdateFolderClient(boolean toUpdateFolderClient) {
        isToUpdateFolderClient = toUpdateFolderClient;
    }

    public void setToUpdateMessageClient(boolean toUpdateMessageClient) {
        isToUpdateMessageClient = toUpdateMessageClient;
    }

    public boolean isToUpdateSmtpClient() {
        return isToUpdateSmtpClient;
    }

    public void setToUpdateSmtpClient(boolean toUpdateSmtpClient) {
        isToUpdateSmtpClient = toUpdateSmtpClient;
    }
}
