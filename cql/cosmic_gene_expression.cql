:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/CosmicCompleteGeneExpression.tsv' AS line FIELDTERMINATOR '\t'
CALL {

WITH line
MERGE (express:CosmicGeneExpression:Mutation {express_identifier: apoc.create.uuid()})

SET express.sample_id = toInteger(line.SAMPLE_ID),
  express.sample_name = line.SAMPLE_NAME,
  express.gene_symbol = line.GENE_NAME,
  express.regulation = line.REGULATION,
  express.z_score = toFloadt(Z_SCORE),
  express.study_id = toInteger(line.ID_STUDY),
  express.date_created = datetime()
}IN TRANSACTIONS OF 3000 ROWS;

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
