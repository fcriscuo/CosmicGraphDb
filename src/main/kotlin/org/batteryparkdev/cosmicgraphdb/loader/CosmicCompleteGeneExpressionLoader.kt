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
import org.batteryparkdev.cosmicgraphdb.model.CosmicCompleteGeneExpression
import org.batteryparkdev.neo4j.service.Neo4jConnectionService

object CosmicCompleteGeneExpressionLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    /*
    Private function to create a channel of CosmicCompleteGeneExpression model objects
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicGeneExpression(cosmicGeneExpressionFile: String) =
        produce<CosmicCompleteGeneExpression> {
            ApocFileReader.processDelimitedFile(cosmicGeneExpressionFile)
                .map { record -> record.get("map") }
                .map { CosmicCompleteGeneExpression.parseValueMap(it) }
                .forEach {
                    send(it)
                    delay(20L)
                }
        }

    /*
    Private function to generate the Cypher command that will load a CosmicCompleteGeneExpression
    node into the Neo4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.generateCypherCommand(expressions: ReceiveChannel<CosmicCompleteGeneExpression>) =
        produce<String> {
            for (expression in expressions){
                send(expression.generateLoadCosmicModelCypher())
                delay(20)
            }
        }

    /*
    Private function to load data from CosmicCompleteGeneExpression model objects
    into a Neo4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
     private fun CoroutineScope.loadCompleteGeneExpression(commands: ReceiveChannel<String>) =
         produce<String> {
         for (command in commands) {
             Neo4jConnectionService.executeCypherCommand(command)
             send(command)
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
            loadCompleteGeneExpression(
                generateCypherCommand(
                parseCosmicGeneExpression(filename)))
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

