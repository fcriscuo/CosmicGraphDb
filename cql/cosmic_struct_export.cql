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

// relationship to SampleMutationCollection
MATCH (struct:CosmicStruct) WHERE struct.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = struct.sample_id
MERGE (smc)-[r1:HAS_STRUCT_MUTATION]->(struct)
;

// add relationship tp Publication and  PubMed URL property
MATCH (struct:CosmicStruct) WHERE struct.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(struct.pub_id)})
SET p.url= genomiccore.resolvePubmedUrl(struct.pub_id)
MERGE (struct)-[r:HAS_PUBLICATION]->(p)
;