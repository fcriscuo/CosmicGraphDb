:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/ActionabilityData.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line

MERGE (action:CosmicActionability {actionability_id:apoc.create.uuid()})
SET action.gene_symbol=line.GENE,
  action.mutation_remark=line.MUTATION_REMARK,
  action.genomic_mutation_id=line.GENOMIC_MUTATION_ID,
  action.fusion_id=line.FUSION_ID,
  action.mutation_aa_syntax=line.MUTATION_AA_SYNTAX,
  action.disease=line.DISEASE,
  action.actionability_rank=toInteger(line.ACTIONABILITY_RANK),
  action.development_status=line.DEVELOPMENT_STATUS,
  action.drug_combination=line.DRUG_COMBINATION,
  action.testing_required=line.TESTING_REQUIRED,
  action.trial_status=line.TRIAL_STATUS,
  action.trial_id=line.TRIAL_ID,
  action.completion_status=line.COMPLETION_STATUS,
  action.trial_completion_date=line.RIAL_PRIMARY_COMPLETION_DATE,
  action.patient_prescreening=line.PATIENT_PRESCREENING,
  action.results_availability=line.RESULTS_AVAILABILITY,
  action.progression_remark=line.PROGRESSION_REMARK,
  action.source_type=line.SOURCE_TYPE,
  action.source=line.SOURCE,
  action.treatment_history=line.TREATMENT_HISTORY,
  action.patient_age=toInteger(line.PATIENT_AGE),
  action.primary_outcome_measure=line.PRIMARY_OUTCOME_MEASURE,
  action.number_of_patients=toInteger(line.NUMBER_OF_PATIENTS),
  action.treated_number=toInteger(line.TREATED_NUMBER),
  action.control_number=toInteger(CONTROL_NUMBER),
  action.control_treatment=line.CONTROL_TREATMENT,
  action.orr_treat=toFloat(line.ORR_TREAT),
  action.orr_con=toFloat(line.ORR_CON),
  action.orr_ci=line.ORR_CI,
  action.orr_pval=line.ORR_PVAL,  // persist as Sting; may have < sign
  action.dor_treat=toFloat(line.DOR_TREAT),
  action.dor_con=toFloat(line.DOR_CON),
  action.dor_ci=toFloat(line.DOR_CI),
  action.dor_pval=line.DOR_PVAL,  // persist as Sting; may have < sign
  action.pfs_treat=toFloat(line.PFS_TREAT),
  action.pfs_con=toFloat(line.PFS_CON),
  action.pfs_ci=line.PFS_CI,
  action.pfs_pval=line.PFS_PVAL, // persist as Sting; may have < sign
  action.ttp_treat=toFloat(line.TTP_TREAT),
  action.ttp_con=toFloat(line.TTP_CON),
  action.ttp_hr=line.TTP_HR,
  action.ttp_ci=line.TTP_CI,
  action.ttp_pval=line.TTP_PVAL, // persist as Sting; may have < sign
  action.dcr_treat=toFloat(line.DCR_TREAT),
  action.dcr_con=toFloat(line.DCR_CON),
  action.dcr_pval=line.DCR_PVAL, // persist as Sting; may have < sign
  action.os_treat=toFloat(line.OS_TREAT),
  action.os_con=toFloat(line.OS_CON),
  action.os_hr=line.OS_HR,
  action.os_ci=line.OS_CI,
  action.os_pval=line.OS_PVAL,
  action.obj_rr_treat=line.OBJ_RR_TREAT,
  action.cr_count=toInteger(line.CR_COUNT),
  action.pr_count=toInteger(line.PR_COUNT),
  action.sd_count=toInteger(line.SD_COUNT),
  action.rfs_treat=toFloat(line.RFS_TREAT),
  action.rfs_con=toFloat(line.RFS_CON),
  action.rfs_hr=line.RFS_HR,
  action.rfs_ci=line.RFS_CI,
  action.rfs_pval=line.RFS_PVAL,
  action.blood_response=line.BLOOD_RESPONSE,
  action.response_value=line.RESPONSE_VALUE,
  action.timepoinr=line.TIMEPOINT,
  action.classification_id=line.CLASSIFICATION_ID,
  action.last_update=line.LAST_UPDATED,
  action.date_created=datetime()
} IN TRANSACTIONS OF 1000 ROWS;

// create CosmicClassification to Actionability relationship
MATCH (act:CosmicActionability) WHERE act.classification_id IS NOT NULL
MATCH (clas:CosmicClassification) WHERE class.phenotype_id = act.classification_id
MERGE (class)-[r:HAS_ACTIONAVAILABILY]->(act)

// add clinical trials URL property
MATCH(act:CosmicActionability) WHERE act.trial_id IS NOT NULL
SET act.drug_trial_url = replace("https://clinicaltrials.gov/ct2/show/XXXX","XXXX", act.trial_id)
;

