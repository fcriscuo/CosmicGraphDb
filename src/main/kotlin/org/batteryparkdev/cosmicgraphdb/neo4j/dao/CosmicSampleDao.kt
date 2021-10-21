package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils

private val logger: FluentLogger = FluentLogger.forEnclosingClass()

/*
  Function to create a basic CosmicSample node as a placeholder
  for subsequent completion if necessary
   */
fun createCosmicSampleNode(sampleId: Int): Int {
    return when (cosmicTumorIdLoaded(sampleId)) {
        true -> sampleId
        false -> Neo4jConnectionService.executeCypherCommand(
            "MERGE (cs:CosmicSample{sample_id: $sampleId }) " +
                    " RETURN  cs.sample_id").toInt()
    }
}

/*
Function to determine if CosmicSample node exists
 */
fun cosmicSampleIdLoaded(sampleId: Int): Boolean =
    Neo4jUtils.nodeLoadedPredicate(
        "OPTIONAL MATCH (cs:CosmicSample{sample_id: $sampleId }) " +
                " RETURN ct IS NOT NULL AS PREDICATE"
    )