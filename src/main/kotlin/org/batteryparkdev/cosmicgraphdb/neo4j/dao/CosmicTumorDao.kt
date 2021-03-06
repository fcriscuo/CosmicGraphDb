package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicTumor
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import org.batteryparkdev.cosmicgraphdb.pubmed.loader.PubMedLoader

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
                    " RETURN  ct.tumor_id").toInt()
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
    if(cosmicTumor.pubmedId > 0 ) {
        // if (!PubMedLoader.pubMedNodeExistsPredicate(cosmicTumor.pubmedId)){
        PubMedLoader.loadPubMedEntryById(cosmicTumor.pubmedId)
        PubMedLoader.addPubMedLabel(cosmicTumor.pubmedId, "CosmicArticle")
        // }
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (ct:CosmicTumor), (pma:PubMedArticle) " +
                    " WHERE ct.tumor_id = ${cosmicTumor.tumorId} AND " +
                    " pma.pubmed_id = ${cosmicTumor.pubmedId} MERGE " +
                    " (ct) -[r:HAS_PUBMED_ARTICLE] ->(pma)"
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


fun loadCosmicTumor(cosmicTumor: CosmicTumor):Int  =
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
    Neo4jUtils.nodeLoadedPredicate(
        "OPTIONAL MATCH (ct:CosmicTumor{tumor_id: $tumorId }) " +
                " RETURN ct IS NOT NULL AS PREDICATE"
    )