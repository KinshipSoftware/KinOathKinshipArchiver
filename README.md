KinOathKinshipArchiver
======================

KinOath Kinship Archiver is a kinship application with the primary goal of connecting kinship data with archived data, such as audio, video or written resources while also being closely integrated with the archive software such as Arbil. Beyond this primary goal it is designed to be flexible and culturally nonspecific, such that culturally different social structures can equally be represented. Kin type strings are used throughout the application for constructing and searching data sets. The representation of kin terms is also integrated into the application allowing comparative diagrams of kin terms. Graphical representation of the data is an important part of the application and the diagrams produced are intended to very flexible and of publishable quality.





There are now a number of discrete maven projects in this GitHub repository:


**batik-renderer:** An example swing application that generates and shows a kinship diagram.

**core:** Shared components used by the other KinOath projects.

**desktop:** The desktop version of KinOath.

**diagram:** Generates the actual diagram used by the other KinOath projects and examples.

**graph-sorter:** Components that perform the task of arranging the entities on the diagram (custom sorters can also be written).

**graph-storage:** The BaseX database module used to store and retrieve entity data.

**kin-type-string-parser:** Parses kin type strings into a structure that can be used to generate diagrams or query the database.

**kinoath-help:** The help system used in the desktop version.

**localisation:** Internationalisation files for the user interface.

**rest:**  An example web application that generates and returns a kinship diagram in SVG format.
