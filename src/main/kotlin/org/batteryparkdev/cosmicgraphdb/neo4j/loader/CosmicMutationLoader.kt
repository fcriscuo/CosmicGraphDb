package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicMutation
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createCosmicMutationToGeneRelationship
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.loadCosmicMutation
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/*
Responsible for loading data from a CosmicMutation model instance into the Neo4j database
Creates a  CosmicMutation -> CosmicGene relationship
 */

object CosmicMutationLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicMutationFile(cosmicMutationFile: String) =
        produce<CosmicMutation> {
            val path = Paths.get(cosmicMutationFile)
            TsvRecordSequenceSupplier(path).get()
                .forEach {
                    send(CosmicMutation.parseCsvRecord(it))
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicMutations(mutations: ReceiveChannel<CosmicMutation>) =
        produce<CosmicMutation> {
            for (mutation in mutations){
                loadCosmicMutation(mutation)
                send(mutation)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.createCosmicGeneRelationships(mutations: ReceiveChannel<CosmicMutation>) =
        produce<Int> {
            for (mutation in mutations) {
                createCosmicMutationToGeneRelationship(mutation.geneSymbol, mutation.mutationId)
                send(mutation.mutationId)
                delay(20)
            }
        }

    fun processCosmicMutationData(filename:String) = runBlocking {
       logger.atInfo().log("Loading CosmicMutantExportCensus data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids = createCosmicGeneRelationships(
            loadCosmicMutations(
                parseCosmicMutationFile(filename)
            )
        )
        for (id in ids) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
       logger.atInfo().log(
            "CosmicMutantExportCensus data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds"
        )
    }

}
fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/sample_CosmicMutantExportCensus.tsv"
    CosmicMutationLoader.processCosmicMutationData(filename)
}