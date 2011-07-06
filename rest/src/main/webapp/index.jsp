<html>
    <body>
        <% String kinTypeString = "EmMD:1:|EmFD:1:|EmS:2:|EmWD:3:|EmD:3:|EmWS:2:|EmZ:1:"; %>
        <h2>KinOath Web Application Example Page</h2>
        <b>Workspaces and Uploaded Files</b><br>
        <p>
            <a href="kinoath/kinspace">list work spaces</a>
        </p>
        <b>HTML output for viewing</b><br>
        <% String htmlUrlString = "kinoath/getkin/view?kts=" + kinTypeString; %>
        <p>
            <a href="<%= htmlUrlString %>"><%= htmlUrlString %></a>
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
        Note that the pedigree package requires that there must always be two parents or none, this means that EM without EM will not be usable in that package.<br>
        Also the pedigree package supports status and relations but these are not output from this service yet.<br>
    </body>
</html>
