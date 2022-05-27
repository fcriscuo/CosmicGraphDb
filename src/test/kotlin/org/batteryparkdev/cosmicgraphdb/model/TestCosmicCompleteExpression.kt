package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicCompleteExpression {

    fun parseCosmicCompleteExpressionFile (filename:String):Int {
        val LIMIT = Long.MAX_VALUE
        deleteGeneExpressionNodes()
        ApocFileReader.processDelimitedFile(filename).stream()
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map{CosmicCompleteGeneExpression.parseValueMap(it)}
            .forEach {
                Neo4jConnectionService.executeCypherCommand(it.generateCompleteGeneExpressionCypher())
                println("CosmicCompleteExpression for gene: ${it.geneSymbol}")
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (exp: CompleteGeneExpression) RETURN COUNT(exp)").toInt()
    }
    private fun deleteGeneExpressionNodes() =
        Neo4jConnectionService.executeCypherCommand("MATCH (exp: CompleteGeneExpression) DETACH DELETE (exp)")
}
fun main (args:Array<String>) {
    val cosmicExpressionFile =  ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CompleteGeneExpression.tsv")
    val recordCount =
        TestCosmicCompleteExpression().parseCosmicCompleteExpressionFile(cosmicExpressionFile)
    println("Processed $cosmicExpressionFile  record count = $recordCount")

}
