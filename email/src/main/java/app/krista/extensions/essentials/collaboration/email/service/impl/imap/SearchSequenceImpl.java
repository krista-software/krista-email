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

import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.SearchSequence;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The SearchSequenceImpl class is a subclass of the SearchSequence class that provides a custom implementation for
 * converting a Date object to an IMAP date string.
 */
public class SearchSequenceImpl extends SearchSequence {

    /**
     * Constructor that takes an IMAPProtocol object as a parameter and calls the superclass constructor
     *
     * @param imapProtocol
     */
    public SearchSequenceImpl(IMAPProtocol imapProtocol) {
        super(imapProtocol);
    }

    /**
     * Converts the given Date object to a IMAP date string.
     * This overrides the default implementation in SearchSequence
     * to format the date in a custom format.
     *
     * @param date the Date object to convert
     * @return the date formatted as a string in dd-MMM-yyyy format
     */
    @Override
    protected String toIMAPDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        return simpleDateFormat.format(date);
    }
}
