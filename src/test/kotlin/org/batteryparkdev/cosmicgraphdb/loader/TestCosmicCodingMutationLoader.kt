package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.util.concurrent.TimeUnit

class TestCosmicCodingMutationLoader {

    fun loadCosmicCodingMutationFile(filename: String):Int {
        deleteCosmicCodingMutationNodes()
        CosmicCodingMutationLoader.loadCosmicCodingMutationData(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (cm: CosmicCodingMutation) RETURN COUNT(cm)").toInt()
    }

    private fun deleteCosmicCodingMutationNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicCodingMutation")
    }
}

fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicMutantExportCensus.tsv")
    println("Loading Cosmic Coding Mutation data from: $filename")
    val stopwatch = com.google.common.base.Stopwatch.createStarted()
    val recordCount = TestCosmicCodingMutationLoader().loadCosmicCodingMutationFile(filename)
    println ("Loaded CosmicCodingMutation file, record count = $recordCount. Elapsed time = " +
            "${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
}