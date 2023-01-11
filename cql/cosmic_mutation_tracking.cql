:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/CosmicMutationTracking.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line

MERGE (track:CosmicMutationTracking {mutation_id:toInteger(line.MUTATION_ID)})
SET track.gene_symbol = line.GENE_NAME,
  track.accession_number = line.ACCESSION_NUMBER,
  track.genomic_mutation_id = line.GENOMIC_MUTATION_ID,
  track.legacy_mutation_id = line.LEGACY_MUTATION_ID,
  track.GRCh = line.GRCH,
  track.mutation_type = line.MUTATION_TYPE,
  track.is_canonical = toUpper(line.IS_CANONICAL),
  track.date_created = datetime()
} IN TRANSACTIONS OF 1000 ROWS;

// relationship to CosmicMutation
MATCH (track:CosmicMutationTracking) WHERE track.mutation_id IS NOT NULL
MATCH (mut:CosmicMutation) WHERE mut.mutation_id = track.mutation_id
MERGE (mut)-[r1:HAS_MUTATION_TRACKING]->(track)
;

// relationship to Cosmic Gene
MATCH (track:CosmicMutationTracking) WHERE track.gene_symbol IS NOT NULL
MATCH (gene:CosmicGene) WHERE gene.gene_symbol = track.gene_symbol
MERGE (gene)-][r2:HAS_MUTATION_TRACKING]->(track)
;
