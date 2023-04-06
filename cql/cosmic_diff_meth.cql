CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CosmicCompleteDifferentialMethylation.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT toInteger(trim(nodeRecord.sample_id)) IS NULL
MERGE (meth:CosmicDiffMethylation {methyl_identifier:toInteger(line_no) })
SET meth.study_id = toInteger(nodeRecord.STUDY_ID),
  meth.sample_id = toInteger(nodeRecord.ID_SAMPLE),
  meth.sample_name = nodeRecord.SAMPLE_NAME,
  meth.tumor_id = toInteger(nodeRecord.ID_TUMOUR),
  meth.fragment_id = nodeRecord.FRAGMENT_ID,
  meth.GRCh = nodeRecord.GENOME_VERSION,
  meth.chromosome = nodeRecord.CHROMOSOME,
  meth.position = toInteger(nodeRecord.POSITION),
  meth.stand = nodeRecord.STRAND,
  meth.gene_symbol = nodeRecord.GENE_NAME,
  meth.methylation = nodeRecord.METHYLATION,
  meth.avg_beta_value_normal = toFloat(nodeRecord.AVG_BETA_VALUE_NORMAL),
  meth.beta_value = toFloat(nodeRecord.BETA_VALUE),
  meth.two_sided_p_value = toFloat(nodeRecord.TWO_SIDED_P_VALUE)
',
{batchSize:4000, iterateList:true});

