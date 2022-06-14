package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.util.concurrent.TimeUnit

class TestCosmicSampleLoader {

    fun loadCosmicSampleFile(filename: String):Int {
      //  deleteSampleRelatedNodes()
        CosmicSampleLoader.processCosmicSampleData(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (cs: CosmicSample) RETURN COUNT(cs)").toInt()
    }

    private fun deleteSampleRelatedNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicPatient")
        Neo4jUtils.detachAndDeleteNodesByName("SampleMutationCollection")
        Neo4jUtils.detachAndDeleteNodesByName("CosmicTumor")
        Neo4jUtils.detachAndDeleteNodesByName("CosmicSample")
    }
}

fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicSample.tsv")
    println("Loading Cosmic Sample data from: $filename")
    val stopwatch = com.google.common.base.Stopwatch.createStarted()
    val recordCount = TestCosmicSampleLoader().loadCosmicSampleFile(filename)
    println ("Loaded CosmicSample file, record count = $recordCount. Elapsed time = " +
            "${stopwatch.elapsed(TimeUnit.SECONDS)}")
}