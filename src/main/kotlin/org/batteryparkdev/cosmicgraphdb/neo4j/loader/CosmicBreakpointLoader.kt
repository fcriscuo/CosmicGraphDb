package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicBreakpoint
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicBreakpointDao.createPubMedRelationship
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicBreakpointDao.loadCosmicBreakpointNode
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicTypeDao
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

object CosmicBreakpointLoader {

    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    //Parse the CosmicBreakpoints TSV file
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicBreakpointFile(cosmicBreakpointFile: String) =
        produce<CosmicBreakpoint> {
            val path = Paths.get(cosmicBreakpointFile)
            TsvRecordSequenceSupplier(path).get()
                .forEach {
                    send(CosmicBreakpoint.parseCsvRecord(it))
                    delay(20)
                }
        }

    // merge the breakpoint data into a Neo4j node
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicBreakpoints(breakpoints:ReceiveChannel<CosmicBreakpoint>) =
        produce<CosmicBreakpoint> {
            for(breakpoint in breakpoints){
                loadCosmicBreakpointNode(breakpoint)
                send(breakpoint)
                delay(20)
            }
        }

    // create a relationship to the breakpoint site type
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processBreakpointSiteType(breakpoints: ReceiveChannel<CosmicBreakpoint>) =
        produce<CosmicBreakpoint> {
            for(breakpoint in breakpoints){
                CosmicTypeDao.processCosmicTypeNode(breakpoint.site)
                send(breakpoint)
                delay(20)
            }
        }
    // create a relationship to the histology type
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processBreakpointHistologyType(breakpoints: ReceiveChannel<CosmicBreakpoint>) =
        produce<CosmicBreakpoint>{
            for(breakpoint in breakpoints){
                CosmicTypeDao.processCosmicTypeNode(breakpoint.histology)
                send(breakpoint)
                delay(20)
            }
        }
    // create a relationship to the mutation type
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processBreakpointMutationType(breakpoints: ReceiveChannel<CosmicBreakpoint>) =
        produce<CosmicBreakpoint>{
            for(breakpoint in breakpoints){
                CosmicTypeDao.processCosmicTypeNode(breakpoint.mutationType)
                send(breakpoint)
                delay(20)
            }
        }

    // create relationship to pubmed node
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processPubMedRelationships(breakpoints: ReceiveChannel<CosmicBreakpoint>)=
        produce<Int> {
            for (breakpoint in breakpoints){
                createPubMedRelationship(breakpoint)
                send(breakpoint.mutationId)
                delay(20)
            }
        }

    // execute the breakpoint data loading pipeline
    fun processCosmicBreakpointData(filename:String) = runBlocking {
        logger.atInfo().log("Loading CosmicBreakpoint data from file: $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids = processPubMedRelationships(
            processBreakpointMutationType(
                processBreakpointHistologyType(
                    processBreakpointSiteType(
                        loadCosmicBreakpoints(
                            parseCosmicBreakpointFile(filename)
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
            "CosmicBreakpoint data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds"
        )
    }
}
// main method for integration testing
fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/sample_CosmicBreakpointsExport.tsv"
    CosmicBreakpointLoader.processCosmicBreakpointData(filename)
}
