package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicMutation
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import kotlin.math.cos

/*
Responsible for loading data from a CosmicMutation model instance into the Neo4j database
Creates a  CosmicMutation -> CosmicGene relationship
 */

object CosmicMutationLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    fun processCosmicMutation(cosmicMutation: CosmicMutation) {
        val id = loadCosmicMutation(cosmicMutation)
        createCosmicGeneRelationship(cosmicMutation.geneName, id)
    }

   private  fun loadCosmicMutation(cosmicMutation: CosmicMutation):Int =
        Neo4jConnectionService.executeCypherCommand(
            "MERGE " +
                    " (cm:CosmicMutation{mutation_id: ${cosmicMutation.mutationId}}) " +
                    " SET cm.genomic_mutation_id = \"${cosmicMutation.genomicMutationId}\", " +
                    "  cm.mutation_cds = \"${cosmicMutation.mutationCds}\", " +
                    "  cm.mutation_aa = \"${cosmicMutation.mutationAA}\", " +
                    "  cm.muation_description = \"${cosmicMutation.mutationDescription}\", " +
                    "   cm.mutation_zygosity = \"${cosmicMutation.mutationZygosity}\", " +
                    "   cm.loh = \"${cosmicMutation}\", cm.grch = ${cosmicMutation.GRCh}, " +
                    "   cm.mutation_strand = \"${cosmicMutation.mutationStrand}\", " +
                    "   cm.snp = \"${cosmicMutation.SNP}\", " +
                    "   cm.resistance_mutation = \"${cosmicMutation.resistanceMutation}\", " +
                    "   cm.fathmm_prediction = \"${cosmicMutation.fathmmPrediction}\", " +
                    "   cm.fathmm_score = ${cosmicMutation.fathmmScore}, " +
                    "   cm.mutation_somatic_status = \"${cosmicMutation.mutationSomaticStatus}\", " +
                    "   cm.hgvsp = \"${cosmicMutation.hgvsp}\", " +
                    "   cm.hgvsc = \"${cosmicMutation.hgvsc}\", " +
                    "   cm.hgvsg = \"${cosmicMutation.hgvsg}\", cm.tier = \"${cosmicMutation.tier}\"" +
                    "   RETURN cm.mutation_id ").toInt()

    private fun createCosmicGeneRelationship (geneName: String, mutation_id: Int) {
        if (CosmicGeneLoader.cancerGeneNameLoaded(geneName)) {
            Neo4jConnectionService.executeCypherCommand(
                "MATCH (cm:CosmicMutation), (cg:CosmicGene) WHERE cg.gene_name = \"$geneName\" " +
                        " AND cm.mutation_id = $mutation_id MERGE (cm) -" +
                        "[r: HAS_COSMIC_GENE}] ->(cg) "
            )
        } else {
            logger.atWarning().log("Unable to resolve gene name $geneName for mutation id $mutation_id")
        }
    }
}