package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.util.concurrent.TimeUnit

class TestCosmicFusionLoader {
    fun loadCosmicFusionFile(filename: String):Int {
        //deleteCosmicFusionNodes()
        CosmicFusionLoader.loadCosmicFusionData(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (cf: CosmicFusion) RETURN COUNT(cf)").toInt()
    }

    private fun deleteCosmicFusionNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicFusion")
    }
}
fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicFusionExport.tsv")
    val stopwatch = Stopwatch.createStarted()
    println("Loading Cosmic Fusion data from: $filename")
    val rowCount = TestCosmicFusionLoader().loadCosmicFusionFile(filename)
    println("Loaded $rowCount CosmicFusion nodes in ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
}