:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/sample/CosmicStructExport.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (struct:CosmicStruct:Mutation {mutation_id:toInteger(line.MUTATION_ID)})
SET struct.sample_name = line.`Sample name`,
  struct.sample_id = toInteger(line.ID_SAMPLE),
  struct.tumor_id = toInteger(line.ID_TUMOUR),
  struct.mutation_type = line.`Mutation Type`,
  struct.GRCh = line.GRCh,
  struct.description = line.description,
  struct.pub_id = line.PUBMED_PMID,
  struct.study_id = toInteger(line.ID_STUDY),
  struct.date_created = datetime()
}IN TRANSACTIONS OF 1000 ROWS;

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