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
import org.batteryparkdev.io.UTF16CSVRecordSupplier
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import java.io.File
import java.nio.file.Paths
import kotlin.streams.asSequence

/*
Specialized loader class to support UTF-16 encoding for COSMIC file
Responsible for loading data from the CosmicHallmark TSV file
into the Neo4j database
 */
object CosmicHallmarkLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    /*
    CosmicHallmark file uses a UTF16 encoding
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.parseCSVRecords(filename: String) =
        produce<CosmicHallmark> {
            val path = Paths.get(filename)
            UTF16CSVRecordSupplier(path).get().asSequence().forEach{
                send( CosmicHallmark.parseCSVRecord(it))
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicHallmarks(hallmarks: ReceiveChannel<CosmicHallmark>) =
        produce<CosmicHallmark> {
            for (hallmark in hallmarks) {
                Neo4jConnectionService.executeCypherCommand(hallmark.generateLoadCosmicModelCypher())
                send(hallmark)
                delay(20)
            }
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.addPubMedRelationship(hallmarks: ReceiveChannel<CosmicHallmark>) =
        produce<String> {
            for (hallmark in hallmarks) {
                if (hallmark.pubmedId>0) {
                    hallmark.createPubMedRelationship()
                }
                send(hallmark.geneSymbol)
                delay(20)
            }
        }

    fun processCosmicHallmarkFile(filename: String) = runBlocking {
        require(filename.isNotEmpty()) {"A CosmicHallmark filename must be specified"}
        check(File(filename).exists()) {"$filename does not exist"}
        logger.atInfo().log("Loading CosmicGeneCensus data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val geneSymbols = addPubMedRelationship(
            loadCosmicHallmarks(parseCSVRecords(filename)
        ))
        for (symbol in geneSymbols) {
            println("Hallmark gene symbol $symbol")
            nodeCount += 1
        }
        println(
            "CosmicHallmark data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )
    }
}