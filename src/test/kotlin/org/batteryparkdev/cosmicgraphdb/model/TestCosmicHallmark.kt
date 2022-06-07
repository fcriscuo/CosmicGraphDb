package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicHallmark {

    private val LIMIT = Long.MAX_VALUE

    fun parseCosmicGeneCensusFile(filename: String): Int {
        // limit the number of records processed
        var recordCount = 0
        deleteCosmicHallmarkNodes()
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicHallmark.parseValueMap(it) }
            .forEach { hall ->
                Neo4jConnectionService.executeCypherCommand(hall.generateLoadCosmicModelCypher())
                println("Loaded Cosmic Hallmark ${hall.geneSymbol}")
                // create a Publication node if a PubMed id is present
                CosmicGeneCensus.registerGenePublication(hall.pubmedId, hall.geneSymbol)
                recordCount += 1
            }
        return recordCount
    }
    private fun deleteCosmicHallmarkNodes(){
        Neo4jUtils.detachAndDeleteNodesByName("CosmicHallmark")
    }
}
fun main() {
    val cosmicHallmarkFile =  ConfigurationPropertiesService.resolveCosmicSampleFileLocation("Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv")
    val recordCount =
        TestCosmicHallmark().parseCosmicGeneCensusFile(cosmicHallmarkFile)
    println("Loaded COSMIC gene census hallmark file: $cosmicHallmarkFile  record count = $recordCount")
}
