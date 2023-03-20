// CosmicCMC
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CMC/cmc_export.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MERGE (cmc:CosmicCMC {legacy_mutation_id:nodeRecord.LEGACY_MUTATION_ID})
SET cmc.gene_symbol = nodeRecord.GENE_NAME,
  cmc.accession_number = nodeRecord.ACCESSION_NUMBER,
  cmc.onc_tsg = nodeRecord.ONC_TSG,
  cmc.cgc_tier = toInteger(nodeRecord.CGC_TIER),
  cmc.mutation_url = nodeRecord.MUTATION_URL,
  cmc.muation_cds = nodeRecord.`Mutation CDS`,
  cmc.mutation_aa = nodeRecord.`Mutation AA`,
  cmc.aa_mut_start = toInteger(nodeRecord.AA_MUT_START),
  cmc.aa_mut_stop = toInteger(nodeRecord.AA_MUT_STOP),
  cmc.shared_aa = nodeRecord.SHARED_AA,
  cmc.genomic_wt_allele_seq = nodeRecord.GENOMIC_WT_ALLELE_SEQ,
  cmc.genomic_mut_allele_seq= nodeRecord.GENOMIC_MUT_ALLELE_SEQ,
  cmc.aa_wt_allele_seq = nodeRecord.AA_WT_ALLELE_SEQ,
  cmc.aa_mut_allele_seq = nodeRecord.AA_MUT_ALLELE_SEQ,
  cmc.mutation_description_cds = nodeRecord.`Mutation Description CDS`,
  cmc.mutation_description_aa = nodeRecord.`Mutation Description AA`,
  cmc.ontology_mutation_code = nodeRecord.ONTOLOGY_MUTATION_CODE,
  cmc.genomic_mutation_id = nodeRecord.GENOMIC_MUTATION_ID,
  cmc.mutation_genome_position_grch37 = nodeRecord.`Mutation genome position GRCh37`,
  cmc.mutation_genome_position_grch38 = nodeRecord.`Mutation genome position GRCh38`,
  cmc.cosmic_sample_tested = toInteger(nodeRecord.COSMIC_SAMPLE_TESTED),
  cmc.cosmic_sample_mutated = toInteger(nodeRecord.COSMIC_SAMPLE_MUTATED),
  cmc.disease = nodeRecord.DISEASE,
  cmc.wgs_disease = nodeRecord.WHS_DISEASE
',
{batchSize:4000, iterateList:true, parallel:true});

