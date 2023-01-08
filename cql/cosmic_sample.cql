:auto CALL {
LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/CosmicSample50K.tsv' AS line FIELDTERMINATOR '\t'

MERGE (patient:Patient:Individual{individual_id: line.id_individual})
MERGE (sample:CosmicSample{sample_id:line.sample_id})
MERGE (smc: SampleMutationCollection{sample_id:line.sample_id})
MERGE (tumor:CosmicTumor{tumor_id:line.id_tumour})
MERGE (patient)-[r1:HAS_TUMOR]->(tumor)
MERGE (tumor)-[r2:HAS_SAMPLE]->(sample)
MERGE (sample)-[r5:HAS_SAMPLE_MUTATION_COLLECTION]->(smc)

SET sample.sample_name=line.sample_name,
    sample.therapy_relationship=line.therapy_relationship,
    sample.sample_differentiator=line.sample_differentiator,
    sample.mutation_allele_specification=line.mutation_allele_specification,
    sample.msi=line.msi,
    sample.ploidy=line.ploidy,
    sample.whole_genome_screen=toUpper(line.whole_genome_screen),
    sample.whole_exome_screen=toUpper(line.whole_exome_screen),
    sample.sample_remark=line.sample_remark,
    sample.germline_mutation=line.germline_mutation,
    sample.sample_type=line.sample_type,
    sample.cosmic_phenotype_id=apoc.text.join(["COSO",toString(line.cosmic_phenotype_id)],""),
    sample.date_created=datetime(),
    tumor.drug_response=line.drug_response,
    tumor.grade=line.grade,
    tumor.age_at_tumour_tumour_recurrence=toInteger(line.age_at_tumour_tumour_recurrence),
    tumor.stage=line.stage,
    tumor.cytogenetics=line.cytogenetics,
    tumor.tumor_source=line.tumour_source,
    tumor.tumor_remark=line.tumour_remark,
    tumor.nci_code=line.nci_code,
    tumor.nci_url=replace('https://ncit.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI%20Thesaurus&code=XXXX','XXXX',line.nci_code),
    tumor.date_created=datetime(),
    patient.metastatic_site=line.metastatic_site,
    patient.age=toInteger(line.age),
    patient.ethnicity=line.ethnicity,
    patient.environmental_variables=line.environmental_variables,
    patient.therapy=line.therapy,
    patient.family=toUpper(line.family),
    patient.normal_tissue_tested=toUpper(line.normal_tissue_tested),
    patient.gender=toUpper(line.gender),
    patient.individual_remark=line.individual_remark,
    patient.datecreated=datetime()
} IN TRANSACTIONS OF 1000 ROWS;

MATCH (cs:CosmicSample) WHERE cs.cosmic_phenotype_id IS NOT NULL
MATCH(cc:CosmicClassification) WHERE cc.phenotype_id = cs.cosmic_phenotype_id
MERGE (cs)-[r:HAS_CALSSIFICATION]->(cc)
;