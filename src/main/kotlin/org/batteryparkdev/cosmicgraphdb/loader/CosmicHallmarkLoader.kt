package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.model.CosmicHallmark
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.dao.addCosmicHallmarkLabel
import org.batteryparkdev.cosmicgraphdb.dao.createCosmicGeneRelationship
import org.batteryparkdev.cosmicgraphdb.dao.createPubMedRelationship
import org.batteryparkdev.cosmicgraphdb.dao.loadCosmicHallmark
import java.nio.file.Paths

/*
Responsible for loading data from the CosmicHallmark TSV file
into the Neo4j database
 */
object CosmicHallmarkLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicHallmarkFile(cosmicHallmarkFile: String) =
        produce<CosmicHallmark> {
            val path = Paths.get(cosmicHallmarkFile)
            TsvRecordSequenceSupplier(path).get()
                .forEach {
                    send(CosmicHallmark.parseCsvRecord(it))
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicHallmarks(hallmarks: ReceiveChannel<CosmicHallmark>) =
        produce<CosmicHallmark> {
            for (hallmark in hallmarks) {
                loadCosmicHallmark(hallmark)
                send(hallmark)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.addHallmarkLabels(hallmarks: ReceiveChannel<CosmicHallmark>) =
        produce<CosmicHallmark> {
            for (hallmark in hallmarks) {
                addCosmicHallmarkLabel(hallmark.hallmarkId, hallmark.hallmark)
                send(hallmark)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.addGeneRelationship(hallmarks: ReceiveChannel<CosmicHallmark>) =
        produce<CosmicHallmark> {
            for (hallmark in hallmarks) {
                if (hallmark.geneSymbol.isNotEmpty()) {
                    createCosmicGeneRelationship(hallmark)
                }
                send(hallmark)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.addPubMedRelationship(hallmarks: ReceiveChannel<CosmicHallmark>) =
        produce<String> {
            for (hallmark in hallmarks) {
                if (hallmark.pubmedId.isNotEmpty()) {
                    createPubMedRelationship(hallmark)
                }
                send(hallmark.geneSymbol)
                delay(20)
            }
        }

    fun processCosmicHallmarkData(filename: String) = runBlocking {
        logger.atInfo().log("Loading CosmicGeneCensus data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val geneSymbols = addPubMedRelationship(
            addGeneRelationship(
                addHallmarkLabels(
                    loadCosmicHallmarks(
                        parseCosmicHallmarkFile(filename)
                    )
                )
            )
        )
        for (symbol in geneSymbols) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicHallmark data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )

    }
}

// main function for integration testing
fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv"
    CosmicHallmarkLoader.processCosmicHallmarkData(filename)
}