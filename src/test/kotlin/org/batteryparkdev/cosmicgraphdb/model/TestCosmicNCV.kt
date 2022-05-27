package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicNCV {
    private val LIMIT = Long.MAX_VALUE

    fun parseCosmicNCVFile(filename: String): Int {
        // limit the number of records processed
        var recordCount = 0
       deleteNCVNodes()
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicNCV.parseValueMap(it) }
            .forEach { ncv ->
                Neo4jConnectionService.executeCypherCommand(ncv.generateCosmicNCVCypher())
                println("Loaded CosmicNCV key: ${ncv.getKey()}")
                // create a Publication node if a PubMed id is present
                //hall.createPubMedRelationship(hall.pubmedId)
                recordCount += 1
            }
        return recordCount
    }
    private fun deleteNCVNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicNCV")
    }
}
fun main() {
    val cosmicNCVFile =  ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicNCV.tsv")
    val recordCount =
        TestCosmicNCV().parseCosmicNCVFile(cosmicNCVFile)
    println("Loaded COSMIC NCV file: $cosmicNCVFile  record count = $recordCount")
}