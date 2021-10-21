package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils

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

/*
Function to determine if CosmicTumor node exists
 */
fun cosmicTumorIdLoaded(tumorId: Int): Boolean =
    Neo4jUtils.nodeLoadedPredicate(
        "OPTIONAL MATCH (ct:CosmicTumor{tumor_id: $tumorId }) " +
                " RETURN ct IS NOT NULL AS PREDICATE"
    )