CREATE CONSTRAINT unique_classification_id IF NOT exists FOR (cl: CosmicClassification)  REQUIRE cl.phenotype_id IS UNIQUE;
CREATE CONSTRAINT unique_site_id IF NOT exists FOR (site:CosmicSite)  REQUIRE site.primary_site IS UNIQUE;
CREATE CONSTRAINT unique_histology_id IF NOT exists FOR (histology:Histology)  REQUIRE histology.histology IS UNIQUE;
CREATE CONSTRAINT unique_gene_symbol IF NOT exists FOR (gene:CosmicGene)  REQUIRE gene.gene_symbol IS UNIQUE;
CREATE CONSTRAINT unique_gmc_gene_symbol IF NOT exists FOR (gmc:GeneMutationCollection)  REQUIRE gmc.gene_symbol IS UNIQUE;
CREATE CONSTRAINT unique_hallmark_id IF NOT exists FOR (hallmark:CosmicHallmark)  REQUIRE hallmark.hallmark_id IS UNIQUE;
CREATE CONSTRAINT unique_hallmark_gene_symbol IF NOT exists FOR (hgc:HallmarkGeneCollection)  REQUIRE hgc.gene_symbol IS UNIQUE;
CREATE CONSTRAINT unique_mutation_id IF NOT EXISTS FOR (cm:CosmicCodingMutation) REQUIRE cm.mutation_id IS UNIQUE;
CREATE CONSTRAINT unique_sample_id IF NOT EXISTS FOR (cs:CosmicSample) REQUIRE cs.sample_id IS UNIQUE;
CREATE CONSTRAINT unique_individual_id IF NOT EXISTS FOR (i:CosmicIndividual) REQUIRE i.individual_id IS UNIQUE;
CREATE CONSTRAINT unique_sample_mut_coll_id IF NOT EXISTS FOR (smc:SampleMutationCollection) REQUIRE smc.sample_id IS UNIQUE;
CREATE CONSTRAINT unique_tumor_id IF NOT EXISTS FOR (ct:CosmicTumor) REQUIRE ct.tumor_id IS UNIQUE;
CREATE CONSTRAINT unique_ncv_id IF NOT EXISTS FOR (ncv:CosmicNCV) REQUIRE ncv.ncv_id IS UNIQUE;
CREATE CONSTRAINT unique_syn_mut_id IF NOT EXISTS FOR (syn:SynonymousMutation) REQUIRE syn.synonymous_id IS UNIQUE;
CREATE CONSTRAINT unique_action_id IF NOT EXISTS FOR (act:CosmicActionability) REQUIRE act.actionability_id IS UNIQUE;
CREATE CONSTRAINT unique_cna_id IF NOT EXISTS FOR (cna:CosmicCNA) REQUIRE cna.cna_identifier IS UNIQUE;
CREATE CONSTRAINT unique_cge_id IF NOT EXISTS  FOR (cge:CompleteGeneExpression) REQUIRE cge.cge_identifier IS UNIQUE;
CREATE CONSTRAINT unique_mut_track_id IF NOT  EXISTS FOR (track:CosmicMutationTracking) REQUIRE track.mutation_id IS UNIQUE;
CREATE CONSTRAINT unique_diff_methyl_id IF NOT EXISTS FOR (methyl:CosmicDiffMethylation) REQUIRE methyl.methyl_identifier IS UNIQUE;
CREATE CONSTRAINT unique_struct_id IF NOT EXISTS FOR (struct:CosmicStruct) REQUIRE struct.mutation_id IS UNIQUE;
CREATE CONSTRAINT unique_breakpoint_id IF NOT EXISTS FOR (break:CosmicBreakpoint) REQUIRE break.mutation_id IS UNIQUE;
CREATE CONSTRAINT unique_resist_mutation_id IF NOT EXISTS FOR (resist:CosmicResistanceMutation) REQUIRE resist.resist_identifier IS UNIQUE;
CREATE CONSTRAINT unique_gene_expression_id IF NOT EXISTS FOR (express:CosmicGeneExpression) REQUIRE express.express_identifier IS UNIQUE;
CREATE CONSTRAINT unique_cmc_mutation_id IF NOT EXISTS FOR (cmc:CosmicCMC) REQUIRE cmc.legacy_mutation_id IS UNIQUE;

CREATE INDEX cosmic_sample_name_index FOR (cs:CosmicSample) ON (cs.sample_name)
CREATE INDEX cosmic_resist_sample_id_index FOR (crm:CosmicResistanceMutation) ON (crm.sample_id)
CREATE INDEX cosmic_resist_drug_name_index FOR (crm:CosmicResistanceMutation) ON (crm.drug_name)