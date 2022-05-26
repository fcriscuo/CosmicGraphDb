package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicFusion {
    private val LIMIT = Long.MAX_VALUE
    fun parseCosmicFusionFile (filename: String): Int{
        deleteExistingGeneNodes()
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map {CosmicFusion.parseValueMap(it)}
            .filter{ fusion -> fusion.isValid()}
            .forEach { fusion ->
                Neo4jConnectionService.executeCypherCommand(fusion.generateCosmicFusionCypher())
                println("Loaded fusion id: ${fusion.fusionId}")
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (cf: CosmicFusion) RETURN COUNT(cf)").toInt()
    }
    private fun deleteExistingGeneNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicFusion")
    }
}
fun main() {
    val cosmicFusionFile = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicFusionExport.tsv")
    val recordCount =
        TestCosmicFusion().parseCosmicFusionFile(cosmicFusionFile)
    println("Processed $cosmicFusionFile record count = $recordCount")
}