CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CosmicNCV.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT toInteger(trim(nodeRecord.`GENOMOC_MUTATION_ID`)) IS NULL
MERGE (ncv:CosmicNCV:Mutation{ncv_id:nodeRecord.GENOMIC_MUTATION_ID})
SET ncv.sample_name=nodeRecord.`Sample name`,
  ncv.sample_id=nodeRecord.ID_SAMPLE,
  ncv.tumor_id=nodeRecord.ID_tumour,
  ncv.legacy_mutation_id=nodeRecord.LEGACY_MUTATION_ID,
  ncv.zygosity=nodeRecord.zygosity,
  ncv.CRCh=nodeRecord.CRCh,
  ncv.genome_position=nodeRecord.`genome position`,
  ncv.mutation_somatic_status=nodeRecord.`Mutation somatic status`,
  ncv.wt_sequence=nodeRecord.WT_SEQ,
  ncv.mut_sequence=nodeRecord.MUT_SEQ,
  ncv.is_whole_genome_reseq=toUpper(nodeRecord.Whole_Genome_Reseq),
  ncv.is_whole_exome=toUpper(nodeRecord.Whole_Exome),
  ncv.study_id=nodeRecord.Whole_Exome,
  ncv.pub_id=nodeRecord.PUBMED_PMID,
  ncv.hgvsg=nodeRecord.HGVSG,
',
{batchSize:4000, iterateList:true});


// CosmicNCV -> Publication
MATCH(ncv:CosmicNCV) WHERE ncv.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(ncv.pub_id)})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
MERGE (ncv)-[r:HAS_PUBLICATION]->(p)
;


