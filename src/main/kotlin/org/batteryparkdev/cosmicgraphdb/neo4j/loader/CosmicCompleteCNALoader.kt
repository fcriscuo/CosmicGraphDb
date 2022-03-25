package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicCompleteCNA
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createRelationshipFromSample
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createRelationshipToGene
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.loadCosmicCompleteCNA
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import java.nio.file.Paths

object CosmicCompleteCNALoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    /*
    Private function to create a coroutine channel of CosmicCompleteCNA
    model objects
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicCompleteCNA(cosmicCompleteCNAFile: String) =
        produce<CosmicCompleteCNA> {
            val path = Paths.get(cosmicCompleteCNAFile)
            TsvRecordSequenceSupplier(path).get()
                .forEach {
                    send(CosmicCompleteCNA.parseCsvRecord(it))
                    delay(20)
                }
        }

    /*
    Private function to create a coroutine channel that loads
    CosmicCompleteCNA data into the Neo4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicCompleteCNA(cnas: ReceiveChannel<CosmicCompleteCNA>) =
        produce {
            for (cna in cnas) {
                loadCosmicCompleteCNA(cna)
                send(cna)
                delay(50)
            }
        }

    /*
    Private function to create CosmicCompleteCNA -> CosmicGene relationship
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.createGeneRelationships(cnas: ReceiveChannel<CosmicCompleteCNA>) =
        produce<CosmicCompleteCNA> {
            for (cna in cnas) {
                createRelationshipToGene(cna.cnvId, cna.geneSymbol)
                send(cna)
                delay(50)
            }
        }

    /*
    Private function to create CosmicSample -> CosmicCompleteCNA relationship
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.createSampleRelationships(cnas: ReceiveChannel<CosmicCompleteCNA>) =
        produce<Int> {
            for (cna in cnas) {
                createRelationshipFromSample(cna.cnvId, cna.sampleId)
                send(cna.cnvId)
                delay(50)
            }
        }

/*
Public function to complete parsing of CosmicCompleteCNA file and
loaded data into the Neo4j database
 */

    fun loadCosmicCompleteCNAData(filename: String) = runBlocking {
        logger.atInfo().log("Loading CosmicCompleteCNA data from file: $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids = createSampleRelationships(
            createGeneRelationships(
                loadCosmicCompleteCNA(
                    parseCosmicCompleteCNA(filename)
                )
            )
        )
        for (id in ids) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicCompleteCNA data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )
    }
}

fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "data/sample_CosmicCompleteCNA.tsv"
    CosmicCompleteCNALoader.loadCosmicCompleteCNAData(filename)
}
