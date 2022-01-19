package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicBreakpoint
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import org.batteryparkdev.cosmicgraphdb.pubmed.dao.PubMedArticleDao
import org.batteryparkdev.cosmicgraphdb.pubmed.model.PubMedIdentifier


object CosmicBreakpointDao {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    private const val cypherLoadTemplate = "MERGE (cb:CosmicBreakpoint{mutation_id: MUTATIONID}) " +
            "SET cb += {sample_name: SAMPLENAME, sample_id: SAMPLEID, tumor_id: TUMORID, " +
            " chromosome_from: CHROMOSOMEFROM, location_from_min: LOCATIONFROMMIN, " +
            " location_from_max: LOCATIONFROMMAX, strand_from: STRANDFROM, " +
            " chromosome_to: CHROMOSOMETO, location_to_min: LOCATIONTOMIN, " +
            " location_to_max: LOCATIONTOMAX, strand_to: STRANDTO} " +
            " RETURN cb.mutation_id "
    private const val mutationLabel = "Mutation"

    fun loadCosmicBreakpointNode(cosmicBreakpoint: CosmicBreakpoint): Int {
        val merge = cypherLoadTemplate.replace(
            "MUTATIONID", cosmicBreakpoint.mutationId.toString()
        )
            .replace("SAMPLENAME", Neo4jUtils.formatQuotedString(cosmicBreakpoint.sampleName))
            .replace("SAMPLEID", cosmicBreakpoint.sampleId.toString())
            .replace("TUMORID", cosmicBreakpoint.tumorId.toString())
            .replace("CHROMOSOMEFROM", Neo4jUtils.formatQuotedString(cosmicBreakpoint.chromosomeFrom))
            .replace("LOCATIONFROMMIN", cosmicBreakpoint.locationFromMin.toString())
            .replace("LOCATIONFROMMAX", cosmicBreakpoint.locationFromMax.toString())
            .replace("STRANDFROM", Neo4jUtils.formatQuotedString(cosmicBreakpoint.strandFrom))
            .replace("CHROMOSOMETO", Neo4jUtils.formatQuotedString(cosmicBreakpoint.chromosomeTo))
            .replace("LOCATIONTOMIN", cosmicBreakpoint.locationToMin.toString())
            .replace("LOCATIONTOMAX", cosmicBreakpoint.locationToMax.toString())
            .replace("STRANDTO", Neo4jUtils.formatQuotedString(cosmicBreakpoint.strandTo))
        return Neo4jConnectionService.executeCypherCommand(merge).toInt()
    }

    fun addMutationLabel(mutationId: Int) =
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (cb:CosmicBreakpoint{ mutation_id: $mutationId}) " +
                    " WHERE apoc.label.exists(cg,\"$mutationLabel\")  = false " +
                    " CALL apoc.create.addLabels(cq, [\"$mutationLabel\"] ) yield node return node"
        )

    fun createPubMedRelationship(cosmicBreakpoint: CosmicBreakpoint) {
        if (cosmicBreakpoint.pubmedId > 0) {
            // if the PubMed article has not been loaded yet, create a placeholder node
            if (!PubMedArticleDao.pubMedNodeExistsPredicate(cosmicBreakpoint.pubmedId)) {
                val identifier = PubMedIdentifier(cosmicBreakpoint.pubmedId, 0, "CosmicArticle")
                PubMedArticleDao.createPlaceholderNode(identifier)
            }
            Neo4jConnectionService.executeCypherCommand(
                "MATCH (cb:CosmicBreakpoint), (pma:PubMedArticle) " +
                        " WHERE cb.mutation_id = ${cosmicBreakpoint.mutationId} AND " +
                        " pma.pubmed_id = ${cosmicBreakpoint.pubmedId} MERGE " +
                        " (ct) -[r:HAS_COSMIC_ARTICLE] ->(pma)"
            )
        }
    }

    fun createCosmicTumorRelationship(cosmicBreakpoint: CosmicBreakpoint) =
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (ct:CosmicTumor), (cb:CosmicBreakpoint) " +
                    " WHERE ct.tumor_id = ${cosmicBreakpoint.tumorId} AND " +
                    " cb.tumor_id = ${cosmicBreakpoint.tumorId} MERGE " +
                    " (ct) -[r:HAS_BREAKPOINT] ->(cb)"
        )
    /*
Function to determine if CosmicBreakpoint node exists
 */
    fun cosmicBreakpointLoaded(mutationId: Int): Boolean =
        Neo4jUtils.nodeLoadedPredicate(
            "OPTIONAL MATCH (cb:CosmicBreakpoint{mutation_id: $mutationId }) " +
                    " RETURN cb IS NOT NULL AS PREDICATE"
        )
}