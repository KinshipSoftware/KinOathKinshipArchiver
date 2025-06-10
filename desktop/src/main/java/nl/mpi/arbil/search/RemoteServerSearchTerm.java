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
package nl.mpi.arbil.search;

import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 * Interface for remote server search term, to be used with MDSearch service
 * Results will need further processing. Filtering can be done by supplying ArbilNodeSearchTerms
 * @see nl.mpi.arbil.search.ArbilSearch
 * @see nl.mpi.arbil.search.ArbilNodeSearchTerm
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface RemoteServerSearchTerm {

    static final String IMDI_RESULT_URL_XPATH = "/ImdiSearchResponse/Result/Match/URL";
    static final String IMDI_SEARCH_BASE = "http://corpus1.mpi.nl/ds/imdi_search/servlet?action=getMatches";
    static final String valueFieldMessage = "<remote server search term (required)>";
    
    /**
     * 
     * @param searchNodes Nodes to search in
     * @return Results of the MDSearch call for the given nodes
     */
    URI[] getServerSearchResults(ArbilDataNode[] searchNodes);
    
}
