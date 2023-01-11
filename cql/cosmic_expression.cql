:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/CosmicCompleteGeneExpression.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line

MERGE (express:CosmicGeneExpression {cge_identifier:apoc.create.uuid()})
SET express.sample_id = toInteger(line.SAMPLE_ID),
  express.sample_name = line.SAMPLE_NAME,
  express.gene_symbol = line.GENE_NAME,
  express.regulation = line.REGULATION,
  express.z_score = toFloat(line.Z_SCORE),
  express.study_id = line.ID_STUDY,
  express.date_created = datetime()
} IN TRANSACTIONS OF 1000 ROWS;

// relationship to CosmicGeneCensus
MATCH (express:CosmicGeneExpression) WHERE express.gene_symbol IS NOT NULL
MATCH (gene:CosmicGene) WHERE gene.gene_symbol = express.gene_symbol
MERGE (gene) -[r1:HAS_GENE_EXPRESSION] -> (express)
;

// relationship to CosmicSample
MATCH (express:CosmicGeneExpression) WHERE express.sample_id IS NOT NULL
MATCH (sample:CosmicSample) WHERE sample.sample_id = express.sample_id
MERGE (sample) -[r2:HAS_GENE_EXPRESSION]-> (express)
;