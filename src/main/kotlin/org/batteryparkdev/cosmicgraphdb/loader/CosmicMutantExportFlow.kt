package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
//import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.cosmicgraphdb.model.CosmicMutation
import org.batteryparkdev.cosmicgraphdb.model.CosmicTumor
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.neo4j.driver.Value
import java.util.concurrent.TimeUnit

/*
Responsible for loading data from a CosmicMutantExport or CosmicMutantExportCensus file
into the Neo4j database as CosmicTumors and CosmicMutations
The two (2) data pipelines (i.e. tumors and mutations are processed using
a single Kotlin Flow
 */

object CosmicMutantExportLoader {
    fun loadMutantExportFile(filename: String) = runBlocking{
        val scope = CoroutineScope(Dispatchers.IO)
        val stopwatch = Stopwatch.createStarted()
        val valueFlow = CosmicMutantExportFlow(scope, filename)
        val tr = TumorReceiver(valueFlow, scope)
        val mr = MutationReceiver(valueFlow, scope)
        with(coroutineContext) {
            stopwatch.elapsed(TimeUnit.SECONDS)
            println("Elapsed time: ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds")
            println("Cancelling children")
            cancelChildren()
        }
    }
}

class CosmicMutantExportFlow(
    private val externalScope: CoroutineScope,
    filename: String,
    private val intervalMs: Long = 50
) {
    val logger: FluentLogger = FluentLogger.forEnclosingClass()

    // Backing property to avoid flow emissions from other classes
    private val valueFlow = MutableSharedFlow<Value>(replay = 0)
    val nodeVlaueFlow: SharedFlow<Value> = valueFlow
    //val path = Paths.get(filename)
    init {
        externalScope.launch {
            ApocFileReader.processDelimitedFile(filename)
                .map { record -> record.get("map") }
                .forEach { valueFlow.emit(it)
                    delay(intervalMs)
                }
        }
    }
}

/*
Class to load Mutation data from the CosmicMutantExport file into
Neo4j
 */
class MutationReceiver(
    private val mutationFlow: CosmicMutantExportFlow,
    private val externalScope: CoroutineScope
) {
    init {
        externalScope.launch {
            mutationFlow.nodeVlaueFlow.collect { parseMutationRecord(it) }
            delay(200)
        }
    }

    private val logger: FluentLogger = FluentLogger.forEnclosingClass()
    var mutationNodeCount = 0

    private suspend fun parseMutationRecord(value:Value) {
        loadCosmicMutationModel(CosmicMutation.parseValueMap(value))
        mutationNodeCount += 1
        delay(20)
    }

    suspend fun loadCosmicMutationModel(mutation: CosmicMutation) {
        Neo4jConnectionService.executeCypherCommand(mutation.generateCosmicMutationCypher())
        delay(20)
        loadMutationPubMedRelationship(mutation)
    }

    suspend fun loadMutationPubMedRelationship(mutation: CosmicMutation) {
        mutation.createPubMedRelationship(mutation.pubmedId)
        delay(20)
        logCosmicMutation(mutation)
    }


     fun logCosmicMutation(mutation: CosmicMutation) {
        logger.atInfo().log("CosmicMutation id: ${mutation.mutationId}   loaded into Neo4j}")
    }
}

/*
Class to load Tumor data from the CosmicMutantExport file into
Neo4j
 */
class TumorReceiver(
    private val tumorFlow: CosmicMutantExportFlow,
    private val externalScope: CoroutineScope
) {
    init {
        externalScope.launch {
            //mutationFlow.nodeVlaueFlow.collect { parseMutationRecord(it) }
            tumorFlow.nodeVlaueFlow.collect { parseTumorRecord(it) }
            delay(200)
        }
    }

    private val logger: FluentLogger = FluentLogger.forEnclosingClass()
    var tumorNodeCount = 0

    suspend fun parseTumorRecord(value: Value) {
        val tumor =CosmicTumor.parseValueMap(value)
        tumorNodeCount += 1
        delay(20)
        loadCosmicTumorData(tumor)
    }

    suspend fun loadCosmicTumorData(tumor: CosmicTumor) {
        Neo4jConnectionService.executeCypherCommand(tumor.generateCosmicTumorCypher())
        delay(20)
        logCosmicTumor(tumor)
    }

    suspend fun logCosmicTumor(tumor: CosmicTumor) {
        logger.atInfo().log("CosmicTumor id: ${tumor.tumorId}   loaded into Neo4j}")
    }
}

