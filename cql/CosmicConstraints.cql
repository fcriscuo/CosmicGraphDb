CREATE CONSTRAINT `imp_uniq_CosmicGeneHallmark_HALLMARK_ID` IF NOT EXISTS
FOR (n: `CosmicGeneHallmark`)
REQUIRE (n.`hallmark_id`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicGeneHallmarkCollection_HALLMARK_ID` IF NOT EXISTS
FOR (n: `CosmicGeneHallmark`)
REQUIRE (n.`gene_symbol`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicSample_sample_id` IF NOT EXISTS
FOR (n: `CosmicSample`)
REQUIRE (n.`sample_id`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicSampleMutationCollection_sample_id` IF NOT EXISTS
FOR (n: `CosmicSampleMutationCollection`)
REQUIRE (n.`sample_id`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_Patient_id_individual` IF NOT EXISTS
FOR (n: `Patient`)
REQUIRE (n.`id_individual`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_Tumor_id_tumour` IF NOT EXISTS
FOR (n: `Tumor`)
REQUIRE (n.`id_tumour`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicGene_Gene_Symbol` IF NOT EXISTS
FOR (n: `CosmicGene`)
REQUIRE (n.`Gene Symbol`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicGeneMutationCollection_Gene_Symbol` IF NOT EXISTS
FOR (n: `CosmicGeneMutationCollection`)
REQUIRE (n.`Gene Symbol`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicMutation_MUTATION_ID` IF NOT EXISTS
FOR (n: `CosmicMutation`)
REQUIRE (n.`mutation_id`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicSampleClassification_COSMIC_PHENOTYPE_ID` IF NOT EXISTS
FOR (n: `CosmicSampleClassification`)
REQUIRE (n.`cosmic_phenotype_id`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicGeneMutationCollection_Gene_Symbol` IF NOT EXISTS
FOR (n: `CosmicGeneMutationCollection`)
REQUIRE (n.`gene_symbol`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicStructuralMutation_MUTATION_ID` IF NOT EXISTS
FOR (n: `CosmicStucturalMutation`)
REQUIRE (n.`mutation_id`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicSampleMutationCollection_sample_id` IF NOT EXISTS
FOR (n: `CosmicSampleMutationCollection`)
REQUIRE (n.`sample_id`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicResistanceMutation_MUTATION_ID` IF NOT EXISTS
FOR (n: `CosmicResistanceMutation`)
REQUIRE (n.`identifier`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicBreakpointMutation_IMUTATION_ID` IF NOT EXISTS
FOR (n: `CosmicBreakpointMutation`)
REQUIRE (n.`mutation_id`) IS UNIQUE;

CREATE CONSTRAINT `imp_uniq_CosmicActionability_ID` IF NOT EXISTS
FOR(n:`CosmicActionability`)
REQUIRE (n.`actionability_id`) IS UNIQUE;

// Publication Constraint
CREATE CONSTRAINT `imp_uniq_Publication_pub_id` IF NOT EXISTS
FOR (n: `Publication`)
REQUIRE (n.`pub_id`) IS UNIQUE;

// CosmicCMC Constraint
CREATE CONSTRAINT `imp_uniq_CosmicCMC_legacy_mutation_id` IF NOT EXISTS
FOR (n: `CosmicCMC`)
REQUIRE (n.`legacy_mutation_id`) IS UNIQUE;

CREATE CONSTRAINT unique_site_id IF NOT exists
FOR (site:CosmicSite)
REQUIRE site.primary_site IS UNIQUE;

CREATE CONSTRAINT unique_histology_id IF NOT exists
FOR (histology:Histology)
REQUIRE histology.histology IS UNIQUE;

CREATE CONSTRAINT unique_ncv_id IF NOT EXISTS
FOR (ncv:CosmicNCV)
REQUIRE ncv.ncv_id IS UNIQUE;

CREATE CONSTRAINT unique_cna_id IF NOT EXISTS
FOR (cna:CosmicCNA)
REQUIRE cna.cna_identifier IS UNIQUE;

CREATE CONSTRAINT unique_cge_id IF NOT EXISTS
FOR (cge:CompleteGeneExpression)
REQUIRE cge.cge_identifier IS UNIQUE;

CREATE CONSTRAINT unique_mut_track_id IF NOT  EXISTS
FOR (track:CosmicMutationTracking)
REQUIRE track.mutation_id IS UNIQUE;

CREATE CONSTRAINT unique_diff_methyl_id IF NOT EXISTS
FOR (methyl:CosmicDiffMethylation)
REQUIRE methyl.methyl_identifier IS UNIQUE;

CREATE CONSTRAINT unique_gene_expression_id IF NOT EXISTS
FOR (express:CosmicGeneExpression)
REQUIRE express.express_identifier IS UNIQUE;

CREATE INDEX cosmic_sample_name_index
FOR (cs:CosmicSample)
ON (cs.sample_name);

CREATE INDEX cosmic_resist_sample_id_index
FOR (crm:CosmicResistanceMutation)
ON (crm.sample_id);

CREATE INDEX cosmic_resist_drug_name_index
FOR (crm:CosmicResistanceMutation)
ON (crm.drug_name);