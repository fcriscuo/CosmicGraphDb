package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicClassification {
    /*
    Test function to parse 100 records from the Cosmic classification.csv file
     */

    fun parseClassificationFile(filename: String): Int {
        val LIMIT = Long.MAX_VALUE
        deleteClassificationNodes()
        ApocFileReader.processDelimitedFile(filename).stream()
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicClassification.parseValueMap(it) }
            .forEach {
                println(
                    "CosmicClassification: ${it.resolveClassificationId()} " +
                            " NCIcode: ${it.nciCode}" +
                            " Primary Site: ${it.siteType.primary} " +
                            " Histology: ${it.histologyType.primary}"
                )
                Neo4jConnectionService.executeCypherCommand(it.generateCosmicClassificationCypher())
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (cc: CosmicClassification) RETURN COUNT(cc)").toInt()
    }
    private fun deleteClassificationNodes() =
        Neo4jConnectionService.executeCypherCommand("MATCH (cc: CosmicClassification) DETACH DELETE (cc)")
}

fun main(args: Array<String>) {
    println("Using Neo4j server at ${System.getenv("NEO4J_URI")}")
    val cosmicClassificationFile =  ConfigurationPropertiesService.resolveCosmicSampleFileLocation("classification.csv")
    println("Loading data from file: $cosmicClassificationFile")
    val apoc = TestCosmicClassification()
    val recordCount = apoc.parseClassificationFile(cosmicClassificationFile)
    println("Loaded CosmicClassification  record count = $recordCount")
}