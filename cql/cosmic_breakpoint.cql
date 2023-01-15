:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/CosmicBreakpointsExport.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line

MERGE (break:CosmicBreakpoint {mutation_id:toInteger(line.Mutation_ID)})
SET break.sample_name = line.`Sample name`,
  break.sample_id = toInteger(line.ID_SAMPLE),
  break.tumor_id = toInteger(line.ID_TUMOUR),
  break.mutation_type=line.`Mutation Type`,
  break.breakpoint_order=line.`Breakpoint Order`,
  break.GRCh = line.GRCh,
  break.chromosome_from = line.`Chrom From`,
  break.location_from_min = toInteger(line.`Location From min`),
  break.location_from_max = toInteger(line.`Location From max`),
  break.strand_from = line.`Strand From`,
  break.chromosome_to = line.`Chrom To`,
  break.location_to_min = toInteger(line.`Location From min`),
  break.location_to_max = toInteger(line.`Location From max`),
  break.strand_to = line.`Strand To`,
  break.non_templated_ins_seq = line.`Non-templated ins seq`,
  break.pub_id = toInteger(line.Pubmed_PMID),
  break.study_id = toInteger(line.STUDY_ID),
  break.date_created = datetime()
} IN TRANSACTIONS OF 1000 ROWS;

// create relationships to SampleMutationCollection nodes
MATCH (break:CosmicBreakpoint) WHERE break.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = break.sample_id
MERGE (smc)-[r1:HAS_BREAKPOINT_MUTATION]->(break)
;

// create relationships to Publication nodes
MATCH(break:CosmicBreakpoint) WHERE break.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(break.pub_id)})
SET p.url= genomiccore.resolvePubmedUrl(break.pub_id)
MERGE (break)-[r2:HAS_PUBLICATION]->(p)
;