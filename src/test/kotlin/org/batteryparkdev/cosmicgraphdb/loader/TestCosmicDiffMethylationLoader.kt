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
import org.batteryparkdev.cosmicgraphdb.model.CosmicDiffMethylation
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.util.concurrent.TimeUnit

class TestCosmicDiffMethylationLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    fun loadCosmicDiffMethylationFile(filename: String): Int {
        deleteCosmicDiffMethylationNodes()
        println("Loading CosmicDiffMethylation data from: $filename")
        CosmicDiffMethylationLoader.loadCosmicDiffMethylationData(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (cdm:CosmicDiffMethylation) " +
                " RETURN COUNT(cdm)").toInt()
    }

    private fun deleteCosmicDiffMethylationNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicDiffMethylation")
    }
}

fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicCompleteDifferentialMethylation.tsv")
    val stopwatch = Stopwatch.createStarted()
    val recordCount = TestCosmicDiffMethylationLoader().loadCosmicDiffMethylationFile(filename)
    println("Loaded $recordCount CosmicDifferentialMethylation records in ${stopwatch.elapsed(TimeUnit.SECONDS)} " +
            "seconds")
}
