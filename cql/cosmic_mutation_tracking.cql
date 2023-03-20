// CosmicMutationTracking
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CosmicMutationTracking.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MERGE (track:CosmicMutationTracking {mutation_id:toInteger(nodeRecord.MUTATION_ID)})
SET track.gene_symbol = nodeRecord.GENE_NAME,
  track.accession_number = nodeRecord.ACCESSION_NUMBER,
  track.genomic_mutation_id = nodeRecord.GENOMIC_MUTATION_ID,
  track.legacy_mutation_id = nodeRecord.LEGACY_MUTATION_ID,
  track.GRCh = nodeRecord.GRCH,
  track.mutation_type = nodeRecord.MUTATION_TYPE,
  track.is_canonical = toUpper(nodeRecord.IS_CANONICAL)
',
{batchSize:4000, iterateList:true, parallel:true});

// add clinical trials URL property
CALL {
MATCH(act:CosmicActionability) WHERE act.trial_id IS NOT NULL
SET act.drug_trial_url = replace("https://clinicaltrials.gov/ct2/show/XXXX","XXXX", act.trial_id)
} IN TRANSACTIONS OF 1000 ROWS;


