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
import org.batteryparkdev.cosmicgraphdb.model.CosmicResistanceMutation
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import java.util.concurrent.TimeUnit

object CosmicResistanceMutationLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicResistanceMutationFile(cosmicDrugResistanceFile: String) =
        produce<CosmicResistanceMutation> {
            ApocFileReader.processDelimitedFile(cosmicDrugResistanceFile)
                .map { record -> record.get("map") }
                .map { CosmicResistanceMutation.parseValueMap(it) }
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicNCVData(muts: ReceiveChannel<CosmicResistanceMutation>) =
        produce<Int> {
            for (mut in muts) {
                Neo4jConnectionService.executeCypherCommand(mut.generateCosmicResistanceCypher())
                send(mut.sampleId)
                delay(20L)
            }
        }
    fun loadCosmicResistanceMutationFile(filename: String) = runBlocking {
        logger.atInfo().log("Loading CosmicResistanceMutation data from file: $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids = loadCosmicNCVData(parseCosmicResistanceMutationFile(filename))
        for (id in ids) {
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicResistanceMutation data loaded $nodeCount nodes in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds"
        )

    }

}