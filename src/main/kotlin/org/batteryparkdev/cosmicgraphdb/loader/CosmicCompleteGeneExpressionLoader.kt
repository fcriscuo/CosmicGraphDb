package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.model.CosmicCompleteGeneExpression
import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import java.nio.file.Paths

object CosmicCompleteGeneExpressionLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    /*
    Private function to create a channel of CosmicCompleteGeneExpression model objects
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicGeneExpression(cosmicGeneExpressionFile: String) =
        produce<CosmicCompleteGeneExpression> {
            val path = Paths.get(cosmicGeneExpressionFile)
            ApocFileReader.processDelimitedFile(cosmicGeneExpressionFile)
                .map { record -> record.get("map") }
                .map { CosmicCompleteGeneExpression.parseValueMap(it) }
                .forEach {
                    send(it)
                    delay(20L)
                }
        }

    /*
    Private function to load data from CosmicCompleteGeneExpression model objects
    into a Neo4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
     private fun CoroutineScope.loadCompleteGeneExpression(expressions: ReceiveChannel<CosmicCompleteGeneExpression>) = produce {
         for (expression in expressions) {
             Neo4jConnectionService.executeCypherCommand(expression.generateCompleteGeneExpressionCypher())
             send(expression)
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
        val ids =
            loadCompleteGeneExpression(parseCosmicGeneExpression(filename))
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

