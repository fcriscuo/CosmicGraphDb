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
  express.study_id = toInteger(nodeRecord.ID_STUDY),
  express.date_created = datetime()
',
{batchSize:4000, iterateList:true, parallel:true});

// define relationships to SampleMutationCollection nodes
MATCH (express:CosmicGeneExpression) WHERE express.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = express.sample_id
MERGE (smc) -[r1:HAS_GENE_EXPRESSION_VARIANT] ->(express)
;

//define relationships to GeneMutationCollection nodes
MATCH (express:CosmicGeneExpression) WHERE express.gene_symbol IS NOT NULL
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = express.gene_symbol
MERGE (gmc) -[r2:HAS_GENE_EXPRESSION_VARIANT] ->(express)
;
