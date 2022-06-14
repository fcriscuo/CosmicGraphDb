package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicClassificationLoader {

    fun loadClassificationFile(filename: String): Int {
       // deleteClassificationNode()
        CosmicClassificationLoader.loadCosmicClassificationData(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (cc: CosmicClassification) RETURN COUNT(cc)").toInt()
    }

    private fun deleteClassificationNode() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicClassification")
    }
}

fun main() {
    val cosmicClassificationFile =
        ConfigurationPropertiesService.resolveCosmicSampleFileLocation("classification.csv")
    val recordCount = TestCosmicClassificationLoader().loadClassificationFile(cosmicClassificationFile)
    println("CosmicClassification data loaded, record count = $recordCount")
}