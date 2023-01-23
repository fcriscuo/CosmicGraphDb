:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/CMC/sample_cmc_export.tsv' AS line FIELDTERMINATOR '\t'
WITH line WHERE line.LEGACY_MUTATION_ID IS NOT NULL

// GENE_NAME	ACCESSION_NUMBER	ONC_TSG	CGC_TIER	MUTATION_URL	LEGACY_MUTATION_ID
// Mutation CDS	Mutation AA	AA_MUT_START	AA_MUT_STOP	SHARED_AA	GENOMIC_WT_ALLELE_SEQ
// GENOMIC_MUT_ALLELE_SEQ	AA_WT_ALLELE_SEQ	AA_MUT_ALLELE_SEQ	Mutation Description CDS
// Mutation Description AA	ONTOLOGY_MUTATION_CODE	GENOMIC_MUTATION_ID
// Mutation genome position GRCh37	Mutation genome position GRCh38	COSMIC_SAMPLE_TESTED
// COSMIC_SAMPLE_MUTATED	DISEASE	WGS_DISEASE	EXAC_AF	EXAC_AFR_AF	EXAC_AMR_AF	EXAC_ADJ_AF
// EXAC_EAS_AF	EXAC_FIN_AF	EXAC_NFE_AF	EXAC_SAS_AF	GNOMAD_EXOMES_AF	GNOMAD_EXOMES_AFR_AF
// GNOMAD_EXOMES_AMR_AF	GNOMAD_EXOMES_ASJ_AF	GNOMAD_EXOMES_EAS_AF	GNOMAD_EXOMES_FIN_AF
// GNOMAD_EXOMES_NFE_AF	GNOMAD_EXOMES_SAS_AF	GNOMAD_GENOMES_AF	GNOMAD_GENOMES_AFR_AF
// GNOMAD_GENOMES_AMI_AF	GNOMAD_GENOMES_AMR_AF	GNOMAD_GENOMES_ASJ_AF	GNOMAD_GENOMES_EAS_AF
// GNOMAD_GENOMES_FIN_AF	GNOMAD_GENOMES_MID_AF	GNOMAD_GENOMES_NFE_AF	GNOMAD_GENOMES_SAS_AF
// CLINVAR_CLNSIG	CLINVAR_TRAIT	GERP++_RS	MIN_SIFT_SCORE	MIN_SIFT_PRED	DNDS_DISEASE_QVAL_SIG
// MUTATION_SIGNIFICANCE_TIER
CALL {
WITH line
MERGE (cmc:CosmicCMC {legacy_mutation_id:line.LEGACY_MUTATION_ID})
SET cmc.gene_symbol = line.GENE_NAME,
  cmc.accession_number = line.ACCESSION_NUMBER,
  cmc.onc_tsg = line.ONC_TSG,
  cmc.cgc_tier = toInteger(line.CGC_TIER),
  cmc.mutation_url = line.MUTATION_URL,
  cmc.muation_cds = line.`Mutation CDS`,
  cmc.mutation_aa = line.`Mutation AA`,
  cmc.aa_mut_start = toInteger(line.AA_MUT_START),
  cmc.aa_mut_stop = toInteger(line.AA_MUT_STOP),
  cmc.shared_aa = line.SHARED_AA,
  cmc.genomic_wt_allele_seq = line.GENOMIC_WT_ALLELE_SEQ,
  cmc.genomic_mut_allele_seq= line.GENOMIC_MUT_ALLELE_SEQ,
  cmc.aa_wt_allele_seq = line.AA_WT_ALLELE_SEQ,
  cmc.aa_mut_allele_seq = line.AA_MUT_ALLELE_SEQ,
  cmc.mutation_description_cds = line.`Mutation Description CDS`,
  cmc.mutation_description_aa = line.`Mutation Description AA`,
  cmc.ontology_mutation_code = line.ONTOLOGY_MUTATION_CODE,
  cmc.genomic_mutation_id = line.GENOMIC_MUTATION_ID,
  cmc.mutation_genome_position_grch37 = line.`Mutation genome position GRCh37`,
  cmc.mutation_genome_position_grch38 = line.`Mutation genome position GRCh38`,
  cmc.cosmic_sample_tested = toInteger(line.COSMIC_SAMPLE_TESTED),
  cmc.cosmic_sample_mutated = toInteger(line.COSMIC_SAMPLE_MUTATED),
  cmc.disease = line.DISEASE,
  cmc.wgs_disease = line.WHS_DISEASE,
  cmc.date_created = datetime()
} IN TRANSACTIONS OF 1000 ROWS;

MATCH (cmc:CosmicCMC) WHERE cmc.gene_symbol IS NOT NULL
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = cmc.gene_symbol
MERGE (gmc) -[r1:HAS_CODING_MUTATION]->(cmc)
;