package org.batteryparkdev.cosmicgraphdb.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.model.CosmicMutation
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneDao.createCosmicGeneNode

private val logger: FluentLogger = FluentLogger.forEnclosingClass()

fun loadCosmicMutation(cosmicMutation: CosmicMutation): Int =
    Neo4jConnectionService.executeCypherCommand(
        "MERGE " +
                " (cm:CosmicMutation{mutation_id: ${cosmicMutation.mutationId}}) " +
                " SET cm.genomic_mutation_id = \"${cosmicMutation.genomicMutationId}\", " +
                "  cm.mutation_cds = \"${cosmicMutation.mutationCds}\", " +
                "  cm.mutation_aa = \"${cosmicMutation.mutationAA}\", " +
                "  cm.mutation_description = \"${cosmicMutation.mutationDescription}\", " +
                "   cm.mutation_zygosity = \"${cosmicMutation.mutationZygosity}\", " +
                "   cm.loh = \"${cosmicMutation}\", cm.grch = \"${cosmicMutation.GRCh}\", " +
                "   cm.mutation_strand = \"${cosmicMutation.mutationStrand}\", " +
                "   cm.resistance_mutation = \"${cosmicMutation.resistanceMutation}\", " +
                "   cm.fathmm_prediction = \"${cosmicMutation.fathmmPrediction}\", " +
                "   cm.fathmm_score = ${cosmicMutation.fathmmScore}, " +
                "   cm.mutation_somatic_status = \"${cosmicMutation.mutationSomaticStatus}\", " +
                "   cm.hgvsp = \"${cosmicMutation.hgvsp}\", " +
                "   cm.hgvsc = \"${cosmicMutation.hgvsc}\", " +
                "   cm.hgvsg = \"${cosmicMutation.hgvsg}\", cm.tier = \"${cosmicMutation.tier}\"" +
                "   RETURN cm.mutation_id "
    ).toInt()

fun createCosmicMutationToGeneRelationship(geneSymbol: String, mutation_id: Int) {
    //  ensure that at least a minimum CancerGene node exists
    // no effect if CancerGene node already exists
    createCosmicGeneNode(geneSymbol)
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cm:CosmicMutation), (cg:CosmicGene) WHERE cg.gene_symbol = \"$geneSymbol\" " +
                " AND cm.mutation_id = $mutation_id MERGE (cm) -" +
                "[r: HAS_COSMIC_GENE] ->(cg) "
    )
}