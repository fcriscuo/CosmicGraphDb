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
import org.batteryparkdev.cosmicgraphdb.model.CosmicNCV
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import java.util.concurrent.TimeUnit

object CosmicNCVLoader {
    /*
    Responsible for parsing the CosmicNCV.tsv file and loading its contents into
    the Neo4j database as CosmicNCV nodes
     */
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicNCVFile(cosmicNCVFile: String) =
        produce<CosmicNCV> {
            ApocFileReader.processDelimitedFile(cosmicNCVFile)
                .map { record -> record.get("map") }
                .map { CosmicNCV.parseValueMap(it) }
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicNCVData(ncvs: ReceiveChannel<CosmicNCV>) =
        produce<Int> {
            for (ncv in ncvs) {
                Neo4jConnectionService.executeCypherCommand(ncv.generateCosmicNCVCypher())
                ncv.createPubMedRelationship(ncv.pubmedId)
                send(ncv.sampleId)
                delay(20L)
            }
        }

    fun loadCosmicNCVFile(filename: String) = runBlocking {
        logger.atInfo().log("Loading CosmicNCV data from file: $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids = loadCosmicNCVData(parseCosmicNCVFile(filename))
        for (id in ids) {
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicNCV data loaded $nodeCount nodes in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds"
        )
    }
}