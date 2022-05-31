package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.util.concurrent.TimeUnit

class TestCosmicStructLoader {

    fun loadCosmicStructFile(filename: String): Int {
        deleteCosmicStructNodes()
        CosmicStructLoader.loadCosmicStructFile(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (cs: CosmicStruct) RETURN COUNT(cs)").toInt()
    }

    private fun deleteCosmicStructNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicStruct")
    }
}
fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicStructExport.tsv")
    println("Loading CosmicStruct  data from: $filename")
    val stopwatch = Stopwatch.createStarted()
    val rowCount = TestCosmicStructLoader().loadCosmicStructFile(filename)
    println("Loaded $rowCount CosmicStruct nodes in ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
}