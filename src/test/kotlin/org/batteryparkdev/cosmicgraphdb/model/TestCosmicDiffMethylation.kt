package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicDiffMethylation {
    fun parseCosmicCompleteDifferentialMethylation(filename: String):Int {
        val LIMIT = Long.MAX_VALUE
        deleteDiffMethylationNodes()
        ApocFileReader.processDelimitedFile(filename).stream()
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicDiffMethylation.parseValueMap(it) }
            .forEach {
                Neo4jConnectionService.executeCypherCommand(it.generateDiffMethylationCypher())
                println("Loading CosmicCompleteDiffMethylation for (non-unique) sample id: ${it.sampleId}")
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (exp: CompleteGeneExpression) RETURN COUNT(exp)").toInt()
    }
    private fun deleteDiffMethylationNodes() =
        Neo4jConnectionService.executeCypherCommand("MATCH (diff: CosmicDiffMethylation) DETACH DELETE (diff)")
    }

fun main (args:Array<String>) {
    val cosmicMethylationFile =  ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicCompleteDifferentialMethylation.tsv")
    val recordCount =
       TestCosmicDiffMethylation().parseCosmicCompleteDifferentialMethylation(cosmicMethylationFile)
    println("Processed $cosmicMethylationFile  record count = $recordCount")
}