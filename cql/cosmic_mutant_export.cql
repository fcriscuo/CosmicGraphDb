:auto CALL {

LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/sample/CosmicMutantExportCensus.tsv' AS line FIELDTERMINATOR '\t'

MERGE (mutation:CosmicCodingMutation:CosmicCensusMutation:Mutation{mutation_id:MUTATION_ID})
SET mutation.gene_symbol= line.`Gene name`,
  mutation.accession_number=line.`Accession Number`,
  mutation.cds_length=toInteger(line.`Gene CDS length`),
  mutation.hgnc_id=line.`HGNC ID`,
  mutation.sample_name=line.`Sample name`,
  mutation.sample_id=toInteger(line.ID_sample),
  mutation.tumor_id=toInteger(line.ID_tumour),
  mutation.is_genome_wide_screen=toUpper(line.`Genome-wide screen`),
  mutation.genomic_mutation_id=line.GENOMIC_MUTATION_ID,
  mutation.legacy_mutation_id=line.LEGACY_MUTATION_ID,
  mutation.mutation_cds=line.`Mutation CDS`,
  mutation.mutation_aa=line.`Mutation AA`,
  mutation.description=line.`Mutation Description`,
  mutation.zygosity=line.`Mutation zygosity`,
  mutation.loh=line.LOH,
  mutation.GRCh=line.GRCh,
  mutation.mutation_genome_position=line.`Mutation genome position`,
  mutation.mutation_strand=line.`Mutation strand`,
  mutation.resistance=toUpper(line.`Resistance Mutation`),
  mutation.mutation_somatic_status=line.`Mutation somatic status`,
  mutation.pub_id=toInteger(line.Pubmed_PMID),
  mutation.study_id=line.ID_STUDY,
  mutation.sample_type=line.`Sample Type`,
  mutation.tumor_origin=line.`Tumour origin`,
  mutation.ier=toInteger(line.Tier),
  mutation.mutation_hgvsp=line.HGVSP,
  mutation.mutation_hgvsc=line.HGVSC,
  mutation.mutation.hgvsg=line.HGVSG,
  mutation.date_created=datetime()
}IN TRANSACTIONS OF 1000 ROWS;

MATCH (ccm:CosmicCodingMutation)
MATCH (smc:SampleMutationCollection) WHERE toInteger(smc.sample_id) = ccm.sample_id
MERGE (smc)-[r1:HAS_MUTATION]->(ccm)
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = ccm.gene_symbol
MERGE (gmc)-[r2:HAS_MUTATION]->(ccm)
;

MATCH (ccm:CosmicCodingMutation) WHERE ccm.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:ccm.pub_id})
SET p.url= genomiccore.resolvePubmedUrl(toString(ccm.pub_id))
MERGE (ccm)-[HAS_PUBLICATION]->(p)
;





