package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicMutation
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicTumor
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.*
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

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
        val recordFlow = CosmicMutantExportFlow(scope, filename)
        val tr = TumorReceiver(recordFlow, scope)
        val mr = MutationReceiver(recordFlow, scope)
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
    private val csvFlow = MutableSharedFlow<CSVRecord>(replay = 0)
    val csvRecordFlow: SharedFlow<CSVRecord> = csvFlow
    val path = Paths.get(filename)

    init {
        externalScope.launch {
            TsvRecordSequenceSupplier(path).get()
                .forEach {
                    csvFlow.emit(it)
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
            //mutationFlow.csvRecordFlow.collect { parseTumorRecord(it) }
            mutationFlow.csvRecordFlow.collect { parseMutationRecord(it) }
            delay(200)
        }
    }

    private val logger: FluentLogger = FluentLogger.forEnclosingClass()
    var mutationNodeCount = 0

    suspend fun parseMutationRecord(record: CSVRecord) {
        loadCosmicMutationModel(CosmicMutation.parseCsvRecord(record))
        mutationNodeCount += 1
        delay(20)
    }

    suspend fun loadCosmicMutationModel(mutation: CosmicMutation) {
        loadCosmicMutation(mutation)
        delay(20)
        createCosmicGeneRelationship(mutation)
    }

    suspend fun createCosmicGeneRelationship(mutation: CosmicMutation) {
        createCosmicMutationToGeneRelationship(mutation.geneSymbol, mutation.mutationId)
        delay(20)
        logCosmicMutation(mutation)
    }

    suspend fun logCosmicMutation(mutation: CosmicMutation) {
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
            //tumorFlow.csvRecordFlow.collect { parseTumorRecord(it) }
            tumorFlow.csvRecordFlow.collect { parseTumorRecord(it) }
            delay(200)
        }
    }

    private val logger: FluentLogger = FluentLogger.forEnclosingClass()
    var tumorNodeCount = 0

    suspend fun parseTumorRecord(record: CSVRecord) {
        loadCosmicTumorData(CosmicTumor.parseCsvRecord(record))
        tumorNodeCount += 1
        delay(20)
    }

    suspend fun loadCosmicTumorData(tumor: CosmicTumor) {
        loadCosmicTumor(tumor)
        delay(20)
        processTumorSiteType(tumor)
    }

    suspend fun processTumorSiteType(tumor: CosmicTumor) {
        CosmicTypeDao.processCosmicTypeNode(tumor.site)
        delay(20)
        processTumorHistologyType(tumor)
    }

    suspend fun processTumorHistologyType(tumor: CosmicTumor) {
        CosmicTypeDao.processCosmicTypeNode(tumor.histology)
        delay(20)
        processSampleRelationship(tumor)
    }

    suspend fun processSampleRelationship(tumor: CosmicTumor) {
        createCosmicSampleRelationship(tumor)
        delay(20)
        processMutationRelationship(tumor)
    }

    suspend fun processMutationRelationship(tumor: CosmicTumor) {
        createCosmicMutationRelationship(tumor)
        delay(20)
        processPubMedRelationship(tumor)
    }

    suspend fun processPubMedRelationship(tumor: CosmicTumor) {
        createPubMedRelationship(tumor)
        delay(20)
        logCosmicTumor(tumor)
    }

    suspend fun logCosmicTumor(tumor: CosmicTumor) {
        logger.atInfo().log("CosmicTumor id: ${tumor.tumorId}   loaded into Neo4j}")
    }
}

/*
main function for Neo4j integration testing
 */
fun main(args: Array<String>) = runBlocking {
    val scope = CoroutineScope(Dispatchers.IO)
    val filename = when (args.isNotEmpty()) {
        true -> args[0]
        false -> "./data/sample_CosmicMutantExportCensus.tsv"
    }
    val stopwatch = Stopwatch.createStarted()
    val recordFlow = CosmicMutantExportFlow(scope, filename)
    val tr = TumorReceiver(recordFlow, scope)
    val mr = MutationReceiver(recordFlow, scope)
    stopwatch.elapsed(TimeUnit.SECONDS)
    println("Elapsed time: ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds")
    println("Cancelling children")
    with(coroutineContext) {
        stopwatch.elapsed(TimeUnit.SECONDS)
        println("Elapsed time: ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds")
        println("Cancelling children")
        cancelChildren()
    }
}