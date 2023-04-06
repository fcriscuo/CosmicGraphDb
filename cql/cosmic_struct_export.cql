// CosmicStructuralMutation
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CosmicStructExport.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE  NOT toInteger(trim(nodeRecord.`MUTATION_ID`)) IS NULL
MERGE (n: `CosmicStructuralMutation` { `mutation_id`: toInteger(trim(nodeRecord.`MUTATION_ID`)) })
SET n.`sample_name` = nodeRecord.`Sample name`
SET n.`sample_id` = toInteger(trim(nodeRecord.`ID_SAMPLE`))
SET n.`tumor_id` = toInteger(trim(nodeRecord.`ID_TUMOUR`))
SET n.`mutation_type` = nodeRecord.`Mutation Type`
SET n.`GRCh` = toInteger(trim(nodeRecord.`GRCh`))
SET n.`description` = nodeRecord.`description`
SET n.`pub_id` = nodeRecord.`PUBMED_PMID`;
',
{batchSize:4000, iterateList:true});

// CosmicStruct ->Publication
MATCH (struct:CosmicStruct) WHERE struct.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(struct.pub_id)})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
MERGE (struct)-[r:HAS_PUBLICATION]->(p)
;


