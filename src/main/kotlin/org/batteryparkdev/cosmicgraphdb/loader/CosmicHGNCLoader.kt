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
import org.batteryparkdev.cosmicgraphdb.model.CosmicHGNC
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import java.util.concurrent.TimeUnit

object CosmicHGNCLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    /*
    Function to produce a stream of CosmicHGNC model objects via
    a coroutine channel
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicHGNCFile(cosmicHGNCFile: String) =
        produce<CosmicHGNC> {
            ApocFileReader.processDelimitedFile(cosmicHGNCFile)
                .map { record -> record.get("map") }
                .map { CosmicHGNC.parseValueMap(it)}
                .forEach {
                    send (it)
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicHGNCs (hgncs: ReceiveChannel<CosmicHGNC>) =
        produce<String> {
            for (hgnc in hgncs){
                Neo4jConnectionService.executeCypherCommand(hgnc.generateCosmicHGNCCypher())
                send (hgnc.hgncGeneSymbol)
                delay(20)
            }
        }

   fun loadCosmicHGNCData(filename: String) = runBlocking {
       logger.atInfo().log("Loading CosmicHGNC data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val symbols = loadCosmicHGNCs( parseCosmicHGNCFile(filename))
        for (symbol in symbols) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicHGNC data loaded $nodeCount nodes in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds"
        )
    }
}