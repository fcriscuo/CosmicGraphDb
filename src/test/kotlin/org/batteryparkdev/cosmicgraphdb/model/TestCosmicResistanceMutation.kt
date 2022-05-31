package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicResistanceMutation {
    private val LIMIT = Long.MAX_VALUE
    fun parseCosmicFusionFile(filename: String): Int {
        deleteExistingResistanceNodes()
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicResistanceMutation.parseValueMap(it) }
            .forEach { mutation ->
                Neo4jConnectionService.executeCypherCommand(mutation.generateCosmicResistanceCypher())
                println("Loaded CosmicResistanceMutation: ${mutation.mutationId}")
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (dr: DrugResistance) RETURN COUNT(dr)").toInt()
    }

    private fun deleteExistingResistanceNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("DrugResistance")
    }
}
fun main() {
    val cosmicResistanceFile = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicResistanceMutations.tsv")
    val recordCount =
        TestCosmicResistanceMutation().parseCosmicFusionFile(cosmicResistanceFile)
    println("Processed $cosmicResistanceFile record count = $recordCount")
}