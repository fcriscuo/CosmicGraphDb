## COSMICGraphDb

COSMICGraphDb is a backend application implemented in Kotlin 1.7 that will
load data from the Catalog of Somatic Mutations in Cancer (COSMIC),
(https://cancer.sanger.ac.uk/cosmic) data files into a local Neo4j 4.4 database
(https://neo4j.com/). The major graph database nodes and relationships 
are illustrated below. This 
design diagram does not specify cardinality (e.g. A tumor may have >1 mutation).


### Application Design

The application takes advantage of Kotlin's native concurrency capabilities (i.e. channels & flows)
to minimize execution run times. Data from COSMIC files are loaded into Neo4j as soon as parent-child
dependencies are met. For example, when the application starts, classification and gene census data are 
loaded concurrently. A separate PubMed data loader, described below, is also initiated. As soon as the
gene census data are loaded, loading of the gene hallmark data is initiated. As soon as the classification data
are loaded, loading the sample data begins.  Within each loader, distinct tasks are run concurrently using
Kotlin channels. This overlap of processing tasks lowers the memory required to process large COSMIC files.

![](DatabaseDesign.jpg)
### Requirements
The application requires the user to define two (2) system environment properties,
NEO4J_ACCOUNT and NEO4J_PASSWORD. This allows for easier code sharing without
exposing Neo4j credentials. The application logs all Neo4j CYPHER commands to a log
file in the /tmp/logs/neo4j directory. The filenames for these log files contain
a timestamp component, so they are not overwritten by subsequent executions.

The application utilizes CSV and TSV files downloaded to local storage. The location
of these files must be specified in the datafiles.properties resource file. The 
Sanger Lab requires a user license in order to download the necessary files
(https://cancer.sanger.ac.uk/cosmic/license). This repository does not provide any
COSMIC data. 

###PubMed Support
The application supplements the COSMIC data by retrieving data for PubMed articles
identified in the COSMIC files. Information is retrieved from NCBI
for the PubMed articles 
directly specified, the PubMed articles referenced by the original article, and for
the PubMed articles that cite the original article.
To accomplish this, the application utilizes the pubmed-parser library available 
from the thecloudcircle
account on GitHub (https://github.com/thecloudcircle/pubmed-parser) to map XML data
received from PubMed to Java JAXB objects.
NCBI enforces a limit of three (3) API requests per second
(10 with a registered API key). To accommodate that restriction,
the application pauses for 300 milliseconds after
each request.
To avoid impacting the loading of COSMIC data, PubMed retrieval and loading is performed as a concurrent
task in parallel with COSMIC data loading.
When a novel PubMed id is encountered in loading COSMIC data, a placeholder (i.e. incomplete) PubMedArticle
node is created in the Neo4j database.
The PubMed data loader is invoked on a periodic basis and, it scans the database for incomplete nodes.
PubMed data are retrieved from NCBI and loaded into Neo4j to complete the nodes.
The noted pubmed-parser library is limited to a JVM of level 15 or below. If a later JVM is required, the application
should be refactored to load PubMed data as a separate task in its own JVM.
