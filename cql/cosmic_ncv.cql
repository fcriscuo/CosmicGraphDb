:auto
LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/sample/CosmicNCV.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (ncv:CosmicNCV:Mutation{ncv_id:GENOMIC_MUTATION_ID})

SET ncv.sample_name=line.`Sample name`,
  ncv.sample_id=line.ID_SAMPLE,
  ncv.tumor_id=line.ID_tumour,
  ncv.legacy_mutation_id=line.LEGACY_MUTATION_ID,
  ncv.zygosity=line.zygosity,
  ncv.CRCh=line.CRCh,
  ncv.genome_position=line.`genome position`,
  ncv.mutation_somatic_status=line.`Mutation somatic status`,
  ncv.wt_sequence=line.WT_SEQ,
  ncv.mut_sequence=line.MUT_SEQ,
  ncv.is_whole_genome.reseq=toUpper(line.Whole_Genome_Reseq),
  ncv.is_whole_exome=toUpper(line.Whole_Exome),
  ncv.study_id=line.Whole_Exome,
  ncv.pub_id=line.PUBMED_PMID,
  ncv.hgvsg=line.HGVSG,
  ncv.date_created=datetime()
} IN TRANSACTIONS OF 1000 ROWS;

// relationship to  SampleMutationCollection node
MATCH(ncv:CosmicNCV)
MATCH(smc:SampleMutationCollection) WHERE smc.sample_id = ncv.sample_id
MERGE (smc)-[r:HAS_MUTATION]->(ncv)

//relationship to Publication node
MATCH(ncv:CosmicNCV) WHERE ncv.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(ncv.pub_id)})
SET p.url= genomiccore.resolvePubmedUrl(ncv.pub_id)
MERGE (ncv)-[r:HAS_PUBLICATION]->(p)
