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
import org.batteryparkdev.cosmicgraphdb.model.CosmicFusion
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import java.util.concurrent.TimeUnit

object
CosmicFusionLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    /*
    Function to generate a stream of CosmicFusion model objects by
    parsing a specified TSV file
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicFusionFile(cosmicFusionFile: String) =
        produce<CosmicFusion> {
            ApocFileReader.processDelimitedFile(cosmicFusionFile)
                .map { record -> record.get("map") }
                .map {CosmicFusion.parseValueMap(it)}
                .filter{ fusion -> fusion.isValid()}  // skip incomplete records
                .forEach {
                    send(it)
                    delay(20L)
                }
        }
    /*
    Function to load CosmicFusion model object into the Neo4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicFusion(fusions: ReceiveChannel<CosmicFusion>) =
        produce<Int> {
            for (fusion in fusions) {
                Neo4jConnectionService.executeCypherCommand(fusion.generateLoadCosmicModelCypher())
                fusion.createPubMedRelationship(fusion.pubmedId)
                send (fusion.fusionId)
                delay(20L)
            }
        }

    fun loadCosmicFusionData(filename: String) = runBlocking {
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        logger.atInfo().log("Loading CosmicFusion data from $filename")
        val ids = loadCosmicFusion(parseCosmicFusionFile(filename))
        for (id in ids){
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicFusion data loaded $nodeCount nodes in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds"
        )
    }
}