<%--

    Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

--%>
<html>
    <body>
        <% String kinTypeString = "EmMD:1:|EmFD:1:|EmS:2:|EmWD:3:|EmD:3:|EmWS:2:|EmZ:1:"; %>
        <h2>KinOath Web Application Example Page</h2>
<!--        <b>Workspaces and Uploaded Files</b><br>
        <p>
            <a href="kinoath/kinspace">list work spaces</a>
        </p>-->
        <b>HTML output for viewing</b><br>
        <% String htmlUrlString = "kinoath/getkin/view?kts=" + kinTypeString; %>
        <p>
            <a href="<%= htmlUrlString %>"><%= htmlUrlString %></a>
        </p>
        <b>SVG output for viewing</b><br>
        <% String svgUrlString = "kinoath/getkin/svg?kts=" + kinTypeString; %>
        <p>
            <a href="<%= svgUrlString %>"><%= svgUrlString %></a>
        </p>

        <% String charlesTypeString = "?kts=Em:Charles II ;of Spain;1661-1700:&kts=EmW:Maria Anna ;of the Palatinate-Neuburg;1689-1700:&kts=EmW:Marie Louise d'Orléans;1679-1689:&kts=EmF:Philip IV ;of Spain;1605-1665:&kts=EmM:Mariana ;of&kts=Austria;1634-1696:&kts=EmMM:Maria Anna ;of Spain;1606-1646:&kts=EmFM:#1;Margaret ;of Austria;&kts=EmFF:#2;Philip III ;of Spain;1578-1621:&kts=EmMF:Ferdinand III; Holy Roman Emperor&kts=EmMFF:#5;Ferdinand II; Holy Roman Emperor:M:#3;Maria Anna ;of Bavaria:&kts=EmMMM:#1:&kts=EmMMF:#2:&kts=EmMFM:Maria Anna ;of Bavaria;1574-1616:&kts=EmMFMM:Renata ;of Lorraine&kts=EmMFMF:William V, ;Duke of Bavaria&kts=EmMMFF:Philip II ;of Spain&kts=EmMMFM:Anna ;of Austria&kts=EmMMMF:#4;Charles II ;of Austria:S:#5:&kts=EmMFMMF:Francis I, ;Duke of Lorraine&kts=EmMFMMM:Christina ;of Denmark&kts=EmMFMFF:#15;Albert V, ;Duke of Bavaria&kts=EmMMFMM:#10;Maria ;of Spain&kts=EmMFMMMF:Christian II ;of Denmark&kts=EmMFMMMM:#14;Isabella ;of Burgundy&kts=EmMFMFM:#8;Anne ;of Habsburg:&kts=EmMFMFMM:#6;Anne of Bohemia:S:#4&kts=EmMMFMF:#7;Maximilian II, ;Holy Roman Emperor:M:#6&kts=EmMMFMFF:#9;Ferdinand I, ;Holy Roman Emperor:S:#4&kts=x:#9:D:#8&kts=EmMMFFF:#11;Charles V, ;Holy Roman Emperor:&kts=EmMMFFM:Isabella ;of Portugal:D:#10&kts=x:#10:F:#11&kts=EmMMFFFF:#13;Philip I ;of Castile&kts=EmMMFFFM:#12;Joanna of Castile&kts=x:#9:F:#13&kts=x:#9:M:#12&kts=x:#14:F:#13&kts=x:#14:M:#12&kts=x:#3:M:#8:&kts=x:#3:F:#15:&kts=x:#3:D:#1"; %>
        <p>
            <a href="<%= "kinoath/getkin/view" + charlesTypeString %>">View Charles II ;of Spain</a>
            <br>
            <a href="<%= "kinoath/getkin/svg" + charlesTypeString %>">SVG Charles II ;of Spain</a>
        </p>

        <b>CSV output for use in R</b><br>
        <% // String csvUrlString = "kinoath/getkin/csv?kts=EmB&kts=EmZ&kts=EmM&kts=EmF&kts=EmS&kts=EmD"; %>
        <% String csvUrlString = "kinoath/getkin/csv?kts=" + kinTypeString; %>
        <p>
            dataFrame <- read.table("<a href="<%= csvUrlString %>"><%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()+request.getContextPath() + "/" %><%= csvUrlString %></a>",header=T)<br>
            library(kinship)<br>
            attach(dataFrame)<br>
            pedigreeObj <- pedigree(id, dadid, momid, sex, affected) <!-- , status, relations --><br>
            plot(pedigreeObj)<br>
        </p>
        Note that the pedigree package requires that there must always be two parents or none, this means that EM without EF will not be usable in that package.<br>
        Also the pedigree package supports status and relations but these are not output from this service yet.<br>
    </body>
</html>
