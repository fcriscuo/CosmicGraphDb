CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CosmicCompleteGeneExpression.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT toInteger(trim(nodeRecord.SAMPLE_ID))) IS NULL
MERGE (express:CosmicGeneExpression {cge_identifier:toInteger(line_no) })
SET express.sample_id = toInteger(nodeRecord.SAMPLE_ID),
  express.sample_name = nodeRecord.SAMPLE_NAME,
  express.gene_symbol = nodeRecord.GENE_NAME,
  express.regulation = nodeRecord.REGULATION,
  express.z_score = toFloat(nodeRecord.Z_SCORE),
  express.study_id = nodeRecord.ID_STUDY,
  express.date_created = datetime()
}',
{batchSize:4000, iterateList:true, parallel:true});

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