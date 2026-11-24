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

package app.krista.extensions.essentials.collaboration.email.service.impl.imap;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.SearchSequence;
import com.sun.mail.util.MailLogger;

import java.io.IOException;
import java.util.Properties;

public class IMAPProtocolImpl extends IMAPProtocol {
    public IMAPProtocolImpl(String name, String host, int port, Properties props, boolean isSSL, MailLogger logger) throws IOException, ProtocolException {
        super(name, host, port, props, isSSL, logger);
    }

    @Override
    protected SearchSequence getSearchSequence() {
        if (searchSequence == null)
            searchSequence = new SearchSequenceImpl(this);
        return searchSequence;
    }
}
