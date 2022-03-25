package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicCompleteGeneExpression
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createGeneExpressionToGeneRelationship
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createGeneExpressionToSampleRelationship
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.loadCosmicCompleteGeneExpression
import java.nio.file.Paths

object CosmicGeneExpressionLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()
    /*
    Private function to create a channel of CosmicCompleteGeneExpression model objects
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicGeneExpression(cosmicGeneExpressionFile: String) =
        produce<CosmicCompleteGeneExpression> {
            val path = Paths.get(cosmicGeneExpressionFile)
            TsvRecordSequenceSupplier(path).get()
                .forEach {
                    send(CosmicCompleteGeneExpression.parseCsvRecord(it))
                    delay(20)
                }
        }

    /*
    Private function to load data from CosmicCompleteGeneExpression model objects
    into a Neo4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
     private fun CoroutineScope.loadGeneExpression( expressions: ReceiveChannel<CosmicCompleteGeneExpression>) = produce {
         for (expression in expressions) {
             loadCosmicCompleteGeneExpression(expression)
             send(expression)
             delay(50)
         }
    }

    /*
    Private function to create CosmicGeneExpression to CosmicGene relationships
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.createGeneRelationships(expressions: ReceiveChannel<CosmicCompleteGeneExpression>) = produce {
        for (expression in expressions) {
            createGeneExpressionToGeneRelationship(expression)
            send(expression)
            delay(50)
        }
    }

    /*
   Private function to create CosmicGeneExpression to CosmicSample relationships
    */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.createSampleRelationships(expressions: ReceiveChannel<CosmicCompleteGeneExpression>) = produce{
        for (expression in expressions){
            createGeneExpressionToSampleRelationship(expression)
            send(expression.key)
            delay(50)
        }
    }

    /*
    Public function to initiate loading CosmicCompleteGeneExpression data
     */
    fun loadCosmicCompleteGeneExpressionData(filename:String) = runBlocking{
        logger.atInfo().log("Loading CosmicCompleteGeneExpression data from file:  $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids = createSampleRelationships(
            createGeneRelationships(
            loadGeneExpression(parseCosmicGeneExpression(filename))))
        for (id in ids) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicCompleteGeneExpression data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )
    }
}

fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "data/sample_CosmicCompleteGeneExpression.tsv"
    CosmicGeneExpressionLoader.loadCosmicCompleteGeneExpressionData(filename)
}