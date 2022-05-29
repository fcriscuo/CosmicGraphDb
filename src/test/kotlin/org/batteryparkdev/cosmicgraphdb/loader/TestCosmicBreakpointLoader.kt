package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicBreakpointLoader {
    fun processBreakpointFile(filename: String):Int {
        deleteBreakpointNodes()
        CosmicBreakpointLoader.loadCosmicBreakpointData(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (cb: CosmicBreakpoint) RETURN COUNT(cb)").toInt()
    }
private fun deleteBreakpointNodes()  =
    Neo4jUtils.detachAndDeleteNodesByName("CosmicBreakpoint")
}

fun main() {
    val cosmicBreakpointFile =
        ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicBreakpointsExport.tsv")
    val recordCount = TestCosmicBreakpointLoader().processBreakpointFile(cosmicBreakpointFile)
    println("Loaded $recordCount CosmicBreakpoint records")
}

