package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicMutation {
    fun parseCosmicMutationFile(filename: String): Int {
        val LIMIT = Long.MAX_VALUE
        // limit the number of records processed
        deleteCosmicMutationNodes()
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicMutation.parseValueMap(it)}
            .forEach { mutation ->
                println("Loading mutation ${mutation.mutationId} for gene: ${mutation.geneSymbol}")
                Neo4jConnectionService.executeCypherCommand(mutation.generateCosmicMutationCypher())
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (cm:CosmicMutation) RETURN COUNT(cm)").toInt()
    }

    private fun deleteCosmicMutationNodes() = Neo4jConnectionService.executeCypherCommand(
        "MATCH (cm:CosmicMutation) DETACH DELETE (cm)"
    )
}
fun main() {
    val filename  = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicMutantExport.tsv")
    val recordCount =
        TestCosmicMutation().parseCosmicMutationFile(filename)
    println("Mutation record count = $recordCount")
}
