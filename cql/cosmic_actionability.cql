CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/Actionability/ActionabilityData.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MERGE (action:CosmicActionability {actionability_id:apoc.create.uuid()})
SET action.gene_symbol=nodeRecord.GENE,
  action.mutation_remark=nodeRecord.MUTATION_REMARK,
  action.genomic_mutation_id=nodeRecord.GENOMIC_MUTATION_ID,
  action.fusion_id=nodeRecord.FUSION_ID,
  action.mutation_aa_syntax=nodeRecord.MUTATION_AA_SYNTAX,
  action.disease=nodeRecord.DISEASE,
  action.actionability_rank=toInteger(nodeRecord.ACTIONABILITY_RANK),
  action.development_status=nodeRecord.DEVELOPMENT_STATUS,
  action.drug_combination=nodeRecord.DRUG_COMBINATION,
  action.testing_required=nodeRecord.TESTING_REQUIRED,
  action.trial_status=nodeRecord.TRIAL_STATUS,
  action.trial_id=nodeRecord.TRIAL_ID,
  action.completion_status=nodeRecord.COMPLETION_STATUS,
  action.trial_completion_date=nodeRecord.RIAL_PRIMARY_COMPLETION_DATE,
  action.patient_prescreening=nodeRecord.PATIENT_PRESCREENING,
  action.results_availability=nodeRecord.RESULTS_AVAILABILITY,
  action.progression_remark=nodeRecord.PROGRESSION_REMARK,
  action.source_type=nodeRecord.SOURCE_TYPE,
  action.source=nodeRecord.SOURCE,
  action.treatment_history=nodeRecord.TREATMENT_HISTORY,
  action.patient_age=toInteger(nodeRecord.PATIENT_AGE),
  action.primary_outcome_measure=nodeRecord.PRIMARY_OUTCOME_MEASURE,
  action.number_of_patients=toInteger(nodeRecord.NUMBER_OF_PATIENTS),
  action.treated_number=toInteger(nodeRecord.TREATED_NUMBER),
  action.control_number=toInteger(nodeRecord.CONTROL_NUMBER),
  action.control_treatment=nodeRecord.CONTROL_TREATMENT,
  action.orr_treat=toFloat(nodeRecord.ORR_TREAT),
  action.orr_con=toFloat(nodeRecord.ORR_CON),
  action.orr_ci=nodeRecord.ORR_CI,
  action.orr_pval=nodeRecord.ORR_PVAL,  // persist as String; may have < sign
  action.dor_treat=toFloat(nodeRecord.DOR_TREAT),
  action.dor_con=toFloat(nodeRecord.DOR_CON),
  action.dor_ci=toFloat(nodeRecord.DOR_CI),
  action.dor_pval=nodeRecord.DOR_PVAL,  // persist as String; may have < sign
  action.pfs_treat=toFloat(nodeRecord.PFS_TREAT),
  action.pfs_con=toFloat(nodeRecord.PFS_CON),
  action.pfs_ci=nodeRecord.PFS_CI,
  action.pfs_pval=nodeRecord.PFS_PVAL, // persist as String; may have < sign
  action.ttp_treat=toFloat(nodeRecord.TTP_TREAT),
  action.ttp_con=toFloat(nodeRecord.TTP_CON),
  action.ttp_hr=nodeRecord.TTP_HR,
  action.ttp_ci=nodeRecord.TTP_CI,
  action.ttp_pval=nodeRecord.TTP_PVAL, // persist as String; may have < sign
  action.dcr_treat=toFloat(nodeRecord.DCR_TREAT),
  action.dcr_con=toFloat(nodeRecord.DCR_CON),
  action.dcr_pval=nodeRecord.DCR_PVAL, // persist as String; may have < sign
  action.os_treat=toFloat(nodeRecord.OS_TREAT),
  action.os_con=toFloat(nodeRecord.OS_CON),
  action.os_hr=nodeRecord.OS_HR,
  action.os_ci=nodeRecord.OS_CI,
  action.os_pval=nodeRecord.OS_PVAL,
  action.obj_rr_treat=nodeRecord.OBJ_RR_TREAT,
  action.cr_count=toInteger(nodeRecord.CR_COUNT),
  action.pr_count=toInteger(nodeRecord.PR_COUNT),
  action.sd_count=toInteger(nodeRecord.SD_COUNT),
  action.rfs_treat=toFloat(nodeRecord.RFS_TREAT),
  action.rfs_con=toFloat(nodeRecord.RFS_CON),
  action.rfs_hr=nodeRecord.RFS_HR,
  action.rfs_ci=nodeRecord.RFS_CI,
  action.rfs_pval=nodeRecord.RFS_PVAL,
  action.blood_response=nodeRecord.BLOOD_RESPONSE,
  action.response_value=nodeRecord.RESPONSE_VALUE,
  action.timepoinr=nodeRecord.TIMEPOINT,
  action.classification_id=nodeRecord.CLASSIFICATION_ID,
  action.last_update=nodeRecord.LAST_UPDATED
',
{batchSize:4000, iterateList:true});

// add clinical trials URL property
MATCH(act:CosmicActionability) WHERE act.trial_id IS NOT NULL
SET act.drug_trial_url = replace("https://clinicaltrials.gov/ct2/show/XXXX","XXXX", act.trial_id)
;

