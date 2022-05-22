package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicClassification {
    fun parseClassificationFile(filename: String): Int {
        val LIMIT = Long.MAX_VALUE
        deleteClassificationNodes()
        ApocFileReader.processDelimitedFile(filename).stream()
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicClassification.parseValueMap(it) }
            .forEach {
                if (Neo4jUtils.nodeExistsPredicate(it.getNodeIdentifier()).not()) {
                    Neo4jConnectionService.executeCypherCommand(it.generateCosmicClassificationCypher())
                    println(
                        "CosmicClassification: ${it.cosmicPhenotypeId} " +
                                " NCIcode: ${it.nciCode}" +
                                " Primary Site: ${it.siteType.primary} " +
                                " Histology: ${it.histologyType.primary}"
                    )
                } else {
                    println("+++++ Duplicate phenotype id: ${it.cosmicPhenotypeId} skipped.")
                }
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