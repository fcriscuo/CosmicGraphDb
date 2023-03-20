// CosmicResistanceMutation
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CosmicResistanceMutations.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT toInteger(trim(nodeRecord.`MUTATION_ID`)) IS NULL
MERGE (n: `CosmicResistanceMutation` { `identifier`: toInteger(line_no) })
SET n.`mutation_id` = toInteger(nodeRecord.`MUTATION_ID`)
SET n.`sample_name` = nodeRecord.`Sample Name`
SET n.`sample_id` = toInteger(trim(nodeRecord.`Sample ID`))
SET n.`gene_symbol` = nodeRecord.`Gene Name`
SET n.`transcript` = nodeRecord.`Transcript`
SET n.`census_gene` = nodeRecord.`Census Gene`
SET n.`drug_name` = nodeRecord.`Drug Name`
SET n.`genomic_mutation_id` = nodeRecord.`GENOMIC_MUTATION_ID`
SET n.`legacy_mutation_id` = nodeRecord.`LEGACY_MUTATION_ID`
SET n.`aa_mutation` = nodeRecord.`AA Mutation`
SET n.`cds_mutation` = nodeRecord.`CDS Mutation`
SET n.`primary_tissue` = nodeRecord.`Primary Tissue`
SET n.`pub_id` = toInteger(trim(nodeRecord.`Pubmed Id`))
SET n.`cgp_study` = nodeRecord.`CGP Study`
SET n.`somatic_status` = nodeRecord.`Somatic Status`
SET n.`sample_type` = nodeRecord.`Sample Type`
SET n.`zygosity` = nodeRecord.`Zygosity`
SET n.`genome_coordinates_grch38` = nodeRecord.`Genome Coordinates (GRCh38)`
SET n.`tier` = toInteger(trim(nodeRecord.`Tier`))
SET n.`HGVSP` = nodeRecord.`HGVSP`
SET n.`HGVSC` = nodeRecord.`HGVSC`
SET n.`HGVSG` = nodeRecord.`HGVSG`;
',
{batchSize:4000, iterateList:true, parallel:true});






