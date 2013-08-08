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
package nl.mpi.kinnate.kintypestrings;

/**
 *  Document   : ParserHighlight
 *  Created on : May 30, 2011, 3:06:02 PM
 *  Author     : Peter Withers
 */
public class ParserHighlight {

    public enum ParserHighlightType {

        KinType, Comment, Error, Query, Parameter, Message, Unknown
    }
    public ParserHighlight nextHighlight = null;
    public ParserHighlightType highlight;
    public int startChar = 0;
    public String tooltipText;

    public ParserHighlight addHighlight(ParserHighlightType highlightType, int startChar, String tooltipText) {

        this.highlight = highlightType;
        this.startChar = startChar;
        this.tooltipText = tooltipText;
        this.nextHighlight = new ParserHighlight();
        return this.nextHighlight;
    }
}
