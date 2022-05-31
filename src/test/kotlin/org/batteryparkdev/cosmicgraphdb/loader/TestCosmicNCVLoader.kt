package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.util.concurrent.TimeUnit

class TestCosmicNCVLoader {
    fun loadCosmicNCVFile(filename: String): Int {
        deleteCosmicNCVNodes()
        CosmicNCVLoader.loadCosmicNCVFile(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (ncv: CosmicNCV) RETURN COUNT(ncv)").toInt()
    }

    private fun deleteCosmicNCVNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicNCV")
    }
}
fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicNCV.tsv")
    println("Loading Cosmic NCV data from: $filename")
    val stopwatch = Stopwatch.createStarted()
    val rowCount = TestCosmicNCVLoader().loadCosmicNCVFile(filename)
    println("Loaded $rowCount CosmicNCV nodes in ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
}