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

import java.util.Objects;

public class Strings {
    public static final String SPACE = " ";
    public static final String SEMI_COLON = ";";
    public static final String SEMI_COLON_WITH_SPACE = "; ";

    private Strings() {

    }

    public static boolean isNullOrBlank(String obj) {
        return Objects.isNull(obj) || obj.isBlank();
    }

    public static boolean isObjectNullOrBlank(Object obj) {
        return Objects.isNull(obj) || String.valueOf(obj).isBlank();
    }

    public static boolean isEqual(String obj1, String obj2) {
        return !Objects.isNull(obj1) && obj1.equalsIgnoreCase(obj2);
    }
}
