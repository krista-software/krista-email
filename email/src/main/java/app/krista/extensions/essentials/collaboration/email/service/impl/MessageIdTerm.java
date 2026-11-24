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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.search.StringTerm;
import java.util.Objects;

public class MessageIdTerm extends StringTerm {

    private static final Logger logger = LoggerFactory.getLogger(MessageIdTerm.class);

    private final String messageId;

    public MessageIdTerm(String messageId) {
        super(messageId);
        this.messageId = messageId;
    }

    @Override
    public boolean match(Message msg) {
        try {
            return (Objects.equals(((MimeMessage) msg).getMessageID(), messageId));
        } catch (MessagingException cause) {
            logger.error("exception caught while matching messageId: {}", cause.getMessage());
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MessageIdTerm that = (MessageIdTerm) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), messageId);
    }
}
