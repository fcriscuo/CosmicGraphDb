:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/CosmicCompleteDifferentialMethylation.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (meth:CosmicDiffMethylation {methyl_identifier:apoc.create.uuid()})

SET meth.study_id = toInteger(line.STUDY_ID),
  meth.sample_id = toInteger(line.ID_SAMPLE),
  meth.sample_name = line.SAMPLE_NAME,
  meth.tumor_id = toInteger(line.ID_TUMOUR),
  meth.fragment_id = line.FRAGMENT_ID,
  meth.GRCh = line.GENOME_VERSION,
  meth.chromosome = line.CHROMOSOME,
  meth.position = toInteger(line.POSITION),
  meth.stand = line.STRAND,
  meth.gene_symbol = line.GENE_NAME,
  meth.methylation = line.METHYLATION,
  meth.avg_beta_value_normal = toFloat(line.AVG_BETA_VALUE_NORMAL),
  meth.beta_value = toFloat(line.BETA_VALUE),
  meth.two_sided_p_value = toFloat(line.TWO_SIDED_P_VALUE),
  meth.date_created=datetime()
} IN TRANSACTIONS OF 1000 ROWS;

// relationship to SampleMutationCollection
MATCH (meth:CosmicDiffMethylation) WHERE meth.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = meth.sample_id
MERGE (smc) -[r1:HAS_DIFFERENTIAL_METHYLATION] ->(meth)
;

// relationship to GeneMutationCollection
MATCH (meth:CosmicDiffMethylation) WHERE meth.gene_symbol IS NOT NULL
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = meth.gene_symbol
MERGE (gmc)-[r2:HAS_DIFFERENTIAL_METHYLATION] ->(meth)
;
