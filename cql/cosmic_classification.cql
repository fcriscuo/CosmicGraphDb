// CosmicSampleClassification
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/classification.csv", { header:true}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.`COSMIC_PHENOTYPE_ID` IS NULL
MERGE (cl:CosmicClassification {phenotype_id: nodeRecord.COSMIC_PHENOTYPE_ID})
MERGE (site: CosmicType:Site{primary_site:nodeRecord.SITE_PRIMARY})
MERGE (histology:CosmicType:Histology{histology: nodeRecord.HISTOLOGY})
MERGE (cl)-[r1:HAS_SITE]->(site)
MERGE (cl)-[r2:HAS_HISTOLOGY]->(histology)
SET cl.nci_code = nodeRecord.NCI_CODE,
cl.efo = nodeRecord.EFO,
cl.created = datetime(),
site.subtype1 = nodeRecord.SITE_SUBTYPE1,
site.subtype2 = nodeRecord.SITE_SUBTYPE2,
site.subtype3 = nodeRecord.SITE_SUBTYPE3,
histology.subtype1 = nodeRecord.HIST_SUBTYPE1,
histology.subtype2 = nodeRecord.HIST_SUBTYPE2,
histology.subtype3 = nodeRecord.HIST_SUBTYPE3
',
{batchSize:1000, iterateList:true, parallel:true});

match (cl:CosmicClassification)
  where cl.nci_code is not null
set cl.url = replace("https://ncit.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI%20Thesaurus&code=XXXX","XXXX",cl.nci_code) ;
