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
import org.batteryparkdev.cosmicgraphdb.model.CosmicStruct
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import java.util.concurrent.TimeUnit

object CosmicStructLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicStructFile(cosmicStructFile: String) =
        produce<CosmicStruct> {
            ApocFileReader.processDelimitedFile(cosmicStructFile)
                .map { record -> record.get("map") }
                .map { CosmicStruct.parseValueMap(it) }
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicStructData(structs: ReceiveChannel<CosmicStruct>) =
        produce<Int> {
            for (struct in structs) {
                Neo4jConnectionService.executeCypherCommand(struct.generateStructCypher())
                struct.createPubMedRelationship(struct.pubmedId)
                send(struct.sampleId)
                delay(20L)
            }
        }

    fun loadCosmicStructFile(filename: String) = runBlocking {
       logger.atInfo().log("Loading CosmicStruct data from file: $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids = loadCosmicStructData(parseCosmicStructFile(filename))
        for (id in ids) {
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicResistanceMutation data loaded $nodeCount nodes in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
    }
}