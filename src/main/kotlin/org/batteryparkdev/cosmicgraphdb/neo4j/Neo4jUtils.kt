package org.batteryparkdev.cosmicgraphdb.neo4j

import com.google.common.flogger.FluentLogger
import java.util.*

object Neo4jUtils {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    /*
    Function to determine if a node has already been loaded into Neo4j
     */
    fun nodeLoadedPredicate(cypherCommand: String): Boolean {
        if (cypherCommand.contains("PREDICATE", ignoreCase = true)) {
            try {
                val predicate = Neo4jConnectionService.executeCypherCommand(cypherCommand)
                when (predicate.lowercase(Locale.getDefault())) {
                    "true" -> return true
                    "false" -> return false
                }
            } catch (e: Exception) {
                logger.atSevere().log(e.message.toString())
                return false
            }
        }
        return false
    }

}