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
{batchSize:4000, iterateList:true, parallel:true});


