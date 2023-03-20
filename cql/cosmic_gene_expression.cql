CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CosmicCompleteGeneExpression.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT toInteger(trim(nodeRecord.`SAMPLE_ID`)) IS NULL
MERGE (express:CosmicGeneExpression:Mutation {express_identifier: toInteger(line_no)})

SET express.sample_id = toInteger(nodeRecord.SAMPLE_ID),
  express.sample_name = nodeRecord.SAMPLE_NAME,
  express.gene_symbol = nodeRecord.GENE_NAME,
  express.regulation = nodeRecord.REGULATION,
  express.z_score = toFloadt(Z_SCORE),
  express.study_id = toInteger(nodeRecord.ID_STUDY)
',
{batchSize:4000, iterateList:true, parallel:true});


