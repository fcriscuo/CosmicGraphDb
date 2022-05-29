package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicCompleteGeneExpressionLoader {
    fun loadGeneExpressFile(filename: String): Int {
        deleteGeneExpressionNodes()
        println("Loading COSMIC complete gene expression data from: $filename")
        CosmicCompleteGeneExpressionLoader.loadCosmicCompleteGeneExpressionData(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (ce: CompleteGeneExpression) RETURN COUNT(ce)").toInt()
    }

    private  fun deleteGeneExpressionNodes(){
        Neo4jUtils.detachAndDeleteNodesByName("CompleteGeneExpression")
    }
}
fun main(args: Array<String>) {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicCompleteGeneExpression.tsv")
    val recordCount = TestCosmicCompleteGeneExpressionLoader().loadGeneExpressFile(filename)
    println("Loaded $recordCount CompleteGeneExpression records")

}