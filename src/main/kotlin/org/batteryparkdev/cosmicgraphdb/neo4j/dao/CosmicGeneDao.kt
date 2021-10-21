package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils

object CosmicGeneDao {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()


    /*
    Function to create a basic CosmicGene node as a placeholder
    for subsequent completion if necessary
     */
    fun createCosmicGeneNode(geneSymbol: String): String {
        return when (cancerGeneSymbolLoaded(geneSymbol)) {
            true -> geneSymbol
            false -> Neo4jConnectionService.executeCypherCommand(
                "MERGE (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
                        " RETURN  cg.gene_symbol"
            )
        }
    }
}

/*
Function to determine if CosmicGene node exists
 */
fun cancerGeneSymbolLoaded(geneSymbol: String): Boolean =
    Neo4jUtils.nodeLoadedPredicate(
        "OPTIONAL MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
                " RETURN cg IS NOT NULL AS PREDICATE"
    )