package org.batteryparkdev.cosmicgraphdb.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.model.CosmicTumor
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.neo4j.service.Neo4jUtils.nodeExistsPredicate
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.batteryparkdev.pubmed.dao.PubMedArticleDao
import org.batteryparkdev.pubmed.model.PubMedIdentifier

private val logger: FluentLogger = FluentLogger.forEnclosingClass()

/*
  Function to create a basic CosmicTumor node as a placeholder
  for subsequent completion if necessary
   */
fun createCosmicTumorNode(tumorId: Int): Int {
    return when (cosmicTumorIdLoaded(tumorId)) {
        true -> tumorId
        false -> Neo4jConnectionService.executeCypherCommand(
            "MERGE (ct:CosmicTumor{tumor_id: $tumorId }) " +
                    " RETURN  ct.tumor_id"
        ).toInt()
    }
}

fun createCosmicMutationRelationship(cosmicTumor: CosmicTumor) =
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (ct:CosmicTumor), (cm:CosmicMutation) " +
                " WHERE ct.tumor_id = ${cosmicTumor.tumorId} AND " +
                " cm.mutation_id = ${cosmicTumor.mutationId} MERGE " +
                " (ct) -[r:HAS_MUTATION] ->(cm)"
    )

fun createPubMedRelationship(cosmicTumor: CosmicTumor) {
    if (cosmicTumor.pubmedId > 0) {

        // if the PubMed article has not been loaded yet, create a placeholder node
        if (PubMedArticleDao.pubMedNodeExistsPredicate(cosmicTumor.pubmedId).not()) {
            val identifier = PubMedIdentifier(cosmicTumor.pubmedId, 0, "CosmicArticle")
            logger.atInfo().log("Loading PubMedIdentifier Id: ${identifier.pubmedId}")
            PubMedArticleDao.createPlaceholderNode(identifier)
        }
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (ct:CosmicTumor), (pma:PubMedArticle) " +
                    " WHERE ct.tumor_id = ${cosmicTumor.tumorId} AND " +
                    " pma.pubmed_id = ${cosmicTumor.pubmedId} MERGE " +
                    " (ct) -[r:HAS_COSMIC_ARTICLE] ->(pma)"
        )
    }
}

fun createCosmicSampleRelationship(cosmicTumor: CosmicTumor) =
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (ct:CosmicTumor), (cs:CosmicSample) " +
                " WHERE ct.tumor_id = ${cosmicTumor.tumorId} AND " +
                " cs.sample_id = ${cosmicTumor.sampleId} MERGE " +
                " (ct) -[r:HAS_SAMPLE] -> (cs)"
    )


fun loadCosmicTumor(cosmicTumor: CosmicTumor): Int =
    Neo4jConnectionService.executeCypherCommand(
        "MERGE (ct:CosmicTumor{tumor_id: ${cosmicTumor.tumorId}}) " +
                "SET ct.genome_wide_screen = ${cosmicTumor.genomeWideScreen}," +
                " ct.pubmed_id = ${cosmicTumor.pubmedId}, ct.study_id = \"${cosmicTumor.studyId}\", " +
                " ct.sample_type =\"${cosmicTumor.sampleType}\", ct.tumor_origin = \"${cosmicTumor.tumorOrigin}\", " +
                " ct.age = ${cosmicTumor.age}  RETURN ct.tumor_id"
    ).toInt()

/*
Function to determine if CosmicTumor node exists
 */
fun cosmicTumorIdLoaded(tumorId: Int): Boolean =
    nodeExistsPredicate(NodeIdentifier("CosmicTumor","tumor_id", tumorId.toString()))