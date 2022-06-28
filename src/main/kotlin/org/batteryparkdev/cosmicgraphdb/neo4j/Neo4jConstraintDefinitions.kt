package org.batteryparkdev.cosmicgraphdb.neo4j

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.neo4j.service.Neo4jConnectionService

/*
A collection of Neo4j database constraint definitions in Cypher
These constraints should be defined prior to loading the database with
any initial data.
 */
val constraints by lazy {
    listOf<String>(
        "CREATE CONSTRAINT unique_classification_id IF NOT EXISTS ON (c:CosmicClassification) ASSERT c.phenotype_id IS UNIQUE",
        "CREATE CONSTRAINT unique_individual_id IF NOT EXISTS ON (i:CosmicIndividual) ASSERT i.individual_id IS UNIQUE",
        "CREATE CONSTRAINT unique_classification_type_id IF NOT EXISTS ON (t:ClassificationType) ASSERT t.type_id IS UNIQUE",
        "CREATE CONSTRAINT unique_cosmic_gene_symbol IF NOT EXISTS ON (cg: CosmicGene) ASSERT cg.gene_symbol IS UNIQUE",
        "CREATE CONSTRAINT unique_cosmic_hgnc_id IF NOT EXISTS ON (hgnc: CosmicHGNC) ASSERT hgnc.hgnc_id IS UNIQUE",
        "CREATE CONSTRAINT unique_cosmic_hallmark_id IF NOT EXISTS ON (hm: CosmicHallmark) ASSERT hm.hallmark_id IS UNIQUE",
        "CREATE CONSTRAINT unique_cosmic_hallmark_collect IF NOT EXISTS ON (hmc: CosmicHallmarkCollection) ASSERT hmc.gene_symbol IS UNIQUE",
        "CREATE CONSTRAINT unique_entrez_id IF NOT EXISTS ON (ent:Entrez) ASSERT ent.entrez_id IS UNIQUE",
        "CREATE CONSTRAINT unique_annotation_id IF NOT EXISTS ON (ca:CosmicAnnotation) ASSERT ca.annotation_value IS UNIQUE",
        "CREATE CONSTRAINT unique_type_id IF NOT EXISTS ON (ct:CosmicType) ASSERT ct.type_id IS UNIQUE",
        "CREATE CONSTRAINT unique_mutation_id IF NOT EXISTS ON (cm:CosmicCodingMutation) ASSERT cm.mutation_id IS UNIQUE",
        "CREATE CONSTRAINT unique_sample_id IF NOT EXISTS ON (cs:CosmicSample) ASSERT cs.sample_id IS UNIQUE",
        "CREATE CONSTRAINT unique_gene_expression_id IF NOT EXISTS ON (cge:CosmicGeneExpression) ASSERT cge.key IS UNIQUE",
        "CREATE CONSTRAINT unique_breakpoint_id IF NOT EXISTS ON (cb:CosmicBreakpoint) ASSERT cb.breakpoint_id IS UNIQUE",
        "CREATE CONSTRAINT unique_struct_id IF NOT EXISTS ON (cs:CosmicStruct) ASSERT cs.mutation_id IS UNIQUE",
        "CREATE CONSTRAINT unique_ncv_id IF NOT EXISTS ON (cn:CosmicNCV) ASSERT cn.genomic_mutation_id IS UNIQUE",
        "CREATE CONSTRAINT unique_drug_resistance_mut_id IF NOT EXISTS ON (dr:DrugResistance) ASSERT dr.mutation_id IS UNIQUE",
        "CREATE CONSTRAINT unique_cna_id IF NOT EXISTS ON (cna:CosmicCompleteCNA) ASSERT cna.cna_id IS UNIQUE",
        "CREATE CONSTRAINT unique_fusion_mut_id IF NOT EXISTS ON (cf:CosmicFusion) ASSERT cf.fusion_id IS UNIQUE",
        "CREATE CONSTRAINT unique_diff_methylation_id IF NOT EXISTS ON (cdm:CosmicDiffMethylation) ASSERT cdm_key IS UNIQUE",
        "CREATE CONSTRAINT unique_complete_cna_id IF NOT EXISTS ON (cna:CosmicCompleteCNA) ASSERT cna.cnv_id IS UNIQUE"
    )
}

val logger: FluentLogger = FluentLogger.forEnclosingClass();

fun defineConstraints() {
    constraints.forEach {
        Neo4jConnectionService.defineDatabaseConstraint(it)
        logger.atInfo().log("Constraint: $it  has been defined")
    }
}

// stand-alone invocation
fun main(){
    defineConstraints()
}