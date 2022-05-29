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
import org.batteryparkdev.cosmicgraphdb.model.CosmicGeneCensus
import org.batteryparkdev.neo4j.service.Neo4jConnectionService

/*
Responsible for creating/merging  CosmicGeneCensus nodes and associated annotation nodes
 */
object CosmicGeneCensusLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicGeneCensusFile(cosmicGeneCensusFile: String) =
        produce<CosmicGeneCensus> {
            ApocFileReader.processDelimitedFile(cosmicGeneCensusFile)
                .map { record -> record.get("map") }
                .map { CosmicGeneCensus.parseValueMap(it) }
                .forEach {
                    send(it)
                    delay(20L)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicGeneCensusData(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<String> {
            for (gene in genes) {
                loadCosmicGeneNode(gene)
                send(gene.geneSymbol)
                delay(20)
            }
        }

    private fun loadCosmicGeneNode(gene: CosmicGeneCensus): String =
        Neo4jConnectionService.executeCypherCommand(gene.generateCosmicGeneCypher())

    /*
    Public function load CosmicGeneCensus nodes and associated annotations
     */
    fun loadCosmicGeneCensusData(filename: String) = runBlocking {
        logger.atInfo().log("Loading CosmicGeneCensus data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val geneSymbols =
            loadCosmicGeneCensusData(
                parseCosmicGeneCensusFile(filename)
            )

        for (symbol in geneSymbols) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicGeneCensus data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )

    }
}
