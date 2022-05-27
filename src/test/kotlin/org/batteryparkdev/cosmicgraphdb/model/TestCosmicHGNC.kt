package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicHGNC {
    fun parseCosmicHGNCFile(filename: String): Int {
        // limit the number of records processed
        val LIMIT = Long.MAX_VALUE
        deleteCosmicHGNCNodes()
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicHGNC.parseValueMap(it) }
            .forEach { hgnc->
                println("Loading HGNC gene symbol ${hgnc.hgncGeneSymbol}")
                Neo4jConnectionService.executeCypherCommand(hgnc.generateCosmicHGNCCypher())
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (ch:CosmicHGNC) RETURN COUNT(ch)").toInt()
    }
    private fun deleteCosmicHGNCNodes(){
        Neo4jUtils.detachAndDeleteNodesByName("CosmicHGNC")
    }
}
fun main() {
    val filename  = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicHGNC.tsv")
    val recordCount =
        TestCosmicHGNC().parseCosmicHGNCFile(filename)
    println("HGNC record count = $recordCount")
}