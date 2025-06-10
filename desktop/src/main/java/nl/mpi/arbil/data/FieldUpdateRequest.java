/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.util.Map;

/**
 *  Document   : FieldUpdateRequest
 *  Created on : May 21, 2010, 11:52:47 AM
 *  Author     : Peter Withers
 */
public class FieldUpdateRequest {

    public String fieldPath;
    public String fieldOldValue;
    public String fieldNewValue;
    public String keyNameValue;
    public String fieldLanguageId;
    public Map<String, Object> attributeValuesMap;
}
