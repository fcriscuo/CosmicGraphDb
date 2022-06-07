package org.batteryparkdev.cosmicgraphdb.model


import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicBreakpoint {
    private val LIMIT = Long.MAX_VALUE
    /*
    Test parsing sample Cosmic breakpoints TSV file and mapping data to
    CosmicBreakpoint model class
    n.b. file name specification must be full path since it is resolved by Neo4j server
     */
    fun parseBreakpointFile(filename: String): Int {
        // limit the number of records processed
       Neo4jUtils.detachAndDeleteNodesByName("CosmicBreakpoint")
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicBreakpoint.parseValueMap(it) }
            .forEach { breakpoint ->
                println("Loading breakpoint  ${breakpoint.mutationId}")
                Neo4jConnectionService.executeCypherCommand(breakpoint.generateLoadCosmicModelCypher())
                // create a Publication node if a PubMed Id is present
                breakpoint.createPubMedRelationship(breakpoint.pubmedId)
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (cb: CosmicBreakpoint) RETURN COUNT(cb)").toInt()
    }
}

fun main() {
    val cosmicBreakpointFile =  ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicBreakpointsExport.tsv")
val recordCount =
    TestCosmicBreakpoint().parseBreakpointFile(cosmicBreakpointFile)
    println("Breakpoint record count = $recordCount")
}