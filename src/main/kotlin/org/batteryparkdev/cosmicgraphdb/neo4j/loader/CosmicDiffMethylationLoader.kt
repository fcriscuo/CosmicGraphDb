package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicDiffMethylation
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createHistologyTypeRelationship
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createSampleRelationship
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.loadCosmicDiffMethylation
import java.nio.file.Paths
import java.util.concurrent.TimeUnit


object CosmicDiffMethylationLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

/*
Coroutine function to produce a channel of CosmicDiffMethylation model objects
Input is the full file name
Output is channel of model objects
*/
    @OptIn(ExperimentalCoroutinesApi::class)
   private  fun CoroutineScope.parseCosmicDiffMethylationChannel(cosmicDiffMethylFile: String) =
        produce<CosmicDiffMethylation> {
            val path = Paths.get(cosmicDiffMethylFile)
            TsvRecordSequenceSupplier(path).get()
                .forEach {
                    send(CosmicDiffMethylation.parseCsvRecord(it))
                    delay(20)
                }

        }

    /*
   Coroutine function to load CosmicDiffMethylation data into the
   connected Neo4j database
    */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicDiffMethylationChannel(methyls: ReceiveChannel<CosmicDiffMethylation>) = produce {
        for (methyl in methyls) {
            loadCosmicDiffMethylation(methyl)
            send(methyl)
            delay(50)
        }
    }

    /*
   Coroutine function to create a CosmicSample to CosmicDiffMethylation relationship
    */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.createRelationshipToSampleChannel(methyls: ReceiveChannel<CosmicDiffMethylation>) = produce {
        for (methyl in methyls) {
            createSampleRelationship(methyl.sampleId, methyl.fragmentId)
            delay(30)
            send(methyl)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadHistologyTypeChannel(methyls: ReceiveChannel<CosmicDiffMethylation>) = produce {
        for (methyl in methyls) {
            val type = CosmicTypeLoader.processCosmicTypeNode(methyl.histology)
            // val type = processCosmicTypeNode(methyl.histology)
            val fragTypePair = Pair(methyl.fragmentId, type)
            send(fragTypePair)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
  private   fun CoroutineScope.createHistologyTypeRelationshipChannel(fragTypePairs: ReceiveChannel<Pair<String, Int>>) =
        produce {
            for (fragTypePair in fragTypePairs) {
                createHistologyTypeRelationship(fragTypePair.first, fragTypePair.second)
                send(fragTypePair.first)
            }
        }

    /*
    Public function to load Cosmic Differential Methylation from a specified file
     */
    fun loadCosmicDiffMethylationData(filename: String)  = runBlocking {
        logger.atInfo().log("Loading CosmicDiffMethylation data from file: $filename")
       var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids =
            createHistologyTypeRelationshipChannel(
                loadHistologyTypeChannel(
                    createRelationshipToSampleChannel(
                        loadCosmicDiffMethylationChannel(
                            parseCosmicDiffMethylationChannel(filename)
                        )
                    )
                )
            )
        for (id in ids) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicDifferentialMethylation data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds"
        )
    }
}

fun main(args: Array<String>) {
    val filename = if (args.size > 0) args[0] else "data/sample_CosmicCompleteDifferentialMethylation.tsv"
    CosmicDiffMethylationLoader.loadCosmicDiffMethylationData(filename)
}