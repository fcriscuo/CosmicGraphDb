CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CosmicCompleteCNA.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT toInteger(trim(nodeRecord.sample_id)) IS NULL
MERGE (cna:CosmicCNA {cna_identifier:toInteger(line_no)})
SET cna.cnv_id = toInteger(nodeRecord.CNV_ID),
  cna.gene_id = toInteger(nodeRecord.ID_CNA),
  cna.gene_symbol = nodeRecord.gene_name,
  cna.sample_id = toInteger(nodeRecord.ID_SAMPLE),
  cna.tumor_id = toInteger(nodeRecord.ID_TUMOUR),
  cna.sample_name = nodeRecord.SAMPLE_NAME,
  cna.total_cn = toInteger(nodeRecord.TOTAL_CN),
  cna.minor_allele = nodeRecord.MINOR_ALLELE,
  cna.mut_type = nodeRecord.MUT_TYPE,
  cna.study_id = toInteger(nodeRecord.ID_STUDY),
  cna.GRCh = nodeRecord.GRCh,
  cna.chromosome_start_stop = nodeRecord.`Chromosome:G_Start..G_Stop`
',
{batchSize:4000, iterateList:true, parallel:true});

