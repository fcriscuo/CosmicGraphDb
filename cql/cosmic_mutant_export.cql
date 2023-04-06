// CosmicMutation
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CosmicMutantExportCensus.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE  toInteger(trim(nodeRecord.`MUTATION_ID`)) IS NULL
MERGE (n: `CosmicMutation` { `mutation_id`: toInteger(trim(nodeRecord.`MUTATION_ID`)) })
SET n.`gene_symbol` = nodeRecord.`Gene name`
SET n.`accession_number` = nodeRecord.`Accession Number`
SET n.`cds_length` = toInteger(trim(nodeRecord.`Gene CDS length`))
SET n.`hgnc_id` = toInteger(trim(nodeRecord.`HGNC ID`))
SET n.`sample_name` = nodeRecord.`Sample name`
SET n.`sample_id` = toInteger(trim(nodeRecord.`ID_sample`))
SET n.`tumor_id` = toInteger(trim(nodeRecord.`ID_tumour`))
SET n.`primary_site` = nodeRecord.`Primary site`
SET n.`primary_histology` = nodeRecord.`Primary histology`
SET n.`genome_screen` = nodeRecord.`Genome-wide screen`
SET n.`genomic_mutation_id` = nodeRecord.`GENOMIC_MUTATION_ID`
SET n.`legacy_mutation_id` = nodeRecord.`LEGACY_MUTATION_ID`
SET n.`mutation_cds` = nodeRecord.`Mutation CDS`
SET n.`mutation_aa` = nodeRecord.`Mutation AA`
SET n.`mutation_description` = nodeRecord.`Mutation Description`
SET n.`Mutation zygosity` = nodeRecord.`Mutation zygosity`
SET n.`LOH` = nodeRecord.`LOH`
SET n.`GRCh` = toInteger(trim(nodeRecord.`GRCh`))
SET n.`mutation_genome_position` = nodeRecord.`Mutation genome position`
SET n.`mutation_strand` = nodeRecord.`Mutation strand`
SET n.`resistance_mutation` = nodeRecord.`Resistance Mutation`
SET n.`mutation_somatic_status` = nodeRecord.`Mutation somatic status`
SET n.`pub_id` = toInteger(trim(nodeRecord.`Pubmed_PMID`))
SET n.`study_id` = toInteger(trim(nodeRecord.`ID_STUDY`))
SET n.`sample_type` = nodeRecord.`Sample Type`
SET n.`tumor_origin` = nodeRecord.`Tumour origin`
SET n.`Age` = toFloat(trim(nodeRecord.`Age`))
SET n.`tier` = toInteger(trim(nodeRecord.`Tier`))
SET n.`HGVSP` = nodeRecord.`HGVSP`
SET n.`HGVSC` = nodeRecord.`HGVSC`
SET n.`HGVSG` = nodeRecord.`HGVSG`;
',
{batchSize:1000, iterateList:true});

// CosmicMutation -> GenomicEntity
MATCH (ge:GenomicEntity) where ge.gene_symbol is not null
match (cm:CosmicMutation) where cm.gene_symbol = ge.gene_symbol
merge (cm)-[r:HAS_GENOMIC_ENTITY]-> (ge)
;

// CosmicMutation -> Publication
MATCH (ccm:CosmicMutation) WHERE ccm.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:ccm.pub_id})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
MERGE (ccm)-[HAS_PUBLICATION]->(p)
;


