:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/CosmicCompleteCNA.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (cna:CosmicCNA {cna_identifier:apoc.create.uuid()})
SET cna.cnv_id = toInteger(line.CNV_ID),
  cna.gene_id = toInteger(line.ID_CNA),
  cna.gene_symbol = line.gene_name,
  cna.sample_id = toInteger(line.ID_SAMPLE),
  cna.tumor_id = toInteger(line.ID_TUMOUR),
  cna.sample_name = line.SAMPLE_NAME,
  cna.total_cn = toInteger(line.TOTAL_CN),
  cna.minor_allele = line.MINOR_ALLELE,
  cna.mut_type = line.MUT_TYPE,
  cna.study_id = toInteger(line.ID_STUDY),
  cna.GRCh = line.GRCh,
  cna.chromosome_start_stop = line.`Chromosome:G_Start..G_Stop`,
  cna.date_created = datetime()
} IN TRANSACTIONS OF 1000 ROWS;

// relationship to SampleMutationCollection
MATCH (cna:CosmicCNA) WHERE cna.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = cna.sample_id
MERGE (smc)-[r1:HAS_CNA_MUTATION]->(cna)
;

// relationship to GeneMutationCollection
MATCH (cna:CosmicCNA) WHERE cna.gene_symbol IS NOT NULL
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = cna.gene_symbol
MERGE (gmc)-[r2:HAS_CNA_MUTATION]->(cna)
;
