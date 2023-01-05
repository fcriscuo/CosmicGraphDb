:auto CALL {
LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/classification.csv' AS line

MERGE (cl:CosmicClassification {phenotype_id: line.COSMIC_PHENOTYPE_ID})
MERGE (site: CosmicType:Site{primary_site:line.SITE_PRIMARY})
MERGE (histology:CosmicType:Histology{histology: line.HISTOLOGY})
MERGE (cl)-[r1:HAS_SITE]->(site)
MERGE (cl)-[r2:HAS_HISTOLOGY]->(histology)
SET cl.nci_code = line.NCI_CODE, cl.efo = line.EFO, cl.created = datetime(),
site.subtype1 = line.SITE_SUBTYPE1,
site.subtype2 = line.SITE_SUBTYPE2,
site.subtype3 = line.SITE_SUBTYPE3,
histology.subtype1 = line.HIST_SUBTYPE1,
histology.subtype2 = line.HIST_SUBTYPE2,
histology.subtype3 = line.HIST_SUBTYPE3
} IN TRANSACTIONS OF 1000 ROWS;

match (cl:CosmicClassification) where cl.nci_code is not null set cl.url = replace("https://ncit.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI%20Thesaurus&code=XXXX","XXXX",cl.nci_code) ;
