package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.cosmicgraphdb.model.CosmicCompleteCNA
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicCompleteCNALoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    fun loadCompleteCNAFile(filename: String) {
        deleteCompleteCNANodes()
        CosmicCompleteCNALoader.loadCosmicCompleteCNAData(filename)
    }

    private fun deleteCompleteCNANodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicCompleteCNA")
    }
}
fun main() {
    val cosmicCNAFile =
        ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicCompleteCNA.tsv")
    TestCosmicCompleteCNALoader().loadCompleteCNAFile(cosmicCNAFile)
    println("Test finished")
}


