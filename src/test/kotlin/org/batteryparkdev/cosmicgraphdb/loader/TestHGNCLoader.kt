package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestHGNCLoader {

    fun loadCosmicHGNCFile(filename: String):Int {
        deleteCosmicHGNCNodes()
        CosmicHGNCLoader.loadCosmicHGNCData(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (ch: CosmicHGNC) RETURN COUNT(ch)").toInt()
    }

    private fun deleteCosmicHGNCNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicHGNC")
    }
}
fun main(args: Array<String>): Unit {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicHGNC.tsv")
    println("Loading Cosmic HGNC data from: $filename")
    val rowCount = TestHGNCLoader().loadCosmicHGNCFile(filename)
    println("Loaded CosmicHGNC data, row count = $rowCount")
}