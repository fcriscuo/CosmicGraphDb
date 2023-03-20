// CosmicSample,  CosmicSampleMutationCollection, CosmicTumor, & CosmicPatient nodes
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/CosmicSample.tsv", { header:true,  sep:"TAB"})
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MERGE (n: `CosmicSample` { `sample_id`: toInteger(trim(nodeRecord.`sample_id`)) })
MERGE (csmc: `CosmicSampleMutationCollection`{ `sample_id`: toInteger(trim(nodeRecord.`sample_id`)) })
MERGE (ct: `Tumor` { `tumor_id`: toInteger(trim(nodeRecord.`id_tumour`)) })
MERGE (cp: `CosmicPatient` { `individual_id`: toInteger(trim(nodeRecord.`id_individual`)) })

MERGE  (n)-[r1:HAS_MUTATION_COLLECTION]->(csmc)
MERGE (cp)-[r3:HAS_TUMOR]->(ct)
MERGE (ct)-[r2:HAS_SAMPLE]->(n)

SET n.`sample_name` = nodeRecord.`sample_name`
SET csmc.`sample_name` = nodeRecord.`sample_name`
SET n.`tumor_id` = toInteger(trim(nodeRecord.`id_tumour`))
SET n.`therapy_relationship` = nodeRecord.`therapy_relationship`
SET n.`sample_differentiator` = nodeRecord.`sample_differentiator`
SET n.`mutation_allele_specification` = nodeRecord.`mutation_allele_specification`
SET n.`msi` = nodeRecord.`msi`
SET n.`whole_genome_screen` = nodeRecord.`whole_genome_screen`
SET n.`whole_exome_screen` = nodeRecord.`whole_exome_screen`
SET n.`sample_remark` = nodeRecord.`sample_remark`
SET n.`germline_mutation` = nodeRecord.`germline_mutation`
SET n.`sample_type` = nodeRecord.`sample_type`
SET n.`cosmic_phenotype_id` = toInteger(trim(nodeRecord.`cosmic_phenotype_id`))
SET cp.`metastatic_site` = nodeRecord.`metastatic_site`
SET cp.`tumor_id` = toInteger(trim(nodeRecord.`id_tumour`))
SET cp.`age` = toInteger(trim(nodeRecord.`age`))
SET cp.`ethnicity` = nodeRecord.`ethnicity`
SET cp.`environmental_variables` = nodeRecord.`environmental_variables`
SET cp.`therapy` = nodeRecord.`therapy`
SET cp.`family` = nodeRecord.`family`
SET cp.`normal_tissue_tested` = nodeRecord.`normal_tissue_tested`
SET cp.`individual_remark` = nodeRecord.`individual_remark`
SET cp.`gender` = nodeRecord.`gender`
SET ct.`drug_response` = nodeRecord.`drug_response`
SET ct.`grade` = nodeRecord.`grade`
SET ct.`age_at_tumor_recurrence` = nodeRecord.`age_at_tumour_recurrence`
SET ct.`stage` = nodeRecord.`stage`
SET ct.`nci_code` = nodeRecord.`nci_code`
 ',
{batchSize:1000, iterateList:true, parallel:true});

