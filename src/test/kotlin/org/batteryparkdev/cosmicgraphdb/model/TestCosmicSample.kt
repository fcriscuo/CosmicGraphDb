package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicSample {
    fun parseCosmicSampleFile(filename: String): Int {
        val LIMIT = Long.MAX_VALUE
        // limit the number of records processed
        deleteCosmicSampleNodes()
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicSample.parseValueMap(it) }
            .forEach {sample->
                println("Loading sample ${sample.sampleId}")
//                val cypher = sample.generateCosmicSampleCypher()
//                println(cypher)
//                Neo4jConnectionService.executeCypherCommand(cypher)
                Neo4jConnectionService.executeCypherCommand(sample.generateCosmicSampleCypher())

            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (cs: CosmicSample) RETURN COUNT(cs)").toInt()
    }
    private fun deleteCosmicSampleNodes(){
        Neo4jUtils.detachAndDeleteNodesByName("CosmicTumor")
        Neo4jUtils.detachAndDeleteNodesByName("CosmicSample")
    }
}
fun main() {
    val filename  = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicSample.tsv")
    val recordCount =
        TestCosmicSample().parseCosmicSampleFile(filename)
    println("Sample record count = $recordCount")
}