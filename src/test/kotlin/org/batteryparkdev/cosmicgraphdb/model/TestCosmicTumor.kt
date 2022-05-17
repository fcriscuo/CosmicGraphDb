package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicTumor {

    fun parseCosmicTumorFile(filename: String): Int {
        val LIMIT = Long.MAX_VALUE
        // limit the number of records processed
        deleteTumorNodes()
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicTumor.parseValueMap(it) }
            .forEach { tumor ->
                println("Loading tumor ${tumor.sampleId}")
                val cypher = tumor.generateCosmicTumorCypher().plus(
                    " RETURN ${CosmicTumor.nodename}"
                )
                Neo4jConnectionService.executeCypherCommand(cypher)
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (ct:CosmicTumor) RETURN COUNT(ct)").toInt()
    }

    private fun deleteTumorNodes() =
        Neo4jConnectionService.executeCypherCommand("MATCH (ct: CosmicTumor) DETACH DELETE (ct)")
}

fun main() {
    val cosmicTumorFile = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicMutantExportCensus.tsv")
    val recordCount =
        TestCosmicTumor().parseCosmicTumorFile(cosmicTumorFile)
    println("CosmicTumor record count = $recordCount")
}