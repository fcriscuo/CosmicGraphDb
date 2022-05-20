package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicStruct {
    private val LIMIT = Long.MAX_VALUE

    fun parseCosmicStructFile(filename: String): Int {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicStruct")
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicStruct.parseValueMap(it) }
            .forEach { struct ->
                println("Loading CosmicStruct  ${struct.mutationId}")
                Neo4jConnectionService.executeCypherCommand(struct.generateStructCypher())
                struct.createPubMedRelationship(struct.pubmedId)
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (cs:CosmicStruct) RETURN COUNT(cs)").toInt()
    }
}

fun main() {
    println("Using Neo4j server at ${System.getenv("NEO4J_URI")}")
    val cosmicStructFile = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicStructExport.tsv")
    println("Loading data from file: $cosmicStructFile")
    val recordCount = TestCosmicStruct().parseCosmicStructFile(cosmicStructFile)
    println("Struct record count = $recordCount")
}