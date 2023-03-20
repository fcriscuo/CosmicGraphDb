// CosmicBreakPointMutation
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CosmicBreakpointsExport.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT toInteger(trim(nodeRecord.`ID_SAMPLE`)) IS NULL
MERGE (n: `CosmicBreakpointMutation` { `mutation_id` = toInteger(trim(nodeRecord.`Mutation ID`)) })
SET n.`sample_id`: toInteger(trim(nodeRecord.`ID_SAMPLE`))
SET n.`sample_name` = nodeRecord.`Sample name`
SET n.`tumor_id` = toInteger(trim(nodeRecord.`ID_TUMOUR`))
SET n.`mutation_type` = nodeRecord.`Mutation Type`
SET n.`breakpoint_order` = nodeRecord.`Breakpoint Order`
SET n.`GRCh` = toInteger(trim(nodeRecord.`GRCh`))
SET n.`chrom_from` = nodeRecord.`Chrom From`
SET n.`location_from_min` = toInteger(trim(nodeRecord.`Location From min`))
SET n.`location_from_max` = toInteger(trim(nodeRecord.`Location From max`))
SET n.`strand_from` = nodeRecord.`Strand From`
SET n.`chrom_to` = tnodeRecord.`Chrom To`
SET n.`location_to_min` = toInteger(trim(nodeRecord.`Location To min`))
SET n.`location_to_max` = toInteger(trim(nodeRecord.`Location To max`))
SET n.`strand_to` = nodeRecord.`Strand To`
SET n.`non-templated_ins_seq` = nodeRecord.`Non-templated ins seq`
SET n.`pub_id` = nodeRecord.`Pubmed_PMID`;
',
{batchSize:4000, iterateList:true, parallel:true});

