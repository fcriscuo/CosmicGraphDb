package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicHallmarkLoader {

    fun loadCosmicHallmarkFile( filename: String): Int {
        deleteCosmicHallmarkNodes()
        CosmicHallmarkLoader.processCosmicHallmarkData(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (ch: CosmicHallmark) RETURN COUNT(ch)").toInt()
    }

    private fun deleteCosmicHallmarkNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicHallmark")
    }
}

fun main () {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv")
    println("Loading Cosmic Gene Census Hallmark data from: $filename")
    val rowCount = TestCosmicHallmarkLoader().loadCosmicHallmarkFile(filename)
    println("Loaded CosmicCGeneCensusHallmark data row count = $rowCount")
}
