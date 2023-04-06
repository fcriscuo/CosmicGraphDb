// CosmicHallmark
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv", { header:true,  sep:"TAB"})
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.`GENE_NAME` IS  NULL
MERGE (n: `CosmicHallmark` { `halmark_id`: toInteger(lineNo) })
SET n.`gene_symbol` = nodeRecord.`GENE_NAME`
SET n.`cell_type` = nodeRecord.`CELL_TYPE`
SET n.`pub_id` = toInteger(trim(nodeRecord.`PUBMED_PMID`))
SET n.`hallmark` = nodeRecord.`HALLMARK`
SET n.`impact` = nodeRecord.`IMPACT`
SET n.`description` = nodeRecord.`DESCRIPTION`
',
{batchSize:1000, iterateList:true});

// CosmicHallmark -> Publication
MATCH(hallmark:CosmicHallmark) WHERE hallmark.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(hallmark.pub_id)})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
MERGE (hallmark)-[r:HAS_PUBLICATION]->(p)
;

