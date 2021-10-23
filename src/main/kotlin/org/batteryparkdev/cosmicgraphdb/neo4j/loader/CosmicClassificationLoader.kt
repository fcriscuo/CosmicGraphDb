package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicClassification
import org.batteryparkdev.cosmicgraphdb.io.CsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicTypeDao
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createCosmicTypeRelationships
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.loadCosmicClassification
import org.batteryparkdev.cosmicgraphdb.neo4j.loader.CosmicClassificationLoader.loadCosmicClassificationData
import java.nio.file.Paths

object CosmicClassificationLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    /*
    Function to produce a stream od CosmicClassification model objects via
    a coroutine channel
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicClassificationFile(cosmicClassificationFile: String) =
        produce<CosmicClassification> {
            val path = Paths.get(cosmicClassificationFile)
            CsvRecordSequenceSupplier(path).get()
                .forEach {
                    send(CosmicClassification.parseCsvRecord(it))
                    delay(20)
                }
        }

    /*
    Function to load CosmicClassification model data into the Neo4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicClassificationData(classifications: ReceiveChannel<CosmicClassification>) =
        produce<CosmicClassification> {
            for (classification in classifications) {
                loadCosmicClassification(classification)
                send(classification)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processSiteType(classifications: ReceiveChannel<CosmicClassification>) =
        produce<CosmicClassification> {
            for (classification in classifications) {
                CosmicTypeDao.processCosmicTypeNode(classification.siteType)
                send(classification)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processHistologyType(classifications: ReceiveChannel<CosmicClassification>) =
        produce<CosmicClassification> {
            for (classification in classifications) {
                CosmicTypeDao.processCosmicTypeNode(classification.histologyType)
                send(classification)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processCosmicSiteType(classifications: ReceiveChannel<CosmicClassification>) =
        produce<CosmicClassification> {
            for (classification in classifications) {
                CosmicTypeDao.processCosmicTypeNode(classification.cosmicSiteType)
                send(classification)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processCosmicHistologyType(classifications: ReceiveChannel<CosmicClassification>) =
        produce<CosmicClassification> {
            for (classification in classifications) {
                CosmicTypeDao.processCosmicTypeNode(classification.cosmicHistologyType)
                send(classification)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processTypeRelationships(classifications: ReceiveChannel<CosmicClassification>) =
        produce<String> {
            for (classification in classifications) {
                createCosmicTypeRelationships(classification)
                send(classification.cosmicPhenotypeId)
                delay(20)
            }
        }

    fun loadCosmicClassificationData(filename: String) = runBlocking {
        logger.atInfo().log("Loading CosmicClassification data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids = processTypeRelationships(
            processCosmicHistologyType(
                processCosmicSiteType(
                    processHistologyType(
                        processSiteType(
                            loadCosmicClassificationData(
                                parseCosmicClassificationFile(filename)
                            )
                        )
                    )
                )
            )
        )
        for (id in ids) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicClassification data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )
    }
}

fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/classification.csv"
    loadCosmicClassificationData(filename)
}