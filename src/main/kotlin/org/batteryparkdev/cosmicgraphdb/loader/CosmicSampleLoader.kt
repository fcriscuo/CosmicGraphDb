package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.cosmicgraphdb.model.CosmicSample
import org.batteryparkdev.cosmicgraphdb.neo4j.logger
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import java.util.concurrent.TimeUnit

object CosmicSampleLoader {
@OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicSampleFile(cosmicSampleFile: String) =
        produce<CosmicSample> {
            ApocFileReader.processDelimitedFile(cosmicSampleFile)
                .map { record -> record.get("map") }
                .map { CosmicSample.parseValueMap(it) }
                .forEach {
                    send(it)
                    delay(20L)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicSamples(samples: ReceiveChannel<CosmicSample>) =
        produce<String> {
            for (sample in samples) {
              Neo4jConnectionService.executeCypherCommand(sample.generateCosmicSampleCypher())
                send(sample.sampleId.toString())
                delay(20)
            }
        }

    fun processCosmicSampleData(filename: String) = runBlocking {
        logger.atInfo().log("Loading CosmicSample data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids = loadCosmicSamples(
                            parseCosmicSampleFile(filename)
                        )

        for (id in ids) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicSample data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds"
        )
    }
}

